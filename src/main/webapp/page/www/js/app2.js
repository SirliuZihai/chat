
(function () {
    'use strict';
 // Init F7 Vue Plugin
    Framework7.use(Framework7Vue);

    // Init Page Components
    Vue.component('page-about', {
      template: '#page-about'
    });
    Vue.component('page-form', {
      template: '#page-form'
    });
    Vue.component('page-dynamic-routing', {
      template: '#page-dynamic-routing'
    });
    Vue.component('page-not-found', {
      template: '#page-not-found'
    });

    var fruits = ('Apple Apricot Avocado Banana Melon Orange Peach Pear Pineapple').split(' ');
    ;
    // Init App
    new Vue({
    	el: '#app',
    	  // Framework7 parameters here
    	data: function () {
    		return {
    			f7params:{
    		      root: '#app',// App root element
    		      name: 'Framework7', // App name
    		      theme: 'auto', // Automatic theme detection
    		      // App routes
    		      routes: [
    		        {
    		          path: '/about/',
    		          component: 'page-about'
    		        },
    		        {
    		          path: '/form/',
    		          component: 'page-form'
    		        },
    		        {
    		          path: '/dynamic-route/blog/:blogId/post/:postId/',
    		          component: 'page-dynamic-routing'
    		        },
    		        {
    		          path: '(.*)',
    		          component: 'page-not-found',
    		        },
    		      ],
    		    }
    	  	}
    	},
    	  mounted:function(){
    		  window.Framework7.autocomplete.create({
    			  openIn: 'dropdown',
    			  inputEl: '#name_auto',
    			  dropdownPlaceholderText: 'Type "Apple"',
    			  source: function (query, render) {
    			    var results = [];
    			    if (query.length === 0) {
    			      render(results);
    			      return;
    			    }
    			    // Find matched items
    			    for (var i = 0; i < fruits.length; i++) {
    			      if (fruits[i].toLowerCase().indexOf(query.toLowerCase()) >= 0) results.push(fruits[i]);
    			    }
    			    // Render items by passing array with result items
    			    render(results);
    			  }
    			});
    	  }
    });
})();