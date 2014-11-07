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

Ext.require( [ 'ASPIREdb.store.GeneSetStore', 'ASPIREdb.TextDataDownloadWindow', 'Ext.grid.*', 'Ext.data.*',
              'Ext.util.*', 'Ext.state.*', 'Ext.form.*' ] );

var rowEditing = Ext.create( 'Ext.grid.plugin.RowEditing', {
   // clicksToMoveEditor: 1,
   clicksToEdit : 2,
   autoCancel : false
} );

// TODO js documentation
Ext.define( 'ASPIREdb.view.GeneSetGrid', {
   extend : 'Ext.grid.Panel',
   alias : 'widget.geneSetGrid',
   emptyText : 'No gene sets found',
   id : 'geneSetGrid',
   border : true,
   store : Ext.create( 'ASPIREdb.store.GeneSetStore' ),

   config : {
      // collection of all the PhenotypeSummaryValueObject loaded
      LoadedGeneSetNames : [],
      // collection of selected gene set value objects
      selGeneSet : [],
      geneValueObjects : [],
   },

   dockedItems : [ {
      xtype : 'toolbar',
      itemId : 'geneSetGridToolbar',
      dock : 'top'
   } ],

   columns : [ {

      header : 'Name',
      dataIndex : 'geneSetName',
      flex : 1,
      editor : {
         // defaults to text field if no xtype is supplied
         allowBlank : false
      }
   }, {
      header : 'Description',
      dataIndex : 'geneDescription',
      flex : 1,
      editor : {
         // defaults to text field if no xtype is supplied
         allowBlank : true
      }
   }, {
      header : 'size',
      dataIndex : 'geneSetSize',
      flex : 1,

   } ],

   plugins : [ rowEditing ],
   listeners : {
      'selectionchange' : function(view, records) {
         this.down( '#removeGeneset' ).setDisabled( !records.length );
      }
   },

   initComponent : function() {

      this.callParent();

      this.on( 'select', this.geneSetSelectHandler, this );

      ASPIREdb.EVENT_BUS.on( 'gene_added', this.geneAddedHandler, this );

      this.on( 'edit', function(editor, e) {
         var record = e.record;
         var me = this;
         UserGeneSetService.findUserGeneSet( me.selGeneSet[0].data.geneSetName, {
            callback : function(gsvo) {
               console.log( 'found gene set name ' + gsvo.name + '  description' + gsvo.description );
               gsvo.name = record.data.geneSetName;
               gsvo.description = record.data.geneDescription;
               console.log( 'AFTER UPDATE - found gene set name ' + gsvo.name + '  description' + ' to string '
                  + gsvo.description + gsvo.id );

               // ///////////////////////////////
               /**
                * UserGeneSetService.updateUserGeneSet( gvo, { callback : function() { console.log('testing the update
                * user gene set**********'); // me.getView().refresh(); ASPIREdb.EVENT_BUS.fireEvent( 'geneset_updated' );
                *  }, errorHandler : function(er, exception) { Ext.Msg.alert( "Update user gene set Error", er + "\n" +
                * exception.stack ); console.log( exception.stack ); } } );
                */

            },
            errorHandler : function(er, exception) {
               Ext.Msg.alert( "Find user gene set error", er + "\n" + exception.stack );
               console.log( exception.stack );
            }
         } );

      } );

   },

   updateGeneSetGridHandler : function(geneSetName) {
      var panel = ASPIREdb.view.GeneManagerWindow.down( '#ASPIREdb_genemanagerpanel' );
      var geneGrid = panel.down( '#geneSetGrid' );
      // TODO : refresh grid when loaded

      this.store.add( geneSetName );
      this.getView().refresh( true );
      this.setLoading( false );
   },

   geneAddedHandler : function(gvo) {
      this.geneValueObjects.push( gvo );

   },

   geneSetSelectHandler : function(ref, record, index, eOpts) {
      var me = this;
      this.selGeneSet = this.getSelectionModel().getSelection();
      var geneSetName = this.selGeneSet[0].data.geneSetName;
      // TODO: This DWR is returning the null objects even though java is returning the correct objects
      UserGeneSetService.loadUserGeneSet( geneSetName, {
         callback : function(gvos) {

            me.populateGeneGrid( gvos );
         }
      } );
      ASPIREdb.EVENT_BUS.fireEvent( 'geneSet_selected', this.selGeneSet );

   },

   // Populate gens in gene grid
   populateGeneGrid : function(gvos) {

      var panel = ASPIREdb.view.GeneManagerWindow.down( '#ASPIREdb_genemanagerpanel' );
      var grid = panel.down( '#geneGrid' );

      var data = [];
      for (var i = 0; i < gvos.length; i++) {
         var gvo = gvos[i];
         var row = []
         // find the position of the fields we want to use
         var geneStore = Ext.getStore('geneStore');
         var fieldNames = [];
         for ( var j = 0; j < geneStore.fields.length; j++) {
            fieldNames.push(geneStore.fields[j].name);
            row[j] = '';
         }
         row[fieldNames.indexOf('symbol')] = gvo.symbol;
         row[fieldNames.indexOf('name')] = gvo.name;
         
//         var row = [ '', '', gvo.symbol, '', gvo.name, '' ];
         data.push( row );
      }

      grid.store.loadData( data );
      grid.setLoading( false );
      grid.getView().refresh();

   },

   enableToolbar : function() {

      this.getDockedComponent( 'geneSetGridToolbar' ).removeAll();

      this.getDockedComponent( 'geneSetGridToolbar' ).add( {
         xtype : 'textfield',
         id : 'geneSetName',
         text : '',
         scope : this,
         allowBlank : true,
         emptyText : 'Type gene Set Name',

      } );

      this.getDockedComponent( 'geneSetGridToolbar' ).add( '-' );

      var ref = this;

      this.getDockedComponent( 'geneSetGridToolbar' ).add( {
         xtype : 'button',
         id : 'addGeneset',
         text : '',
         tooltip : 'Add new gene set',
         icon : 'scripts/ASPIREdb/resources/images/icons/add.png',
         // TODO: Need a better workaround
         handler : function() {

            var newGeneSetName = ref.down( '#geneSetName' ).getValue();

            geneValueObjects = [];
            geneValueObjects.push( new GeneValueObject() );
            UserGeneSetService.saveUserGeneSet( newGeneSetName, geneValueObjects, {
               callback : function(gvoId) {
                  var panel = ASPIREdb.view.GeneManagerWindow.down( '#ASPIREdb_genemanagerpanel' );
                  var geneSetGrid = panel.down( '#geneSetGrid' );
                  // add gene set name to geneset grid
                  var data = [];
                  var row = [ newGeneSetName, '', 0 ];
                  data.push( row );
                  geneSetGrid.store.add( data );
                  geneSetGrid.getView().refresh( true );
                  geneSetGrid.setLoading( false );

                  var panel = ASPIREdb.view.GeneManagerWindow.down( '#ASPIREdb_genemanagerpanel' );
                  var grid = panel.down( '#geneGrid' );
                  grid.store.removeAll( true );
                  ref.down( '#geneSetName' ).setValue( '' );
                  console.log( 'returned gene value object : ' + gvoId );
                  ASPIREdb.view.SaveUserGeneSetWindow.fireEvent( 'new_geneSet_saved' );
               }
            } );

         }
      } );

      this.getDockedComponent( 'geneSetGridToolbar' ).add( {
         xtype : 'button',
         id : 'removeGeneset',
         text : '',
         tooltip : 'Remove the selected gene set',
         icon : 'scripts/ASPIREdb/resources/images/icons/delete.png',
         handler : function() {
            // Delete gene set
            UserGeneSetService.deleteUserGeneSet( ref.selGeneSet[0].data.geneSetName, {
               callback : function() {
                  var panel = ASPIREdb.view.GeneManagerWindow.down( '#ASPIREdb_genemanagerpanel' );
                  var geneSetGrid = panel.down( '#geneSetGrid' );
                  var selection = geneSetGrid.getView().getSelectionModel().getSelection()[0];
                  if ( selection ) {
                     geneSetGrid.store.remove( selection );
                  }

                  console.log( 'selected geneset :' + ref.selGeneSet[0].data.geneSetName + ' deleted' );
               }
            } );

         }
      } );

   }
} );
