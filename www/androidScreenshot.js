var cordova = require('cordova');
var channel = require('cordova/channel');
var exec = require('cordova/exec');

console.log("androidScreenshot");

var androidScreenshot = function () {
};

androidScreenshot.prototype.startScreenshotDetect = function (successCallback, errorCallback) {
    console.warn('<--- androidScreenshotDetectStart --->')
    exec(successCallback, errorCallback, "androidScreenshot", "start", []);
};

androidScreenshot.prototype.stopScreenshotDetect = function (successCallback, errorCallback) {
    console.warn('<--- androidScreenshotDetectStop --->')
    exec(successCallback, errorCallback, "androidScreenshot", "stop", []);
};


channel.onCordovaReady.subscribe(function () {
    screenshot.startScreenshotDetect(function (info) {
    	console.warn('startScreenshotDetect', info);
        setTimeout(function () {
            cordova.fireDocumentEvent('screenshotDetect');
        }, 300);
        
    },
    function (e) {
        console.log('Error: ' + e);
    });
});

var screenshot = new androidScreenshot(); // jshint ignore:line

module.exports = screenshot;