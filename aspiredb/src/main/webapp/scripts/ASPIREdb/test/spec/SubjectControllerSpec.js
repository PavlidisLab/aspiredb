describe ("ExtJS Subject App Test Suite", function () {
var mainPanel = null;
var SubjectStore = null;
var storeLength = -1;
var controller = null;
  /* Setup method to be called before each Test case.*/
  beforeEach (function () {
        // Initializing the mainPanel.
       mainPanel = Ext.create ('QAApp.view.MainPanel');
       SubjectStore = Ext.StoreManager.lookup ('SubjectStore');
       //controller = Ext.create ('QAApp.controller.SubjectController');
       storeLength = SubjectStore.data.items.length;
  }); // before each

  /* Test if View is created Successfully.*/
  it ('Main View is loaded', function () {
        expect (mainPanel != null).toBeTruthy ();
  });

 /* Test if store is loaded successfully.*/ 
  it ('Store shouldn’t be null', function () {
        expect (SubjectStore != null).toBeTruthy();
   });

  /* Test controller is initialized successfully.*/ 
 // it ('Controller shouldn’t be null', function () {
//        expect (controller != null).toBeTruthy();
//   });

/* Test if Grid in MainPanel is loaded successfully.*/   
  it ('Grid should be loaded', function () {
        expect (Ext.getCmp ("SubjectGrid") != null).toBeTruthy ();
  });

 /* Test if Grid in MainPanel is loaded successfully.*/   
  it ('Store has items', function () {
  
       expect (SubjectStore.data.items.length).toBe (storeLength);
  });

 /* Test if new item is added to store.*/   
 it ('New item should be added to store', function () {
        var record = Ext.create ("QAApp.model.Subject");
        record.id = 1;
        record.Subject = 'Subjects 3';
        SubjectStore.add (record);
        expect (SubjectStore.data.items.length).toBe (storeLength + 1);
        SubjectStore.removeAt (storeLength);
 });

/* Item should be removed from store via controller.*/   
 it ('Item should be removed from store', function () {
        var record = Ext.create ("QAApp.model.Subject");
        record.id = 1;
        record.Subject = 'Subjects 3';
        SubjectStore.add (record);

        /* Removing item from controller API.*/   
        controller.deleteSubjectFromStore(record);
        SubjectStore.removeAt (storeLength);
        expect (SubjectStore.data.items.length).toBe (storeLength);
 });

});