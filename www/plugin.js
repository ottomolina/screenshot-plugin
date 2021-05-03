
var exec = require('cordova/exec');

var PLUGIN_NAME = 'ScreenshotPG';

var ScreenshotPG = {
  captureScreen: function (name, successCallback, errorCallback){
    exec(successCallback, errorCallback, PLUGIN_NAME, "captureScreen", [name]);
  },

};

module.exports = ScreenshotPG;
