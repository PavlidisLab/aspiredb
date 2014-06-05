Ext.Loader.setConfig( {
   enabled : true
} );

// Loading different components like controller, model, view..
Ext.application( {
  //controllers : [ 'SubjectController' ],
   models : [ 'Subject' ],
   stores : [ 'SubjectStore' ],
   views : [ 'MainPanel' ],
   autoCreateViewport : false,
   name : 'QAApp',

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