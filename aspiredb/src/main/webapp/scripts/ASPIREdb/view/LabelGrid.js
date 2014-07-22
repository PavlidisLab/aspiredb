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
/**
 * author:  gaya 
 * Created to hold the labels for label manager
 */
Ext.require( [ 'ASPIREdb.store.LabelStore', 'ASPIREdb.TextDataDownloadWindow', 'Ext.grid.*', 'Ext.data.*',
              'Ext.util.*', 'Ext.state.*', 'Ext.form.*' ] );

var rowEditing = Ext.create( 'Ext.grid.plugin.RowEditing', {
   // clicksToMoveEditor: 1,
   clicksToEdit : 2,
   autoCancel : false
} );

// TODO js documentation
Ext.define( 'ASPIREdb.view.LabelGrid', {
   extend : 'Ext.grid.Panel',
   alias : 'widget.LabelGrid',
   emptyText : 'No label sets found',
   id : 'LabelGrid',
   border : true,
   store : Ext.create( 'ASPIREdb.store.LabelStore' ),

   config : {
      // collection of all the PhenotypeSummaryValueObject loaded
      LoadedlabelNames : [],
      // collection of selected label set value objects
      selLabel : [],
      labelValueObjects : [],
   },

   dockedItems : [ {
      xtype : 'toolbar',
      itemId : 'LabelGridToolbar',
      dock : 'top'
   } ],

   columns : [ {

      header : 'Id',
      dataIndex : 'labelId',
      flex : 1,
      editor : {
         // defaults to text field if no xtype is supplied
         allowBlank : false
      }
   }, {

      header : 'Name',
      dataIndex : 'labelName',
      flex : 1,
      editor : {
         // defaults to text field if no xtype is supplied
         allowBlank : false
      }
   }, {
      header : 'Label Colour',
      dataIndex : 'labelColour',
      flex : 1,
      editor : {
         // defaults to text field if no xtype is supplied
         allowBlank : true
      }
   }, /**
       * { header : '# Owners', dataIndex : 'ownerSize', flex : 1,
       *  }
       */
   ],

   plugins : [ rowEditing ],
   listeners : {
      'selectionchange' : function(view, records) {
         this.down( '#removeLabel' ).setDisabled( !records.length );
      }
   },

   initComponent : function() {

      this.callParent();

      this.on( 'select', this.LabelSelectHandler, this );

      ASPIREdb.EVENT_BUS.on( 'label_added', this.labelAddedHandler, this );

      this.on( 'edit', function(editor, e) {
         var record = e.record;
         var me = this;
         /**
          * LabelService.findLabel( me.selLabel[0].data.labelName, { callback : function(gsvo) { console.log( 'found
          * label set name ' + gsvo.name + ' description' + gsvo.description ); gsvo.name = record.data.labelName;
          * gsvo.description = record.data.labelDescription; console.log( 'AFTER UPDATE - found label set name ' +
          * gsvo.name + ' description' + ' to string ' + gsvo.description + gsvo.id );
          * 
          *  }, errorHandler : function(er, exception) { Ext.Msg.alert( "find user label set Error", er + "\n" +
          * exception.stack ); console.log( exception.stack ); } } );
          */

      } );

   },

   updateLabelGridHandler : function(labelName) {
      var panel = ASPIREdb.view.labelManagerWindow.down( '#ASPIREdb_Labelmanagerpanel' );
      var labelGrid = panel.down( '#LabelGrid' );
      // TODO : refresh grid when loaded

      this.store.add( labelName );
      this.getView().refresh( true );
      this.setLoading( false );
   },

   labelAddedHandler : function(gvo) {
      this.labelValueObjects.push( gvo );

   },

   LabelSelectHandler : function(ref, record, index, eOpts) {
      var me = this;
      this.selLabel = this.getSelectionModel().getSelection();
      var labelName = this.selLabel[0].data.labelName;
      // TODO: This DWR is returning the null objects even though java is returning the correct objects
      /**
       * LabelService.loadLabel( labelName, { callback : function(gvos) {
       * 
       * me.populatelabelGrid( gvos ); } } )
       */
      ASPIREdb.EVENT_BUS.fireEvent( 'Label_selected', this.selLabel );

   },

   // Populate gens in label grid
   populatelabelGrid : function(gvos) {

      var panel = ASPIREdb.view.labelManagerWindow.down( '#ASPIREdb_Labelmanagerpanel' );
      var grid = panel.down( '#labelGrid' );

      var data = [];
      for (var i = 0; i < gvos.length; i++) {
         var gvo = gvos[i];
         var row = [ gvo.symbol, '', gvo.name, '' ];
         data.push( row );
      }

      grid.store.loadData( data );
      grid.setLoading( false );
      grid.getView().refresh();

   },

   enableToolbar : function() {

      this.getDockedComponent( 'LabelGridToolbar' ).removeAll();

      this.getDockedComponent( 'LabelGridToolbar' ).add( {
         xtype : 'textfield',
         id : 'labelName',
         text : '',
         scope : this,
         allowBlank : true,
         emptyText : 'Type label Set Name',

      } );

      this.getDockedComponent( 'LabelGridToolbar' ).add( '-' );

      var ref = this;

      this.getDockedComponent( 'LabelGridToolbar' ).add( {
         xtype : 'button',
         id : 'addLabel',
         text : '',
         tooltip : 'Add new label set',
         icon : 'scripts/ASPIREdb/resources/images/icons/add.png',
         // TODO: Need a better workaround
         handler : function() {

            var newlabelName = ref.down( '#labelName' ).getValue();

            labelValueObjects = [];
            labelValueObjects.push( new labelValueObject() );

            /**
             * LabelService.saveLabel( newlabelName, labelValueObjects, { callback : function(gvoId) { var panel =
             * ASPIREdb.view.labelManagerWindow.down( '#ASPIREdb_Labelmanagerpanel' ); var LabelGrid = panel.down(
             * '#LabelGrid' ); // add label set name to Label grid var data = []; var row = [ newlabelName, '', 0 ];
             * data.push( row ); LabelGrid.store.add( data ); LabelGrid.getView().refresh( true ); LabelGrid.setLoading(
             * false );
             * 
             * var panel = ASPIREdb.view.labelManagerWindow.down( '#ASPIREdb_Labelmanagerpanel' ); var grid =
             * panel.down( '#labelGrid' ); grid.store.removeAll( true ); ref.down( '#labelName' ).setValue( '' );
             * console.log( 'returned label value object : ' + gvoId ); ASPIREdb.view.SaveUserLabelWindow.fireEvent(
             * 'new_Label_saved' ); } } );
             */

         }
      } );

      this.getDockedComponent( 'LabelGridToolbar' ).add( {
         xtype : 'button',
         id : 'removeLabel',
         text : '',
         tooltip : 'Remove the selected label set',
         icon : 'scripts/ASPIREdb/resources/images/icons/delete.png',
         handler : function() {
            // Delete label set
            /**
             * LabelService.deleteLabel( ref.selLabel[0].data.labelName, { callback : function() { var panel =
             * ASPIREdb.view.labelManagerWindow.down( '#ASPIREdb_Labelmanagerpanel' ); var LabelGrid = panel.down(
             * '#LabelGrid' ); var selection = LabelGrid.getView().getSelectionModel().getSelection()[0]; if ( selection ) {
             * LabelGrid.store.remove( selection ); }
             * 
             * console.log( 'selected Label :' + ref.selLabel[0].data.labelName + ' deleted' ); } } );
             */

         }
      } );

   }
} );
