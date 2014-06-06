Ext.Loader.setConfig( {
   enabled : true
} );

// Loading different components like controller, model, view..
Ext.application( {
  //controllers : [ 'SubjectController' ],
   models : [ 'Subject' ],
   stores : [ 'SubjectStore' ],
   views : [ 'ASPIREdb.MainPanel' ],
   autoCreateViewport : false,
   name : 'QAApp',
   appFolder: 'scripts',

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

