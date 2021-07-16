var express = require('express');
var router = express.Router();

/* GET users listing. */
router.get('/', function(req, res, next) {
  console.log(req.session);
  if (req.session.id) {
    console.log("userinfo user", req.session.id)
  }
  res.render('userinfo', req.session.passport.user);
});

module.exports = router;
