var newman = require('newman');

var args = process.argv.slice(2);

var host = args[0];
var userName = args[1];
var Password = args[2];
var target_os = args[3];

var myEnvironment = require('./jwala.postman_environment.json');
	myEnvironment.values.push({
	  "key": "host",
	  "type": "text",
	  "value": host,
	  "enabled": true,
	  "warning": ""
	});
	myEnvironment.values.push({
      "key": "userName",
      "type": "text",
      "value": userName,
      "enabled": true,
      "warning": ""
    });
	myEnvironment.values.push({
      "key": "Password",
      "type": "text",
      "value": Password,
      "enabled": true,
      "warning": ""
    });
	myEnvironment.values.push({
      "key": "target_os",
      "type": "text",
      "value": target_os,
      "enabled": true,
      "warning": ""
    });
                
newman.run({
    collection: require('./jwala-collection.postman_collection.json'),
                environment: myEnvironment,
                insecure: true,
				bail: true,
    reporters: 'cli'
}).on('start', function (err, args) { // on start of run, log to console
    console.log('running a collection...');
}).on('done', function (err, summary) {
    if(summary.run.failures.length !== 0){
    process.exit(-1);
	}
    else {
        console.log('collection run completed.');
    }
});
