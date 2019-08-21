var cordova = require('cordova');
var channel = require('cordova/channel');
var exec = require('cordova/exec');

console.log("androidScreenshot");

var androidScreenshot = function () {
    
};

androidScreenshot.prototype.startScreenshotDetect = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "androidScreenshot", "start", []);
};


channel.onCordovaReady.subscribe(function () {
    screenshot.startScreenshotDetect(function (info) {
        setTimeout(function () {
        	console.log("androidScreenshotDetect");
            cordova.fireDocumentEvent('screenshotDetect');
        }, 500);
        
    },
    function (e) {
        console.log('Error: ' + e);
    });
});

var screenshot = new androidScreenshot(); // jshint ignore:line

module.exports = screenshot;