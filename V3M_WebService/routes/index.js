var mongoClient = require('mongodb').MongoClient;
var url = 'mongodb://Team_4:team4@ds031802.mongolab.com:31802/cmpe283_vmmm';
var Client = require('node-rest-client').Client;
var client = new Client()
var dt={};
var util = require('util')
/*
 * GET home page.
 */
exports.index = function(req, res){
	res.render('index', { title: 'Express' });
};


exports.auth = function(req,res){
	console.log("Inside auth");
	console.log(req.body);
	var user = req.body.user;
	var password = req.body.password;
	console.log("user:"+user+" password:"+password);

	//Getting the Data from MonogoDB using REST Api
	client.get("https://api.mongolab.com/api/1/databases/cmpe283_vmmm/collections/User?apiKey=2RIIDMoQ2hehmGBUhi3XqzcJ1l8LF6Xm", 
			function(data, response){
		for(var i=0;i<data.length;i++)
		{
			dt['id'] = data[i]._id;
			dt['user'] = data[i].user;
			if(dt['user'] === user){
				dt['password'] = data[i].password;
				if(dt['password'] === password){
					console.log("Authentication successful");
					client.get("https://api.mongolab.com/api/1/databases/cmpe283_vmmm/collections/datacenter?apiKey=2RIIDMoQ2hehmGBUhi3XqzcJ1l8LF6Xm", 
					function(data, response){
						res.render('Homepage', { title: 'Welcome to list page',data:data});
					});
				}
				else{
					continue;
				}
			}		
		}
	});
};

exports.analytics = function(req,res){
		console.log("params:"+req.params.id);
		var datacenter =req.params.id;
		console.log(util.inspect(datacenter));
		var url="https://api.mongolab.com/api/1/databases/cmpe283_vmmm/collections/"+datacenter+"_VM_Details?apiKey=2RIIDMoQ2hehmGBUhi3XqzcJ1l8LF6Xm";
		client.get(url,function(data, response){
					for(var i=0; i<data.length;i++){
						console.log(data[i].VM_Name);
						console.log(data[i].VM_IPAddress);
						console.log(data[i].Max_CPU_Usage);
						console.log(data[i].Max_Memory);
						console.log(data[i].Number_Of_NICs);
						console.log(data[i].Number_Of_CPUs);
						console.log(data[i].Number_Of_Disks);
						console.log(data[i].UpTime);
						console.log(data[i].MemUsage);
				}
				res.render('analytics', { title: 'Welcome analytics page',data:data,datacenter:datacenter});
			});
	
};

exports.charts = function(req,res){
	client.get("https://api.mongolab.com/api/1/databases/cmpe283_vmmm/collections/VM_Statistics?apiKey=2RIIDMoQ2hehmGBUhi3XqzcJ1l8LF6Xm", 
	function(data,response){
		if(!response){
			console.log("unable to get VM_Statistics data from mongo lab:");
			console.log(util.inspect(response));
		}
		else{
			res.render('charts', { title: 'Welcome chart page', data:data});		
		}
		console.log(util.inspect(data));
	});
};

exports.charts_post = function(req,res){
	console.log("--------------------------");

	var dataCenter = req.params.id;
    //var input = JSON.parse(JSON.stringify(req.body));
    var str = dataCenter.split(":");
    var dc= str[0];
    var vm = str[1];
	console.log(util.inspect(req.body));
	console.log("dc:vm:"+dc+":"+vm);
  

//	var vm_name = req.params.id;
	var stats = [];
	var url = "https://api.mongolab.com/api/1/databases/cmpe283_vmmm/collections/"+dc+"_VM_Statistics?apiKey=2RIIDMoQ2hehmGBUhi3XqzcJ1l8LF6Xm";
	client.get(url,	function(data,response){
		if(!response){
			console.log("unable to get VM_Statistics data from mongo lab:");
			//console.log(util.inspect(response));
		}
		else{
		//console.log("data:"+data);
		var j=0;
		for(var i=0; i<data.length ;i++)
		{
			if(data[i].VM_Name === vm)
			{
				console.log("Found the VM:"+data[i].VM_Name);
				stats[j] = data[i];
				j++;
			}
			else
			{
				continue;
			}
		}
		console.log("stats:"+stats);
		
		}
		console.log(util.inspect(stats));
		res.render('charts_post', { title: 'Welcome chart page', data:stats});		
	});
	
	
	//res.render('charts_post', { title: 'Welcome chart page'});
	
};


exports.logger = function(req,res){
	var url = "https://api.mongolab.com/api/1/databases/cmpe283_vmmm/collections/V3M_LOGS?apiKey=2RIIDMoQ2hehmGBUhi3XqzcJ1l8LF6Xm";
	client.get(url,	function(data,response){
		if(!response){
			console.log("unable to get VM_Statistics data from mongo lab:");
			//console.log(util.inspect(response));
		}
		else{
			for(var i=0;i<data.length;i++)
			{
				console.log("logs:"+data[i].name);
				console.log("logs:"+data[i].datetime);
				console.log("logs:"+data[i].logMsg);
			}
			res.render('logger', { title: 'Welcome to V3M logger page',data:data});
		}
	});
};

exports.tables = function(req,res){
	res.render('tables', { title: 'Welcome tables page'});
};

exports.forms = function(req,res){
	res.render('forms', { title: 'Welcome tables page'});
};

exports.bootstrapElements = function(req,res){
	res.render('bootstrap-elements', { title: 'Welcome bootstrapElements page'});
};

exports.bootstrapGrid = function(req,res){
	res.render('bootstrap-elements', { title: 'Welcome bootstrapGrid page'});
};

exports.indexRtl = function(req,res){
	res.render('index-rtl', { title: 'Welcome index Rtl page'});
};
