const session = require('express-session');
const createError = require('http-errors');
const express = require('express');
const path = require('path');
const cookieParser = require('cookie-parser');
const logger = require('morgan');
const axios = require("axios");

const indexRouter = require('./routes/index');
const userinfoRouter = require('./routes/userinfo');

const app = express();

app.use(express.static('public'))

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'hbs');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());

app.use(session({ secret: "big secret" }));

app.use('/', indexRouter);
app.use('/userinfo', userinfoRouter);


// passport.js
const passport = require('passport')
  , OAuth2Strategy = require('passport-oauth2');

app.use(passport.initialize());
app.use(passport.session());

const oauth2Strategy = new OAuth2Strategy({
  authorizationURL: process.env.OAUTH_AUTHORIZATION_URL,
  tokenURL: process.env.OAUTH_TOKEN_URL,
  clientID: process.env.OAUTH_CLIENT_ID,
  clientSecret: process.env.OAUTH_CLIENT_SECRET,
  callbackURL: process.env.OAUTH_CALLBACK_URL,
},
  function (accessToken, refreshToken, profile, done) {
    console.log("provider: ", accessToken, refreshToken, profile);
    return done(null, profile);
  }
)

oauth2Strategy.userProfile = function (accessToken, done) {
  console.log("userProfile", accessToken);

  const token = 'Bearer '.concat(accessToken);
  axios.get(process.env.OAUTH_USERINFO_URL, { headers: { Authorization: token } })
    .then(response => {
      return done(null, response.data);
    })
    .catch((error) => {
      console.log('error ' + error);
      return done(null, "");
    });

}

passport.use('provider', oauth2Strategy);

app.post('/logout', function (req, res) {
  req.session.destroy();
  res.redirect(process.env.OAUTH_LOGOUT_URL);
});

app.get('/oidc/auth', passport.authenticate('provider', { scope: ['openid', 'profile', 'email'] }));

app.get('/oidc/callback',
  passport.authenticate('provider', {
    successRedirect: '/userinfo',
    failureRedirect: '/'
  }));

// Passport session management
passport.serializeUser(function (user, done) {
  done(null, user);
});

passport.deserializeUser(function (user, done) {
  done(null, user);
});

// catch 404 and forward to error handler
app.use(function (req, res, next) {
  next(createError(404));
});

// error handler
app.use(function (err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

module.exports = app;
