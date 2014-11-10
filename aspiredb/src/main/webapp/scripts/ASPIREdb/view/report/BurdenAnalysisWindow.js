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
Ext.require( [ 'ASPIREdb.view.report.BurdenAnalysisPerSubject' ] );

/**
 * Create Burden Analysis Window
 */
Ext.define( 'ASPIREdb.view.report.BurdenAnalysisWindow', {
   extend : 'Ext.Window',
   width : 1090,
   height : 500,
   id : 'burdenAnalysisWindow',
   title : 'Burden Analysis',
   layout : 'fit',
   resizable : true,

   config : {
      variantIds : null,
   },

   constructor : function(cfg) {
      this.initConfig( cfg );
      this.callParent( arguments );
   },

   tbar : [ {
      itemId : 'saveTxtButton',
      // text : 'Save as TXT',
      tooltip : 'Save as TXT',
      icon : 'scripts/ASPIREdb/resources/images/icons/disk.png',
   } ],

   initComponent : function() {

      this.callParent();

      this.reportTypeStore = Ext.create( 'Ext.data.ArrayStore', {
         fields : [ {
            name : 'id',
            type : 'string'
         }, {
            name : 'name',
            type : 'string'
         } ],
         data : [ [ 'perSubject', 'Per Subject' ], ], // TODO per group, gene-centric and locus-centric
         autoLoad : true,
         autoSync : true,
      } );

      this.down( 'toolbar' ).insert( 0, {
         xtype : 'tbfill'
      } );
      this.down( 'toolbar' ).insert( 0, {
         xtype : 'combo',
         itemId : 'reportCombo',
         fieldLabel : 'Analyze',
         store : this.reportTypeStore,
         displayField : 'name',
         valueField : 'id',
         queryMode : 'local',
         editable : false,
         forceSelection : true,
         listeners : {
            'change' : this.reportComboSelectHandler,
            afterrender : function(combo) {
               var recordSelected = combo.getStore().getAt( 0 );
               combo.setValue( recordSelected.get( 'id' ) );
            }
         }
      } );

   },

   createAndShow : function(variantIds) {
      var me = this;

      me.variantIds = variantIds;

      me.doLayout();
      me.show();
   },

   reportComboSelectHandler : function(sel) {
      var reportType = sel.value;
      var window = this.up( '#burdenAnalysisWindow' );
      var reportPanel = null;

      if ( reportType !== 'perSubject' ) {
         Ext.Msg.alert( 'Error', 'Report ' + reportType + ' not implemented' );
         return;
      }

      window.remove( window.down( '#burdenAnalysisPerSubject' ) );

      reportPanel = Ext.create( 'ASPIREdb.view.report.BurdenAnalysisPerSubject' );
      window.add( reportPanel );
      window.doLayout();

      reportPanel.createReport( window.variantIds );

      window.down( '#saveTxtButton' ).on( 'click', function() {
         ASPIREdb.TextDataDownloadWindow.showBurdenAnalysisDownload( Ext.getStore( 'burdenAnalysisPerSubjectStore' ) );
      } );
   },

} );