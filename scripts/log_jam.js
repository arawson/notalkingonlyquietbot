#!/usr/bin/env node
//TODO: load other scripts and one-offs and commands independantly

const Promise = require('bluebird');
const toml = Promise.promisifyAll(require('toml'));
const fs = Promise.promisifyAll(require('fs'));
const Discord = Promise.promisifyAll(require('discord.js'));

var conf = {};

const client = new Discord.Client();

var stringifier = stringify({
	delimiter: ','
});

client.onAsync('ready')
.then(function() {
	console.log('connected to discord');

	var th = null;
	for (i in client.guilds.array()) {
		var guild = client.guilds.array()[i];
		if (guild.name == 'Tempest Horizon') {
			th = guild;
		}
	}

	if (th != null) {
		console.log('got tempest horizon');
	} else {
		console.log('couldn\'t find the server we wanted');
		process.exit(-1);
	}

	if (!th.available) {
		console.log('found server but its not available');
		process.exit(-1);
	}

	var ot = null;
	for (i in th.channels.array()) {
		var channel = th.channels.array()[i];
		//console.log('channel: ' + channel.name + ' is ' + channel.type);
		if (channel.name == 'off-topic') {
			ot = channel;
		}
	}
	if (ot != null) {
		console.log('got off-topic channel');
	} else {
		console.log('couldn\'t get off-topic channel');
		process.exit(-1);
	}

	if (ot.type != 'text') {
		console.log('off-topic is of type ' + ot.type + ' which this script'
		+ ' doesn\'t handle');
		process.exit(-1);
	}

	return ot.fetchMessages({limit: 10});
})
.then(function(messages) {
	console.log('received ' + messages.size + ' messages');
	var a = messages.array();
	var ms = [];
	for (i in a) {
		var m = a[i];
		console.log(m.content);
		ms.push({
			text: m.content
		});
	}
	
})
.then(function(output) {
	console.log(output);
	process.exit();
})
.catch(console.error);

//TODO: better error handling
fs.readFileAsync('../config.toml', 'utf8').then(function(data) {
	console.log('loaded config file');
	conf = toml.parse(data);
	//console.dir(conf);
	client.login(conf.login.token);
});

