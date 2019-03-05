(function () {
    'use strict';
    var webSocket,app,pos;
    var myapp_ip = "192.168.137.1/myapp/";
    var states= {
        name: '',
        msgs: [],
        attachments: [],
        sheetVisible: false,
        attachmentsVisible:true,
        images: [
   	          'http://'+myapp_ip+"libs/image/liuyizhi.jpg",
   	      		"http://"+myapp_ip+"libs/image/wangmeng.jpg"
   	        ],
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
         function(error){}, options
         );
    }
    function initSocket(usercode){
    	var new_url = "ws://"+myapp_ip+"websocket/chat/"+usercode;
    	if(webSocket!=null){
    		if(webSocket.url==new_url){
    			if(webSocket.readyState==webSocket.OPEN||webSocket.readyState==webSocket.CONNECTING){
    				webSocket = new WebSocket(new_url);
               	 	initwebSocket(webSocket);
    			}
    		}else{
    			webSocket.close(1000, "close");
    			webSocket = new WebSocket(new_url);
           	 	initwebSocket(webSocket);
    		}
    	}else{
       	 	webSocket = new WebSocket(new_url);
       	 	initwebSocket(webSocket);
    	}
         
    }
    function initwebSocket(webSocket){
    	webSocket.onmessage = function (data) 
        { 
           var message = eval("("+data.data+")");
           var type = message.name == states.name ? 'sent' : 'received';
           var name = type == 'send' ? states.name : message.name;
           states.msgs.push({name:name, text:message.text, type:type,avatar:message.avatar});
        };
        webSocket.onerror = function (event) {
        	console.log(event);
        };
        webSocket.onclose = function () {
       	let tryTime = 0;
       	 // 重试2次，每次之间间隔5秒
       	 if (tryTime < 2) {
       	 setTimeout(function () {
       	 webSocket = null;
       	tryTime++;
       	initSocket(states.name);
       	 }, 10000);
       	 } else {
       	 tryTime = 0;
       	}
       };
       var heartid = window.setInterval(function(){ //每隔5秒钟发送一次心跳，避免websocket连接因超时而自动断开
    	   if(webSocket)
    		   if(webSocket.readyState!=webSocket.CLOSING||webSocket.readyState!=webSocket.CLOSED){
        		   webSocket.send("heartbeat[myapp]");
    		   }else{
    			   window.clearInterval(heartid);
    		   }
       },5000);
    }
 // Init F7 Vue Plugin
    Framework7.use(Framework7Vue);

    // Init Page Components
    Vue.component('page-about', {
      template: '#page-about'
    });
    Vue.component('page-not-found', {
      template: '#page-not-found'
    });
    Vue.component('page-chat', {
        template: '#page-chat',
        data:function(){return states;},
        methods: {
        	sendMessage: function() {
   	 		    const self = this;
	   	        const text = self.$f7.messagebar.getValue().replace(/\n/g, '<br>').trim();
	   	       // Reset attachments
	   	        self.attachments = [];
	   	        // Hide sheet
	   	        self.sheetVisible = false;
	   	        // Clear area
	   	        self.$f7.messagebar.clear();
	   	        // Send message
	   	        if(text)webSocket.send(text);
            },
        	handleAttachment:function (e) {
		        const self = this;
		        const index = self.$$(e.target).parents('label.checkbox').index();
		        const image = self.images[index];
		        if (e.target.checked) {
		          // Add to attachments 
		          self.attachments.unshift(image);
		        } else {
		          // Remove from attachments
		          self.attachments.splice(self.attachments.indexOf(image), 1);
		        }
		      },
        }
    });

    // Init App
    app =  new Vue({
    	el: '#app',
    	  // Framework7 parameters here
    	data: function () {
    		return {
    			f7params:{
    		      root: '#app',// App root element
    		      theme: 'auto', // Automatic theme detection
    		      // App routes
    		      routes: [
    		        {
    		          path: '/about/',
    		          component: 'page-about'
    		        },
    		        {
    		          path: '/page-chat/',
    		          component: 'page-chat'
    		        },
    		        {
    		          path: '(.*)',
    		          component: 'page-not-found',
    		        },
    		      ],
    		    }, 
    		    chatdata: states
    	  	}
    	},
    	methods: {
    		  /**
             * Gets called when user name was entered and user enters chat
             */
            enterChat: function (event) {
            	states.name = localStorage.getItem("name");
                if (states.name == 'undefined'||states.name == null||states.name=='') {
                	this.$f7.dialog.alert('Please login');
                	if(event.preventDefault) //标准技术
                		 event.preventDefault();
                	if(event.returnValue) //兼容IE9之前的IE
                		 event.returnValue=false;
                }
                //states.msgs.length = 0;
              // this.$f7.views.main.router.load({url: '/page-chat/'});
                initSocket(states.name.trim());  
            },
            login:function(){
            	localStorage.setItem("name", this.chatdata.name);
            },
            testrouter:function(){
            	//this.$f7.views.main.router.load({“componentUrl: '/about/'});
            	this.$f7.views.main.router.navigate('/about/');
            	this.panel.close();
            },
            backward:function(){},
            forward:function(){}
            
            
        },
    	  mounted:function(){
    		  
    	  }
    });
})();