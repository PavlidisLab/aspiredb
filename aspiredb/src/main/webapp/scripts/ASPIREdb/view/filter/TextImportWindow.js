/*
 * The aspiredb project
 *
 * Copyright (c) 2013 University of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
Ext.require( [ 'Ext.window.*', 'Ext.layout.container.Border' ] );

/**
 * Show Text Area for entering a list of gene symbols to include in the filter.
 */
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
    * selectedProperty - e.g. GeneProperty() me.propertyFilterRef.populateMultiComboItemFromImportList - function to
    * parse GeneValueObjects and load into combo list or a grid component
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

      me.setLoading( true );

      if ( textList ) {

         QueryService.getVariantLocationValueObjects( this.property, textList, {
            callback : function(valueObjects) {

               var verifiedValues = me.processVerifiedValues( valueObjects, textList );

               me.propertyFilterRef.populateMultiComboItemFromImportList( verifiedValues );

               me.setLoading( false );

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
