(function () {
    'use strict';
    var webSocket,app,pos="";
    var states = {
        name: '',
        msgs: []
    };
    var $$ = Dom7;
    function show_history(status, response) {
        var history = response.messages;
        for (var i=0; i<history.length; i++) {
            var type = history[i].entry.name == states.name ? 'sent' : 'received';
            states.msgs.push({
                name:history[i].entry.name,
                text:history[i].entry.text,
                type:type
            });
        }
    }
    function getPosition(){
        var options = {
      	      enableHighAccuracy: true,
      	      maximumAge: 3600000
      	 }
        var watchid = navigator.geolocation.getCurrentPosition(
        function(position){
        	pos = position.coords.longitude+","+position.coords.latitude+","+position.coords.altitude;
         }, 
         function(error){}, options);
    }
    
    /**
     * Initializes all Vue templates
     */
    function initVue () {

        // Tell Vue that we want to use Framework7-Vue plugin
        Vue.use(Framework7Vue);
        
        // Init chat template
        Vue.component('page-chat', {
            template: '#page-chat',
            data: function() {
                return states;
            },
            methods: {
                /**
                 * Listener gets call when new msg should be sent
                 *
                 * @param {string} text The msg to send
                 * @param {function} clear Call this function to clear the message bar component
                 */
                onSend: function(text, clear) {
                    if (text.trim().length === 0) return;
                    webSocket.send(text);
                    if (typeof clear == 'function') clear();
                }
            }
        });

        // Init Vue
       app = new Vue({
            el: '#app',
            data: function() {
                return states;
            },
            methods: {
                /**
                 * Gets called when user name was entered and user enters chat
                 */
                enterChat: function () {
                    if (this.name.trim().length === 0) {
                    	this.$f7.alert('Please enter your name');
                        return false;
                    }
                    this.msgs.length = 0;
                    this.$f7.mainView.router.load({url: '/chat/'});
                    webSocket = new WebSocket("ws://192.168.137.1/myapp/websocket/chat/"+this.name.trim());
                    webSocket.onmessage = function (data) 
                    { 
                       var messgae = eval("("+data.data+")");
                       var type = messgae.name == states.name ? 'sent' : 'received';
                       var name = type == 'send' ? states.name : messgae.name;
                       states.msgs.push({name:name, text:messgae.text, type:type});
                    };

                }
            },
            framework7: {
                root: '#app',
                // material: true, // Remember to change css paths to ios/material theme!
                material: Framework7.prototype.device.android ? true : false,
                // Mapping of routes -> templates
                routes: [{
                    path: '/chat/',
                    component: 'page-chat'
                }]
            }
        });
   	/*var moreActions = app.$f7.sactions.create({
  	  buttons: [
  	    [
  	      {
  	        text: 'Here comes some optional description or warning for actions below',
  	        label: true,
  	      },
  	      {
  	        text: 'Action 1',
  	      },
  	      {
  	        text: 'Action 2',
  	      },
  	    ],
  	    [
  	      {
  	        text: 'Cancel',
  	        bold: true,
  	      }
  	    ]
  	  ]
  	});
  	$$('.open-more-actions').on('click', function () {
  	  moreActions.open();
  	});*/
       /*  var loginScreen = app.$f7.loginScreen.create({
	 	  el:'#login-screen',
	 	  on: {
	 		    opened: function () {
	 		      console.log('Login Screen opened')
	 		    }
	 		  }
	     });*/

    	 
      /* window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, function (fs) {
           app.$f7.alert('file system open: ' + fs.name);
           fs.root.getFile("newPersistentFile.txt", { create: true, exclusive: false }, function (fileEntry) {
               // fileEntry.name == 'someFile.txt'
               // fileEntry.fullPath == '/someFile.txt'
               writeFile(fileEntry, null);
               
               fileEntry.file(function (file) {
                   var reader = new FileReader();
                   reader.onloadend = function() {
                       displayFileData(fileEntry.fullPath + ": " + this.result);
                       
                       //var blob = new Blob([new Uint8Array(this.result)], { type: "image/png" });
                       //displayImage(blob);
                   };

                   reader.readAsText(file);
                  // reader.readAsArrayBuffer(file);
               }, onErrorReadFile);

           }, onErrorCreateFile);
           
       }, onErrorLoadFs);*/
       
    }
    document.addEventListener("deviceready", onDeviceReady, false);
    // device APIs are available
    function onDeviceReady() {
        document.addEventListener("searchbutton", function(){navigator.notification.alert("searchbutton")}, false);
        document.addEventListener("menubutton", function(){navigator.notification.alert("menubutton")}, false);
        document.addEventListener("backbutton", function(){navigator.notification.alert("backbutton")}, false);
        document.addEventListener("pause", function(){navigator.notification.alert("pause")}, false);
        document.addEventListener("resume", function(){navigator.notification.alert("resume")}, false);
        setInterval("getPosition()","1000");
        // Add similar listeners for other events
    }
	
	 // Wait until device is ready and then init the app
    document.addEventListener('DOMContentLoaded', function () {
        if (Framework7.prototype.device.android) {
            Dom7('.view.navbar-through').removeClass('navbar-through').addClass('navbar-fixed');
            Dom7('.view .navbar').prependTo('.view .page');
        }
        initVue();
    }, false);
	function onBackKeyDown(e) {
	   e.preventDefault();
	   navigator.notification.alert('Back Button is Pressed!');
	}
	function callbackFunction() {
 	   var localStorage = window.localStorage;
 	   localStorage.setItem("Name", "John");
 	  navigator.notification.alert(localStorage.getItem("Name"));
 	}
    function cordovaDevice() {
    	navigator.notification.alert("Cordova version: " + device.cordova + "\n" +
    	      "Device model: " + device.model + "\n" +
    	      "Device platform: " + device.platform + "\n" +
    	      "Device UUID: " + device.uuid + "\n" +
    	      "Device version: " + device.version);
    }  	
   
})();