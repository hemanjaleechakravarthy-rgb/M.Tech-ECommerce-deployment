const express = require('express');
const session = require('express-session');

const app = express();

app.use(express.json());

app.use(
  session({
    secret: 'your-secret-key',
    resave: false,
    saveUninitialized: false,
  })
);

app.get('/', (req, res) => {
  res.send('Session Authentication Example');
});

app.listen(3000, () => {
  console.log('Server running on http://localhost:3000');
});