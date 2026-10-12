import { useCartStore } from "../store/cartStore";

interface CartProps {
    onCheckout?: () => void;
}

function Cart({ onCheckout }: CartProps) {

    const cartItems = useCartStore(
        (state) => state.cartItems
    );

    const increaseQuantity = useCartStore(
        (state) => state.increaseQuantity
    );

    const decreaseQuantity = useCartStore(
        (state) => state.decreaseQuantity
    );

    const clearCart = useCartStore(
        (state) => state.clearCart
    );

    const total = cartItems.reduce(
        (sum, item) =>
            sum + item.product.price * item.quantity,
        0
    );

    const numberOfItems = cartItems.reduce(
        (sum, item) => sum + item.quantity,
        0
    );

    return (
        <div className="cart">

            <h2>Shopping Cart</h2>

            {cartItems.length === 0 ? (

                <p>Your cart is empty.</p>

            ) : (

                <>

                    {cartItems.map((item) => (

                        <div
                            className="cart-item"
                            key={item.product.productId}
                        >

                            <img
                                src={item.product.imageUrl}
                                alt={item.product.productName}
                            />

                            <div className="cart-item-details">

                                <h3>
                                    {item.product.productName}
                                </h3>

                                <p>
                                    Price: ₹{item.product.price}
                                </p>

                                <div className="quantity-controls">

                                    <button
                                        onClick={() =>
                                            decreaseQuantity(
                                                item.product.productId
                                            )
                                        }
                                    >
                                        -
                                    </button>

                                    <span>
                                        {item.quantity}
                                    </span>

                                    <button
                                        onClick={() =>
                                            increaseQuantity(
                                                item.product.productId
                                            )
                                        }
                                    >
                                        +
                                    </button>

                                </div>

                                <p>
                                    Subtotal: ₹
                                    {item.product.price *
                                        item.quantity}
                                </p>

                            </div>

                        </div>

                    ))}

                    <div className="cart-summary">

                        <h2>
                            Total: ₹{total}
                        </h2>

                        <p>
                            Total Items: {numberOfItems}
                        </p>

                        <div className="cart-buttons">

                            <button onClick={clearCart}>
                                Clear Cart
                            </button>

                            <button
                                onClick={onCheckout}
                                disabled={!onCheckout}
                            >
                                Proceed to Checkout
                            </button>

                        </div>

                    </div>

                </>

            )}

        </div>
    );
}

export default Cart;