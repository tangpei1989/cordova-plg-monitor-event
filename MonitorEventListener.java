/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package ewell.plugin.cordova.moniterevent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class MonitorEventListener extends CordovaPlugin {

    private static final String LOG_TAG = "MonitorEventListener";

    // moniterEvent args
    public static final String MONITER_EVENT_NAME = "ewell.plugin.cordova.moniterevent.event_name";
    public static final String MONITER_EVENT_ARGS = "ewell.plugin.cordova.moniterevent.event_args";

    public static final String MONITER_INTENT_ACTION = "ewell.plugin.cordova.moniterevent.intent_action"

    BroadcastReceiver receiver;

    private CallbackContext moniterCallbackContext = null;

    /**
     * Constructor.
     */
    public MonitorEventListener() {
        this.receiver = null;
    }

    /**
     * Executes the request.
     *
     * @param action        	The action to execute.
     * @param args          	JSONArry of arguments for the plugin.
     * @param callbackContext 	The callback context used when calling back into JavaScript.
     * @return              	True if the action was valid, false if not.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        if (action.equals("start")) {
            if (this.moniterCallbackContext != null) {
                callbackContext.error( "Battery listener already running.");
                return true;
            }
            this.moniterCallbackContext = callbackContext;

            // We need to listen to power events to update battery status
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(MONITER_INTENT_ACTION);
            if (this.receiver == null) {
                this.receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        updateMoniterEventInfo(intent);
                    }
                };
                webView.getContext().registerReceiver(this.receiver, intentFilter);
            }

            // Don't return any result now, since status results will be sent when events come in from broadcast receiver
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            return true;
        }

        else if (action.equals("stop")) {
            removeMoniterListener();
            this.sendUpdate(new JSONObject(), false); // release status callback in JS side
            this.moniterCallbackContext = null;
            callbackContext.success();
            return true;
        }

        return false;
    }

    /**
     * Stop battery receiver.
     */
    public void onDestroy() {
        removeMoniterListener();
    }

    /**
     * Stop battery receiver.
     */
    public void onReset() {
        removeMoniterListener();
    }

    /**
     * Stop the battery receiver and set it to null.
     */
    private void removeMoniterListener() {
        if (this.receiver != null) {
            try {
                webView.getContext().unregisterReceiver(this.receiver);
                this.receiver = null;
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error unregistering battery receiver: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Creates a JSONObject with the current battery information
     *
     * @param batteryIntent the current battery information
     * @return a JSONObject containing the battery status information
     */
    private JSONObject getMoniterEventInfo(Intent moniterIntent) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("event", moniterIntent.getStringExtra(MONITER_EVENT_NAME));
            obj.put("args", moniterIntent.getStringArrayExtra(MONITER_EVENT_ARGS);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return obj;
    }

    /**
     * Updates the JavaScript side whenever the battery changes
     *
     * @param batteryIntent the current battery information
     * @return
     */
    private void updateMoniterEventInfo(Intent moniterIntent) {
        sendUpdate(this.getMoniterEventInfo(moniterIntent), true);
    }

    /**
     * Create a new plugin result and send it back to JavaScript
     *
     * @param connection the network info to set as navigator.connection
     */
    private void sendUpdate(JSONObject info, boolean keepCallback) {
        if (this.moniterCallbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, info);
            result.setKeepCallback(keepCallback);
            this.moniterCallbackContext.sendPluginResult(result);
        }
    }
}
