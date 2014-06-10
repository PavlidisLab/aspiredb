describe ("ExtJS Subject App Test Suite", function () {
   
   it ("has loaded ExtJS 4",function() {
      expect( Ext ).toBeDefined();
      expect( Ext.getVersion() ).toBeTruthy();
      expect( Ext.getVersion().major ).toEqual( 4 );
   });

   
var mainPanel = null;

var controller = null;
 //  Setup method to be called before each Test case.
  beforeEach (function () {
        // Initializing the loginForm
       mainPanel = Ext.create ('AspiredbTest.view.LoginForm');

  }); // before each

  // Test if View is created Successfully.
  it ('Main View is loaded', function () {
        expect (mainPanel != null).toBeTruthy ();
  });

});
