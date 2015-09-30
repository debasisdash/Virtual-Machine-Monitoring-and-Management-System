/**
 * Module dependencies.
 */

var express = require('express')
  , routes = require('./routes')
  , user = require('./routes/user')
  , http = require('http')
  , path = require('path');

var Client = require('node-rest-client').Client;
var client = new Client();
//var Db = require('mongodb').Db;
//var mongo = require('mongodb').MongoClient;

var index = require('./routes/index');
//Connection URL 
//var url = 'mongodb://Team_4:team4@ds031802.mongolab.com:31802/cmpe283_vmmm';
// Use connect method to connect to the Server 
//var db = mongo.connect(url, function(err, db) {
  //assert.equal(null, err);
//  console.log("Connected correctly to server");
//  db.close();
//});

var app = express();

// all environments
app.set('port', process.env.PORT || 8080);
app.set('views', __dirname + '/views');
app.set('view engine', 'ejs');
app.use(express.favicon());
app.use(express.logger('dev'));
app.use(express.bodyParser());
app.use(express.methodOverride());
app.use(app.router);
app.use(express.static(path.join(__dirname, 'public')));

// development only
if ('development' === app.get('env')) {
  app.use(express.errorHandler());
}

app.get('/', routes.index);
app.get('/users', user.list);
app.get('/index/analytics/:id', routes.analytics);
app.get('/index/charts_post/:id', routes.charts_post);
app.get('/index/charts', routes.charts);
app.get('/index/tables', routes.tables);
app.get('/index/forms', routes.forms);
app.get('/index/bootstrapElements', routes.bootstrapElements);
app.get('/index/bootstrapGrid', routes.bootstrapGrid);
app.get('/index/logger', routes.logger);
app.get('/index/indexRtl', routes.indexRtl);
app.post('/index/auth', index.auth);


http.createServer(app).listen(app.get('port'), function(){
  console.log('Express server listening on port ' + app.get('port'));
});
