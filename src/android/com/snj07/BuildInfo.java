package com.snj07;

import android.app.Activity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.safetynet.SafetyNet;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RootSafety extends CordovaPlugin {

    // actions
    private final String CHECK_GOOGLE_PLAY_SERVICES_AVAILABILITY = "checkGooglePlayServicesAvailability";
    private final String ATTEST_ACTION = "attest";
    private final String BUILD_INFO_ACTION = "buildInfo";

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals(CHECK_GOOGLE_PLAY_SERVICES_AVAILABILITY)) {
            checkGooglePlayServicesAvailability(callbackContext);
        }

        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                this.cordova.getActivity().getApplicationContext()) == ConnectionResult.SUCCESS) {
            switch (action) {
                case ATTEST_ACTION: {
                    String nonce = args.getString(0);
                    String key = args.getString(1);
                    this.handleAttestRequest(nonce, key, RootSafety.this.cordova.getActivity(), callbackContext);
                    break;
                }
                case BUILD_INFO_ACTION:{
                    try {
                        String buildConfigClassName = null;
                        if (1 < args.length()) {
                            buildConfigClassName = args.getString(0);
                        }
                        JSONObject buildInfo = BuildInfo.GetBuildInfo(buildConfigClassName, this.cordova.getActivity());

                        callbackContext.success(buildInfo);
                    } catch (Exception e){
                        callbackContext.error(createJsonReponse(SafetyNetHandler.STATUS_ERROR, "error in getting build info"));
                    }
                }
            }

        } else {
            // play service not supported
            callbackContext.error("Play Services not supported");
        }
        return true;
    }

    private void handleAttestRequest(String nonce, String key, Activity activity, CallbackContext callbackContext) {
        try {
            SafetyNet.getClient(activity).attest(nonce.getBytes(), key)
                    .addOnSuccessListener(activity,
                            response -> callbackContext.success(createJsonReponse(SafetyNetHandler.STATUS_SUCCESS, response.getJwsResult())))
                    .addOnFailureListener(activity,
                            et -> callbackContext.success(createJsonReponse(SafetyNetHandler.STATUS_FAILURE, et.getLocalizedMessage())));
        } catch (Exception e) {
            callbackContext.error(createJsonReponse(SafetyNetHandler.STATUS_FAILURE, e.getLocalizedMessage()));
        }
    }

    private JSONObject createJsonReponse(String status, String reponse) {
        Map<String, String> data = new HashMap<>();
        data.put(SafetyNetHandler.STATUS_FIELD, status);
        data.put(SafetyNetHandler.RESPONSE_FIELD, reponse);
        return new JSONObject(data);
    }

    private void checkGooglePlayServicesAvailability(CallbackContext callbackContext) {
        String response = new SafetyNetHandler().checkGooglePlayServicesAvailability(this.cordova.getActivity());
        if (response.equals(SafetyNetHandler.STATUS_ERROR)) {
            callbackContext.error(SafetyNetHandler.STATUS_ERROR);
        }
        callbackContext.success(response);
    }
}