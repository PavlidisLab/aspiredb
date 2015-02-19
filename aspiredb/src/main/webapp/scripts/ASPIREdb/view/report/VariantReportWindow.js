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
Ext.require( [ 'ASPIREdb.view.report.VariantReportPanel', 'ASPIREdb.view.report.BurdenAnalysisPerSubject',
              'ASPIREdb.view.report.BurdenAnalysisPerSubjectLabel' ] );

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
   tbar : [ {
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
   } ],

   statics : {
      getColumnDataFromStore : function(store, columnName) {
         var result = [];
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

      me.reportTypeStore = me.createReportTypeStore();

      me.subjectLabelStore = me.createSubjectLabelStore();

      me.down( 'toolbar' ).insert( 0, {
         xtype : 'combo',
         itemId : 'reportCombo',
         fieldLabel : 'Data',
         store : me.reportTypeStore,
         displayField : 'name',
         valueField : 'id',
         queryMode : 'local',
         editable : false,
         forceSelection : true,
         width : 350,
         listeners : {
            'change' : me.reportComboSelectHandler,
            afterrender : function(combo) {
               var recordSelected = combo.getStore().getAt( 0 );
               combo.setValue( recordSelected.get( 'id' ) );
            }
         }
      } );
      this.down( 'toolbar' ).insert( 1, {
         xtype : 'tbspacer'
      } );
      this.down( 'toolbar' ).insert( 2, {
         xtype : 'checkbox',
         itemId : 'logTransformCheckbox',
         hidden : true,
         value : true,
         boxLabel : 'Log2 transform? ',
      } );

      me.down( 'toolbar' ).insert( 3, {
         xtype : 'combo',
         itemId : 'subjectLabelCombo',
         queryMode : 'local',
         fieldLabel : 'Subject label',
         autoSelect : 'true',
         store : me.subjectLabelStore,
         displayField : 'name',
         valueField : 'id',
         listeners : {
            'change' : me.subjectLabelComboSelectHandler,
            afterrender : function(combo) {
               var recordSelected = combo.getStore().getAt( 0 );
               if ( recordSelected == null )
                  return;
               combo.setValue( recordSelected.get( 'id' ) );
            }
         },
         hidden : false
      } );

      this.down( 'toolbar' ).insert( 4, {
         xtype : 'tbfill'
      } );

      this.down( "#logTransformCheckbox" ).on( 'change', me.reportComboSelectHandler );

   },

   createReportTypeStore : function() {
      return Ext.create( 'Ext.data.ArrayStore', {
         fields : [ {
            name : 'id',
            type : 'string'
         }, {
            name : 'name',
            type : 'string'
         } ],
         // [ VariantValueObjectPropertyName : DisplayValue ]
         data : [
         // gene summary tables, cnv specific
         [ 'genesPerSubject', 'CNV summary per subject' ], [ 'genesPerSubjectLabel', 'CNV summary per subject label' ],

         // cnv specific
         [ 'type', 'CNV type' ], [ 'cnvLength', 'CNV length' ],

         // commonly used reports
         [ 'chromosome', 'Chromosome' ], [ 'patientId', 'Patient ID' ], [ 'variantType', 'Variant type' ],

         // characteristics
         [ 'Array Platform', 'Array platform' ], [ 'Array Report', 'Array report' ],
                 [ 'Characteristics', 'Characteristics' ], [ 'Inheritance', 'Inheritance' ],

         ],

         autoLoad : true,
         autoSync : true,
      } );
   },

   /**
    * Populates the labelCombo with Subject Labels
    */
   createSubjectLabelStore : function() {

      var me = this;
      var subjectLabelData = [];
      var subjectLabelStore = Ext.create( 'Ext.data.ArrayStore', {
         data : subjectLabelData,
         fields : [ {
            name : 'id',
         }, {
            name : 'name',
         } ],
      } );

      // subject labels
      LabelService.getSubjectLabels( {
         callback : function(labels) {
            for (i = 0; i < labels.length; i++) {
               subjectLabelData.push( [ labels[i].id, labels[i].name ] );
            }
            // console.log(Ext.JSON.encode(me.subjectLabelData));
            me.down( '#subjectLabelCombo' ).store.reload();
         },
         errorHandler : function(message, exception) {
            Ext.Msg.alert( 'Error', message )
            console.log( message )
            console.log( dwr.util.toDescriptiveString( exception.stackTrace, 3 ) )
         }
      } );

      return subjectLabelStore;
   },

   /**
    * Hides report types that are not found in variantStore
    */
   filterReportCombo : function(variantStore) {
      var me = this;

      var cnvOnlyData = [ 'genesPerSubject', 'genesPerSubjectLabel', 'type', 'cnvLength' ];

      var reportCombo = me.down( '#reportCombo' );

      reportCombo.store.filter( [ {
         fn : function(record) {
            if ( record.get( 'id' ) === "Array Platform" ) {
               return variantStore.collect( 'Array Platform' ).length > 0;
            } else if ( record.get( 'id' ) === "Array Report" ) {
               return variantStore.collect( 'Array Report' ).length > 0;
            } else if ( cnvOnlyData.indexOf( record.get( 'id' ) ) != -1 ) {
               return variantStore.collect( 'variantType' ).indexOf( 'CNV' ) != -1;
               ;
            } else if ( record.get( 'id' ) === "Inheritance" ) {
               return variantStore.collect( 'inheritance' ).length > 0;
            } else if ( record.get( 'id' ) === "Characteristics" ) {
               return variantStore.collect( 'Characteristics' ).length > 0;
            }
            return true;
         }
      } ] )

   },

   createAndShow : function(variantStore) {
      var me = this;

      me.variantStore = variantStore;

      me.filterReportCombo( variantStore );

      me.doLayout();
      me.show();
   },

   // TODO
   subjectLabelComboSelectHandler : function(cmp, newValue, oldValue, eOpts) {
      console.log( 'subjectLabelComboSelectHandler ' + newValue )
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
      if ( selReportType === "genesPerSubject" ) {
         window.down( '#savePngButton' ).hide();
         reportPanel = Ext.create( 'ASPIREdb.view.report.BurdenAnalysisPerSubject', {
            id : 'variantReport'
         } );
      } else if ( selReportType === "genesPerSubjectLabel" ) {
         window.down( '#savePngButton' ).hide();
         reportPanel = Ext.create( 'ASPIREdb.view.report.BurdenAnalysisPerSubjectLabel', {
            id : 'variantReport'
         } );
      } else if ( selReportType === "cnvLength" ) {
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

      reportPanel.createReport( window.variantStore, selReportType );

      window.doLayout();
   },

} );