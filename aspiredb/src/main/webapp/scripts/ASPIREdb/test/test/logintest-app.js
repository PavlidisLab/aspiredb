Ext.Loader.setConfig( {
   enabled : true
} );

// Loading different components like controller, model, view..
Ext.application( {

   views : [ 'LoginForm' ],
   autoCreateViewport : false,
   name : 'AspiredbTest',
   appFolder: 'scripts/ASPIREdb',

   // using the Launch method of Application object to execute the Jasmine
   // Test Cases
   launch : function() {
      var jasmineEnv = jasmine.getEnv();
      jasmineEnv.updateInterval = 1000;
      var htmlReporter = new jasmine.HtmlReporter();
      jasmineEnv.addReporter( htmlReporter );
      jasmineEnv.execute();
   }
} );

