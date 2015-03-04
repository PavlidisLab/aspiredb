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
Ext.require( [ 'ASPIREdb.view.report.VariantReportPanel' ] );

/**
 * Create Variant Report Window
 */
Ext.define( 'ASPIREdb.view.report.VariantReportWindow', {
   extend : 'Ext.Window',
   width : 950,
   height : 500,
   id : 'variantReportWindow',
   title : 'Variant Report',
   layout : 'fit',
   resizable : true,
   modal : true,
   tbar : [],

   statics : {
      getColumnDataFromStore : function(store, columnName) {
         var result = [];
         if ( store == null || store.data == null ) {
            console.log( "store is " + store );
            return result;
         }
         for (var i = 0; i < store.data.length; i++) {

            // extract values
            var row = store.data.getAt( i ).data;
            var val = row[columnName];

            if ( val == undefined ) {
               console.log( "Error " + val );
               continue;
            }

            // For CNV specific attributes like type and length, ignore SNVs
            if ( !this.isVariantTypeAndReportFieldCompatible( columnName, row["variantType"] ) ) {
               continue;
            }

            // show NA for empty values
            if ( val === "" ) {
               val = "NA";
            }

            result.push( val );
         }
         return result;
      },

      isVariantTypeAndReportFieldCompatible : function(columnName, variantType) {
         if ( variantType !== "CNV" ) {
            if ( columnName === "type" || columnName === "cnvLength" ) {
               return false;
            }
         }

         return true;
      },
   },

   initComponent : function() {

      var me = this;

      this.callParent();

      me.reportTypeStore = { 
         proxy : {
            type : 'dwr',
            dwrFunction : VariantService.suggestPropertiesForVariantTypeInProject,
            dwrParams : [ 'CNV', ASPIREdb.ActiveProjectSettings.getActiveProjectIds()[0] ],
            model : 'ASPIREdb.model.Property',
            reader : {
               type : 'json',
               root : 'data',
               totalProperty : 'count'
            }
         }
      };

      me.down( 'toolbar' ).add( {
         xtype : 'combo',
         itemId : 'reportCombo',
         fieldLabel : 'Data',
         store : me.reportTypeStore,
         editable : false,
         forceSelection : true,
         emptyText : 'Select report',
         displayField : 'displayName',
         valueField : 'name',
         width : 350,
         listeners : {
            select : me.reportComboSelectHandler,
            /*afterrender : function(combo) {
               var recordSelected = combo.getStore().getAt( 0 );
               combo.setValue( recordSelected.get( 'id' ) );
            }*/
         }
      }, {
         xtype : 'tbspacer'
      }, {
         xtype : 'checkbox',
         itemId : 'logTransformCheckbox',
         hidden : true,
         value : true,
         boxLabel : 'Log2 transform? ',
      }, {
         xtype : 'tbfill'
      }, {
         itemId : 'saveTxtButton',
         text : 'Save as TXT',
         tooltip : 'Save as TXT',
      // icon : 'scripts/ASPIREdb/resources/images/icons/disk.png',
      }, {
         xtype : 'button',
         itemId : 'savePngButton',
         text : 'Save as PNG',
         tooltip : 'Save as PNG',
      // icon : 'scripts/ASPIREdb/resources/images/icons/disk.png',
      } );

      this.down( "#logTransformCheckbox" ).on( 'change', me.reportComboSelectHandler );
      
   },

   /**
    * Hides report types that are not found in variants
    */
   filterReportCombo : function(variants) {
      var me = this;
      var reportCombo = me.down( '#reportCombo' );

      reportCombo.store.filter( [ {
         fn : function(record) {
            return variants.collect( record.get('name') ).length > 0;
         }
      } ] );

   },
   
   createAndShow : function( vvos ) {
      var me = this;

      me.variants = vvos;

      me.filterReportCombo( vvos );

      me.doLayout();
      me.show();
   },

   reportComboSelectHandler : function(cmp, newValue, oldValue, eOpts) {
      var me = this;
      
      var window = this.up( '#variantReportWindow' );

      var reportCombo = window.down( '#reportCombo' );

      var selReportType = reportCombo.value
      
      var reportPanel = window.down( '#variantReport' );
      if ( reportPanel != null ) {
         window.remove( reportPanel );
         reportPanel.destroy();
      }

      var logTransformCheckbox = window.down( "#logTransformCheckbox" );

      logTransformCheckbox.hide();

      var saveTextHandler = null;
      if ( selReportType === "cnvLength" ) {
         logTransformCheckbox.show();
         reportPanel = Ext.create( 'ASPIREdb.view.report.VariantReportPanel', {
            logTransform : logTransformCheckbox.value
         } );
      } else {
         window.down( '#savePngButton' ).show();
         reportPanel = Ext.create( 'ASPIREdb.view.report.VariantReportPanel' );
      }

      window.down( '#saveTxtButton' ).on( 'click', reportPanel.saveAsTXT );

      window.down( '#savePngButton' ).on(
         'click',
         function(e) {
            var me = this;
            Ext.MessageBox.confirm( 'Confirm Download', 'Would you like to download the chart as an image?', function(
               choice) {
               if ( choice == 'yes' ) {
                  reportPanel.saveAsPNG();
               }
            } );
         } );

      window.add( reportPanel );

      reportPanel.createReport( window.variants, selReportType );

      window.doLayout();
      
   },

} );