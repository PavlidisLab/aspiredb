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
Ext.require( [ 'ASPIREdb.view.report.BurdenAnalysisPerSubject', 'ASPIREdb.view.report.BurdenAnalysisPerSubjectLabel' ] );

/**
 * Create Variant Report Window
 */
Ext.define( 'ASPIREdb.view.report.BurdenAnalysisWindow', {
   extend : 'Ext.Window',
   width : 950,
   height : 500,
   id : 'burdenAnalysisWindow',
   title : 'Burden Analysis',
   layout : 'fit',
   resizable : true,
   tbar : [],

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

      me.charLabelStore = me.createCharacteristicStore();

      me.varLabelStore = me.createVariantLabelStore();

      // me.down( 'toolbar' ).add( {
      me.down( 'toolbar' ).add( {
         xtype : 'container',
         layout : 'vbox',
         // height : 40,
         // width : 200,
         items : [ {
            xtype : 'combo',
            itemId : 'subjectLabelCombo1',
            queryMode : 'local',
            fieldLabel : 'Group 1',
            autoSelect : 'true',
            store : me.subjectLabelStore,
            displayField : 'name',
            valueField : 'id',
            width : 300,
            listeners : {
               'change' : me.subjectLabelCombo1SelectHandler,
               afterrender : function(combo) {
                  var recordSelected = combo.getStore().getAt( 0 );
                  if ( recordSelected == null )
                     return;
                  combo.setValue( recordSelected.get( 'id' ) );
               }
            },
            hidden : false
         }, {
            xtype : 'combo',
            itemId : 'subjectLabelCombo2',
            queryMode : 'local',
            fieldLabel : 'Group 2',
            autoSelect : 'true',
            store : me.subjectLabelStore,
            displayField : 'name',
            valueField : 'id',
            width : 300,
            listeners : {
               'change' : me.subjectLabelCombo2SelectHandler,
            },
            hidden : true
         }, ]

      }, {
         xtype : 'tbspacer'
      }, {
         xtype : 'container',
         layout : 'vbox',
         items : [ {
            xtype : 'combo',
            itemId : 'reportCombo',
            fieldLabel : 'Report',
            store : me.reportTypeStore,
            displayField : 'name',
            valueField : 'id',
            queryMode : 'local',
            editable : false,
            forceSelection : true,
            width : 300,
            listeners : {
               'change' : me.reportComboSelectHandler,
            /*
             * afterrender : function(combo) { var recordSelected = combo.getStore().getAt( 0 ); combo.setValue(
             * recordSelected.get( 'id' ) ); }
             */
            },
            hidden : true
         }, {
            xtype : 'combo',
            itemId : 'charLabelCombo',
            queryMode : 'local',
            fieldLabel : 'Characteristic',
            autoSelect : 'true',
            store : me.charLabelStore,
            displayField : 'name',
            valueField : 'id',
            ueryMode : 'local',
            editable : false,
            forceSelection : true,
            width : 300,
            listeners : {
               'change' : me.charLabelComboSelectHandler,
            /*
             * afterrender : function(combo) { var recordSelected = combo.getStore().getAt( 0 ); if ( recordSelected ==
             * null ) return; combo.setValue( recordSelected.get( 'id' ) ); }
             */
            },
            hidden : true,
         }, , {
            xtype : 'combo',
            itemId : 'varLabelCombo',
            queryMode : 'local',
            fieldLabel : 'Label',
            autoSelect : 'true',
            store : me.varLabelStore,
            displayField : 'name',
            valueField : 'id',
            ueryMode : 'local',
            editable : false,
            forceSelection : true,
            width : 300,
            listeners : {
               'change' : me.varLabelComboSelectHandler,
            },
            hidden : true,
         }, ]

      }, {
         xtype : 'tbspacer'
      }, {
         xtype : 'tbfill'
      }, {
         itemId : 'runButton',
         text : 'Analyze',
         listeners : {
            'click' : me.analyze,
         }

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
         hidden : true
      // icon : 'scripts/ASPIREdb/resources/images/icons/disk.png',
      } );

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

         // TODO
         // [ 'cnvLength', 'CNV Length' ],

         // TODO gene summary tables, cnv specific
         // [ 'genesPerSubject', 'CNV summary per subject' ],

         [ 'genesPerSubjectLabel', 'Genes overlapped' ],

         // TODO
         // [ 'variantLabel', 'Variant Label' ],

         // TODO
         // [ 'characteristic', 'Characteristic' ],

         // TODO genes for SNVs??

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
            me.down( '#subjectLabelCombo1' ).store.reload();
            me.down( '#subjectLabelCombo2' ).store.reload();
         },
         errorHandler : function(message, exception) {
            Ext.Msg.alert( 'Error', message )
            console.log( message )
            console.log( dwr.util.toDescriptiveString( exception.stackTrace, 3 ) )
         }
      } );

      return subjectLabelStore;
   },

   createVariantLabelStore : function() {
      var me = this;
      var data = [];
      var store = Ext.create( 'Ext.data.ArrayStore', {
         data : data,
         fields : [ {
            name : 'id',
         }, {
            name : 'name',
         } ],
      } );

      data.push( [ 1, 'var label 1', true ] );
      data.push( [ 2, 'var label 2', false ] );

      store.reload();

      return store;
   },

   /**
    * Populates the labelCombo with Variant characteristics
    */
   createCharacteristicStore : function() {
      var me = this;
      var data = [];
      var store = Ext.create( 'Ext.data.ArrayStore', {
         data : data,
         fields : [ {
            name : 'id',
         }, {
            name : 'name',
         } ],
      } );

      data.push( [ 1, 'characteristic1', true ] );
      data.push( [ 2, 'characteristic2', false ] );

      store.reload();

      return store;
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
            if ( cnvOnlyData.indexOf( record.get( 'id' ) ) != -1 ) {
               return variantStore.collect( 'variantType' ).indexOf( 'CNV' ) != -1;
               ;
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

   subjectLabelCombo1SelectHandler : function(cmp, newValue, oldValue, eOpts) {
      var me = this;

      var window = me.up( '#burdenAnalysisWindow' );

      var combo = window.down( '#subjectLabelCombo2' );

      combo.show();
   },

   subjectLabelCombo2SelectHandler : function(cmp, newValue, oldValue, eOpts) {
      var me = this;

      var window = me.up( '#burdenAnalysisWindow' );

      var combo = window.down( '#reportCombo' );

      combo.show();
   },

   reportComboSelectHandler : function(cmp, newValue, oldValue, eOpts) {
      var me = this;

      var window = me.up( '#burdenAnalysisWindow' );

      var charCombo = window.down( '#charLabelCombo' );
      var varLabelCombo = window.down( '#varLabelCombo' );

      if ( me.value === "characteristic" ) {
         charCombo.show();
         varLabelCombo.hide();
      } else if ( me.value === "variantLabel" ) {
         charCombo.hide();
         varLabelCombo.show();
      } else {
         charCombo.hide();
         varLabelCombo.hide();
      }

   },

   charLabelComboSelectHandler : function(cmp, newValue, oldValue, eOpts) {
      var me = this;

      // var window = me.up( '#burdenAnalysisWindow' );
      //
      // var combo = window.down( '#reportCombo' );

   },

   varLabelComboSelectHandler : function(cmp, newValue, oldValue, eOpts) {
      var me = this;

      // var window = me.up( '#burdenAnalysisWindow' );
      //
      // var combo = window.down( '#reportCombo' );

   },

   analyze : function() {
      var me = this;

      var window = me.up( '#burdenAnalysisWindow' );

      var combo = window.down( '#reportCombo' );

      var selReportType = window.down( '#reportCombo' ).value
      var subjectLabel1 = window.down( '#subjectLabelCombo1' ).value
      var subjectLabel2 = window.down( '#subjectLabelCombo2' ).value
      var charLabel = window.down( '#charLabelCombo' ).value
      var varLabel = window.down( '#varLabelCombo' ).value

      // TODO
      console.log( 'selReportType=' + selReportType + "; subjectLabel1=" + subjectLabel1 + "; subjectLabel2="
         + subjectLabel2 + "; charLabel=" + charLabel + "; varLabel=" + varLabel );

      var reportPanel = window.down( '#variantReport' );
      if ( reportPanel != null ) {
         window.remove( reportPanel );
         reportPanel.destroy();
         reportPanel = null;
      }

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
      }

      if ( reportPanel == null ) {
         console.log( "reportPanel is null" );
         return;
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