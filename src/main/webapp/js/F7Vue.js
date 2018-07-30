// First of all, we need to initialize/enable Framework7 Vue plugin:
Vue.use(Framework7Vue,Framework7);

// Init Vue App
new Vue({
    // App Root Element
    el: '#app',
    // Init Framework7. All Framework7 parameters should be passed in "framework7" property, e.g.:
    framework7: {
        // App Root Element, should be the same as the component root "el"
        root: '#app',
        animateNavBackIcon: true,
        swipePanel: 'left'
    },
    // App root data
    data: {
        // ....
    },
    // App root methods
    methods: {
        // ....
    }
})