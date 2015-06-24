var express = require('express');
var app = express();

app.set('port', (process.env.PORT || 7171));

var pageData = {
  "javascriptsBase": "/assets/javascripts",
  "stylesheetsBase": "/assets/stylesheets",
  "imagesBase": "/assets/images"
};

app.use('/assets', express.static(__dirname + '/public'));

app.set('view engine', 'jade');
app.set('views', './assets/jade');

function beforeAllFilter(req, res, next) {
  app.locals.pretty = true;

  next();
}

app.all('*', beforeAllFilter);

app.get('/', function(req, res){
  res.render('index', pageData);
});

app.get('/poll', function(req, res){
  res.render('poll', pageData);
});

app.get('/view-poll', function(req, res){
  res.render('view-poll', pageData);
});

app.get('/library', function(req, res){
  res.render('library', pageData);
});

app.get('/routes', function(req, res){
  res.render('routes', pageData);
});

app.listen(app.get('port'), function() {
  console.log('Node app is running on port', app.get('port'));
});