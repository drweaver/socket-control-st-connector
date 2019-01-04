'use strict';

const express = require('express');
const bodyParser = require('body-parser');
const mqtt = require('./lib/mqtt');

const log = require('./lib/log');

const app = module.exports = express();
app.use(bodyParser.json());
app.post('/', function(req, response) {
    handleCallback(req, response)
});

function handleCallback(req, response) {
    let evt = req.body;
    // sample: { action: "on", socket: "1" }
    log.trace(`REQUEST: ${JSON.stringify(evt, null, 2)}`);
    mqtt.publish(evt.socket, evt.action);
    log.response(response, {status: evt.action, socket: evt.socket});
}

app.listen(3003);
log.info('Open: http://127.0.0.1:3003');
