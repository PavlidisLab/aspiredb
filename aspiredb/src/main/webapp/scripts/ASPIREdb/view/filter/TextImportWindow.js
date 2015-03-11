Ext.require( [ 'Ext.window.*', 'Ext.layout.container.Border' ] );

Ext.define( 'ASPIREdb.view.filter.TextImportWindow', {
   extend : 'Ext.Window',
   alias : 'widget.textimportwindow',
   singleton : true,
   title : 'Gene Symbols',
   closable : true,
   modal : true,
   closeAction : 'hide',
   width : 500,
   height : 400,
   layout : 'fit',
   bodyStyle : 'padding: 5px;',

   initComponent : function() {
      var me = this;
      this.items = [ {
         xtype : 'textarea',
         itemId : 'textImport',
         emptyText : 'Type one gene symbol per line',
      } ];

      this.buttons = [ {
         text : 'OK',
         handler : me.okHandler,
         scope : me

      }, {
         text : 'Clear',

         handler : function() {
            me.getComponent( "textImport" ).setValue( "" );
         }
      }, {
         text : 'Cancel',
         handler : function() {
            me.hide();
         }

      } ];

      this.callParent();

   },

   /**
    * propertyFilterRef must have these fields:
    * 
    * selectedProperty - e.g. GeneProperty()
    * me.propertyFilterRef.populateMultiComboItemFromImportList - function to parse GeneValueObjects and load into combo list or a grid component
    * 
    */
   setPropertyFilterAndShow : function(propertyFilterRef) {

      this.property = propertyFilterRef.selectedProperty;

      this.propertyFilterRef = propertyFilterRef;
      
      this.show();
   },

   okHandler : function() {

      var me = this;

      var text = this.getComponent( "textImport" ).getValue();

      var textList = text.match( /[^\r\n]+/g );

      if ( textList ) {

         QueryService.getVariantLocationValueObjects( this.property, textList, {
            callback : function(valueObjects) {

               var verifiedValues = me.processVerifiedValues( valueObjects, textList );

               me.propertyFilterRef.populateMultiComboItemFromImportList( verifiedValues );

            }
         } );
      }

   },

   processVerifiedValues : function(vos, textList) {

      var invalidFilterIndicies = [];

      var verifiedValues = [];

      for (var i = 0; i < vos.length; i++) {
         var vo = vos[i];

         if ( vo === null ) {
            invalidFilterIndicies.push( i );
         } else {
            verifiedValues.push( vo );
         }
      }

      if ( invalidFilterIndicies.length > 0 ) {
         var errorMessage = "The following text cannot be parsed:";

         for (var j = 0; j < invalidFilterIndicies.length; j++) {
            errorMessage = errorMessage + "<br />" + textList[invalidFilterIndicies[j]];
         }

         Ext.Msg.alert( 'Text not parsed', errorMessage );

      }

      this.close();

      return verifiedValues;

   }

} );
