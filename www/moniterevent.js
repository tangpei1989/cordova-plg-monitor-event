/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/

/**
 * This class contains information about the current battery status.
 * @constructor
 */
var cordova = require('cordova'),
    exec = require('cordova/exec');

var MoniterEvent = function() {
    // Create new event handlers on the window (returns a channel instance)
    this.channels = {
      moniterevent:cordova.addWindowEventHandler("moniterevent"),
    };

    for (var key in this.channels) {
        this.channels[key].onHasSubscribersChange = MoniterEvent.onHasSubscribersChange;
    }
};

function handlers() {
    return moniterevent.channels.moniterevent.numHandlers;
}

/**
 * Event handlers for when callbacks get registered for the battery.
 * Keep track of how many handlers we have so we can start and stop the native battery listener
 * appropriately (and hopefully save on battery life!).
 */
MoniterEvent.onHasSubscribersChange = function() {
  // If we just registered the first handler, make sure native listener is started.
  if (this.numHandlers === 1 && handlers() === 1) {
      exec(moniterevent._status, moniterevent._error, "MonitorEvent", "start", []);
  } else if (handlers() === 0) {
      exec(null, null, "MonitorEvent", "stop", []);
  }
};

/**
 * Callback for battery status
 *
 * @param {Object} info            keys: level, isPlugged
 */
MoniterEvent.prototype._status = function (info) {

    if (info) {
        // Something changed. Fire batterystatus event
        cordova.fireWindowEvent("moniterevent", info);
    }
};

/**
 * Error callback for Moniter Event start
 */
MoniterEvent.prototype._error = function(e) {
    console.log("Error initializing Moniter Event: " + e);
};

var moniterevent = new MoniterEvent(); // jshint ignore:line

module.exports = moniterevent;
