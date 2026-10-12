package com.klu.ecommerce_backend.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.klu.ecommerce_backend.model.BackgroundJob;
import com.klu.ecommerce_backend.model.Order;
import com.klu.ecommerce_backend.repository.BackgroundJobRepository;
import com.klu.ecommerce_backend.repository.OrderRepository;

@Service
public class BackgroundJobProcessor {

    private final BackgroundJobRepository jobRepository;
    private final JobNotificationService notifier;
    private final JavaMailSender mailSender;
    private final OrderRepository orderRepository;

    @Value("${smartcart.mail.from}")
    private String mailFrom;

    public BackgroundJobProcessor(
            BackgroundJobRepository jobRepository,
            JobNotificationService notifier,
            JavaMailSender mailSender,
            OrderRepository orderRepository) {

        this.jobRepository = jobRepository;
        this.notifier = notifier;
        this.mailSender = mailSender;
        this.orderRepository = orderRepository;
    }

    @Async("taskExecutor")
    public void process(Long jobId) {

        BackgroundJob job = jobRepository.findById(jobId).orElseThrow();

        try {

            update(job, "PROCESSING", "Task started");

            switch (job.getJobType()) {

                case "EMAIL" -> sendOrderEmail(job);

                case "INVOICE" -> generateInvoice(job);

                case "NOTIFICATION" -> sendNotification(job);

                default -> throw new RuntimeException("Unknown job type");
            }

            update(job, "COMPLETED", "Task completed successfully");

        } catch (Exception e) {

            System.err.println("========================================");
            System.err.println("BACKGROUND JOB FAILED");
            System.err.println("Job ID: " + job.getJobId());
            System.err.println("Job Type: " + job.getJobType());
            System.err.println("Error: " + e.getMessage());
            System.err.println("========================================");

            e.printStackTrace();

            /*
             * Prevent very long exception messages from causing
             * another database error when saving FAILED status.
             */
            String errorMessage = e.getMessage();

            if (errorMessage == null || errorMessage.isBlank()) {
                errorMessage = "Background job failed";
            }

            if (errorMessage.length() > 250) {
                errorMessage = errorMessage.substring(0, 250);
            }

            update(job, "FAILED", errorMessage);
        }
    }

    @Transactional
    public void update(
            BackgroundJob job,
            String status,
            String message) {

        job.setStatus(status);
        job.setMessage(message);

        jobRepository.save(job);

        notifier.publish(job);
    }

    private void sendOrderEmail(BackgroundJob job) {

        System.out.println("========================================");
        System.out.println("EMAIL JOB STARTED: " + job.getJobId());

        Order order = orderRepository.findById(job.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        System.out.println("ORDER FOUND: " + order.getOrderId());

        String customerEmail = order.getUser().getEmail();

        System.out.println("CUSTOMER EMAIL: " + customerEmail);
        System.out.println("MAIL FROM: " + mailFrom);
        System.out.println("MAIL TO: " + customerEmail);

        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setFrom(mailFrom);
        mail.setTo(customerEmail);

        mail.setSubject("SmartCart Order Confirmation");

        mail.setText(
                "Dear " + order.getUser().getName() + ",\n\n"
                + "Your order " + order.getOrderId()
                + " has been confirmed successfully.\n"
                + "Total Amount: ₹" + order.getTotalAmount()
                + "\n\n"
                + "Thank you for shopping with SmartCart!"
        );

        System.out.println("ABOUT TO SEND EMAIL...");

        mailSender.send(mail);

        System.out.println(
                "EMAIL SENT SUCCESSFULLY: " + job.getJobId()
        );

        System.out.println("========================================");
    }

    private void generateInvoice(BackgroundJob job) {

        try {

            Path folder = Paths.get("invoices");

            Files.createDirectories(folder);

            String html = """
                <html>
                <body>
                    <h1>SmartCart Invoice</h1>
                    <p>Order ID: %d</p>
                    <p>Payment: Successful</p>
                    <p>Thank you for shopping with SmartCart!</p>
                </body>
                </html>
                """.formatted(job.getOrderId());

            Files.writeString(
                    folder.resolve(
                            "invoice-" + job.getOrderId() + ".html"
                    ),
                    html,
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Invoice generation failed",
                    e
            );
        }
    }

    private void sendNotification(BackgroundJob job) {

        System.out.println(
                "SmartCart notification: Order "
                + job.getOrderId()
                + " payment confirmed"
        );
    }
}