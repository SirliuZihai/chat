// Init F7 Vue Plugin
Framework7.use(Framework7Vue);

// Init Page Components
Vue.component('page-about', {
  template: '#page-about'
});
Vue.component('page-form', {
  template: '#page-form',
  mounted:function(){
	  //plugins here
	  var fruits = ('Apple Apricot Avocado Banana Melon Orange Peach Pear Pineapple').split(' ');
	   var autocompleteSearchbar = this.$f7.autocomplete.create({
		  openIn: 'dropdown',
		  inputEl: '#name_auto input[autocomplete="name"]',
		 // dropdownPlaceholderText: 'Type "Apple"',
		  source: function (query, render) {
		    let results = [];
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
Vue.component('page-dynamic-routing', {
  template: '#page-dynamic-routing'
});
Vue.component('page-not-found', {
  template: '#page-not-found'
});


// Init App
app = new Vue({
  el: '#app',
  data: function () {
    return {
      // Framework7 parameters here
      f7params: {
        root: '#app', // App root element
        id: 'io.framework7.testapp', // App bundle ID
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
        	path: '/custom/',
        	componentUrl:'http://192.168.137.1/myapp'
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
	  //plugins here
  }
});