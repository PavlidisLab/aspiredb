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
Ext.require( [ 'ASPIREdb.view.report.BurdenAnalysisCharacteristic',
              'ASPIREdb.view.report.BurdenAnalysisPerSubjectLabel' ] );

/**
 * Create Burden Analysis window
 */
Ext.define( 'ASPIREdb.view.report.BurdenAnalysisWindow', {
   extend : 'Ext.Window',
   width : 950,
   height : 600,
   id : 'burdenAnalysisWindow',
   title : 'Burden Analysis',
   tools: [
           { 
            type: 'help',
            tooltip: 'This panel shows the list of subjects that meet currently configured query criteria (‘Filter‘ button). Selecting a row (by clicking on it) highlights variants belonging to this subject (Ideogram view) and shows associated phenotypes (Phenoype panel).'
           }
          ],      
   layout : 'fit',
   resizable : true,
   tbar : [],
   modal : true,

   initComponent : function() {

      var me = this;

      this.callParent();

      me.reportTypeStore = me.createReportTypeStore();

      me.subjectLabelStore = me.createSubjectLabelStore();

      me.charLabelStore = me.createCharacteristicStore();

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
            valueField : 'vo',
            width : 300,
            emptyText : 'Select subject label',
            listeners : {
               'change' : me.subjectLabelCombo1SelectHandler,
            /*
             * afterrender : function(combo) { var recordSelected = combo.getStore().getAt( 0 ); if ( recordSelected ==
             * null ) return; combo.setValue( recordSelected.get( 'vo' ) ); }
             */
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
            valueField : 'vo',
            emptyText : 'Select subject label',
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
            emptyText : 'Select report type',
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
            hidden : false
         }, {
            xtype : 'combo',
            itemId : 'charLabelCombo',
            queryMode : 'local',
            fieldLabel : 'Characteristic',
            autoSelect : 'true',
            store : me.charLabelStore,
            displayField : 'name',
            valueField : 'vo',
            emptyText : 'Select variant characteristic',
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
      }, {
         xtype : 'button',
         itemId : 'showChartButton',
         text : 'Show table',
         hidden : true,
      }, {
         xtype : 'button',
         flex : 1,
         text : 'Help',
         itemId : 'helpButton',
         handler : function() {
            window.open( 'http://aspiredb.chibi.ubc.ca/manual/burden-analysis/' );
         },
         scope : me,
      } );

      var showChartButton = me.down( '#showChartButton' );
      showChartButton.on( 'click', function() {
         var showChart = showChartButton.text === "Show chart";
         var reportPanel = me.down( "#burdenReport" );
         var chart = reportPanel.down( "#phenotypeEnrichmentChart" );
         var grid = reportPanel.down( '#burdenAnalysisPerPhenotypeGrid' );
         if ( showChart ) {
            showChartButton.setText( "Show table" );
            chart.setVisible( true );
            grid.setVisible( false );
         } else {
            showChartButton.setText( "Show chart" );
            chart.setVisible( false );
            grid.setVisible( true );
         }
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

         data : [ [ 'genesPerSubjectLabel', 'Length and genes overlapped' ], [ 'variantLabel', 'Variant label' ],
                 [ 'characteristic', 'Characteristic' ], [ 'phenotype', 'Phenotype' ], ],

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
            name : 'vo',
         }, {
            name : 'name',
         } ],
      } );

      // subject labels
      var projectId = ASPIREdb.ActiveProjectSettings.getActiveProjectIds()[0];
      LabelService.getSubjectLabelsByProjectId( projectId, {
         callback : function(labels) {
            for (i = 0; i < labels.length; i++) {
               subjectLabelData.push( [ labels[i], labels[i].name ] );
            }
            // console.log(Ext.JSON.encode(me.subjectLabelData));
            me.down( '#subjectLabelCombo1' ).store.reload();
            me.down( '#subjectLabelCombo2' ).store.reload();
         },
         errorHandler : function(message, exception) {
            console.log( message );
            console.log( dwr.util.toDescriptiveString( exception.stackTrace, 3 ) );
         },
      } );

      return subjectLabelStore;
   },

   /**
    * Adds characteristic to data if the characteristic contains nominal values (e.g. gain, loss, maternal, paternal,
    * etc.)
    */
   addNominalCharacteristic : function(characteristic, data, store) {

      var char = characteristic;
      var MAX_VALUES = 20; // maximum number of unique characteristic values we allow

      VariantService.suggestValues( characteristic, null, {
         callback : function(results) {
            if ( results.length === 0 ) {
               return;
            }
            // if ( isNaN(Number(result[0])) ) {
            // return true;
            // }
            if ( results.length > MAX_VALUES ) {
               console
                  .log( char.displayName + " is not nominal! " + results.length + " values : "
                     + results[0].displayValue + ", " + results[1].displayValue + ", " + results[2].displayValue
                     + " ... " );
               return;
            }

            if ( data === null ) {
               console.log( "data is " + data );
               return;
            }

            data.push( [ char, char.displayName ] );

            store.reload();
         },
         errorHandler : function(message, exception) {
            console.log( message );
            console.log( dwr.util.toDescriptiveString( exception.stackTrace, 3 ) );
         },
      } );

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
            name : 'vo',
         }, {
            name : 'name',
         } ],
      } );

      var pId = ASPIREdb.ActiveProjectSettings.getActiveProjectIds()[0];
      VariantService.suggestPropertiesForVariantTypeInProject( "CNV", pId, {
         callback : function(results) {

            for (var i = 0; i < results.length; i++) {
               var char = results[i];

               if ( !char.characteristic ) {
                  continue;
               }

               me.addNominalCharacteristic( char, data, store );

            }

            store.reload();
         },
         errorHandler : function(message, exception) {
            console.log( message );
            console.log( dwr.util.toDescriptiveString( exception.stackTrace, 3 ) );
         },
      } );

      return store;
   },

   createAndShow : function(variantStore) {
      var me = this;

      me.variantStore = variantStore;

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

      if ( me.value === "characteristic" ) {
         charCombo.show();
      } else if ( me.value === "variantLabel" ) {
         charCombo.hide();
      } else {
         charCombo.hide();
      }

   },

   charLabelComboSelectHandler : function(cmp, newValue, oldValue, eOpts) {
      var me = this;

      // var window = me.up( '#burdenAnalysisWindow' );
      //
      // var combo = window.down( '#reportCombo' );

   },

   analyze : function() {
      var me = this;

      var window = me.up( '#burdenAnalysisWindow' );

      var combo = window.down( '#reportCombo' );

      var selReportType = window.down( '#reportCombo' ).value;
      var subjectLabel1 = window.down( '#subjectLabelCombo1' ).value;
      var subjectLabel2 = window.down( '#subjectLabelCombo2' ).value;
      var characteristic = window.down( '#charLabelCombo' ).value;

      var reportPanel = window.down( '#burdenReport' );
      if ( reportPanel !== null ) {
         window.remove( reportPanel );
         reportPanel.destroy();
         reportPanel = null;
      }

      // reset state
      window.down( '#savePngButton' ).hide();
      window.down( '#showChartButton' ).setText( "Show table" );
      window.down( '#showChartButton' ).hide();

      var saveTextHandler = null;
      if ( selReportType === "characteristic" ) {
         reportPanel = Ext.create( 'ASPIREdb.view.report.BurdenAnalysisCharacteristic', {
            id : 'burdenReport',
            label1 : subjectLabel1,
            label2 : subjectLabel2,
            characteristic : characteristic
         } );
      } else if ( selReportType === "variantLabel" ) {
         reportPanel = Ext.create( 'ASPIREdb.view.report.BurdenAnalysisVariantLabel', {
            id : 'burdenReport',
            label1 : subjectLabel1,
            label2 : subjectLabel2
         } );
      } else if ( selReportType === "genesPerSubjectLabel" ) {
         reportPanel = Ext.create( 'ASPIREdb.view.report.BurdenAnalysisPerSubjectLabel', {
            id : 'burdenReport',
            label1 : subjectLabel1,
            label2 : subjectLabel2,
         } );
      } else if ( selReportType === "phenotype" ) {
         window.down( '#showChartButton' ).show();
         reportPanel = Ext.create( 'ASPIREdb.view.report.BurdenAnalysisPerPhenotype', {
            id : 'burdenReport',
            label1 : subjectLabel1,
            label2 : subjectLabel2,
         } );

      }

      if ( reportPanel === null ) {
         console.log( "burdenReport is null" );
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