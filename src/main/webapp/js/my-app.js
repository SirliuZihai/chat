var app = new Framework7({
  root: '#app',
  name: 'My App',
  id: 'com.myapp.test',
  panel: {
    swipe: 'left',
  },
  routes: [
            {
                path: '/about/',
                componentUrl: 'page/about.html'
            }
          ],
  on: {
	    init: function () {
	      console.log('App initialized')
	    },
	    pageInit: function () {
	      console.log('Page initialized')
	      //app.dialog.alert('Hello World');
	    }
	  }

});
var $$ = Dom7;
//- Three groups
var ac4 = app.actions.create({
  buttons: [
    [
      {
        text: 'Share',
        label: true
      },
      {
        text: 'Mail',
      },
      {
        text: 'Messages',
      }
    ],
    [
      {
        text: 'Social share',
        label: true
      },
      {
        text: 'Facebook',
      },
      {
        text: 'Twitter',
      }
    ],
    [
      {
        text: 'Cancel',
        color: 'red'
      }
    ]
  ]
});

$$('.ac-4').on('click', function () {
    ac4.open();
});

var mainView = app.views.create('.view-main');

var fruits = ('Apple Apricot Avocado Banana Melon Orange Peach Pear Pineapple').split(' ');
var autocompleteDropdownSimple = app.autocomplete.create({
	  inputEl: '#autocomplete-dropdown',
	  openIn: 'dropdown',
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
var searchbar = app.searchbar.create({
	  el: '#searchbar-autocomplete',
	  customSearch: true,
	  on: {
	    search: function (query) {
	      console.log(query);
	    }
	  }
	});
	var autocompleteSearchbar = app.autocomplete.create({
	  openIn: 'dropdown',
	  inputEl: '#searchbar-autocomplete input[type="search"]',
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
	})
	var calendar = app.calendar.create({
		inputEl: '#calendar-input',
		dateFormat: 'DD, MM dd, yyyy',
		 //Disable everyting since December 2015
	    /*disabled: {
	        from: new Date(2015, 11, 1)
	    },*/
		events: [
		          new Date(2015, 9, 1),
		          new Date(2015, 9, 5),
		          {
		              from: new Date(2015, 9, 10),
		              to: new Date(2015, 9, 15)
		          },
		          {
		              from: new Date(2015, 9, 20),
		              to: new Date(2015, 9, 31)
		          }
		      ]
	});
	
	// With callback on close
	var notificationCallbackOnClose = app.notification.create({
	  icon: '<i class="icon demo-icon">7</i>',
	  title: 'Framework7',
	  titleRightText: 'now',
	  subtitle: 'Notification with close on click',
	  text: 'Click me to close',
	  closeOnClick: true,
	  on: {
	    close: function () {
	      app.dialog.alert('Notification closed');
	    },
	  },
	});
	
	$$('.open-callback-on-close').on('click', function () {
		  notificationCallbackOnClose.open();
	});
	//swip
	var moreActions = app.actions.create({
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
	});