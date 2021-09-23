var exec = require("cordova/exec");

var PLUGIN_NAME = "rootSafety";

var rootSafety = {
  checkGooglePlayServicesAvailability: function(cb, err) {
    exec(cb, err, PLUGIN_NAME, "checkGooglePlayServicesAvailability", []);
  },
  attest: function(nonce, api_key, cb, err) {
    exec(cb, err, PLUGIN_NAME, "attest", [nonce, api_key]);
  },
  buildInfo: function(cb, err) {
    exec(cb, err, PLUGIN_NAME, "buildInfo", []);
  }
};

if (typeof module != "undefined" && module.exports) {
  module.exports = rootSafety;
}
