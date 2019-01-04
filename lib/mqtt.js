'user strict'

const mqtt = require('mqtt');
const log = require('./log');

const topicBase = "home/socket/";
const url = process.env.MQTT_URL;
const opts = { username: process.env.MQTT_USERNAME, password: process.env.MQTT_PASSWORD };

// Create a client connection
var client = mqtt.connect(url, opts);

client.on('connect', function() { // When connected
    log.info('MQTT: Successfully connected');
    // subscribe to topic
    client.subscribe(topicBase+'+');
});

client.on('close', function() {
   log.warn('MQTT: Connection closed');
});

module.exports = {
    publish: function(socket, action) {
        client.publish(topicBase+socket+'/set', action);
    }
};