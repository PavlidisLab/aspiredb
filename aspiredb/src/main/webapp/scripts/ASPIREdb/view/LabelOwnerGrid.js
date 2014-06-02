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

Ext.require( [ 'Ext.grid.*',  'ASPIREdb.TextDataDownloadWindow', 'Ext.data.*', 'Ext.util.*',
              'Ext.state.*', 'Ext.form.*', 'ASPIREdb.model.PropertyValue' ,'ASPIREdb.store.SubjectStore', /**'ASPIREdb.model.subjectProperty', 'ASPIREdb.SubjectSuggestionStore'*/] );

var rowEditing = Ext.create( 'Ext.grid.plugin.RowEditing', {
   // clicksToMoveEditor: 1,
   clicksToEdit : 2,
   autoCancel : false
} );

/**
 * Create LabelOwner Grid
 */
Ext.define( 'ASPIREdb.view.LabelOwnerGrid', {
   extend : 'Ext.grid.Panel',
   alias : 'widget.LabelOwnerGrid',
   emptyText : 'No LabelOwners found',
   id : 'LabelOwnerGrid',
   border : true,
   //store : Ext.create( 'ASPIREdb.store.LabelOwnerStore' ),

   config : {
       // collection of selected LabelOwner value objects
      selectedLabelOwner : [],
      gvos : [],
      selectedLabelOwnerSet : [],
      suggestionContext : null,

   },

   dockedItems : [ {
      xtype : 'toolbar',
      itemId : 'LabelOwnerGridToolbar',
      dock : 'top'
   } ],

   columns : [ {
      header : 'Subject Id',
      dataIndex : 'id',
      flex : 1,
      editor : {
         // defaults to textfield if no xtype is supplied
         allowBlank : false
      }
   }, {
      header : 'Patient Id',
      dataIndex : 'patientId',
      flex : 1,
      editor : {
         // defaults to textfield if no xtype is supplied
         allowBlank : true
      }
   } ],

   plugins : [ rowEditing ],
   listeners : {
      'selectionchange' : function(view, records) {
         this.down( '#removeSubject' ).setDisabled( !records.length );
         this.selectedLabelOwner = this.getSelectionModel().getSelection();

      }
   },

   initComponent : function() {
      this.callParent();
      var me = this;
      me.enableToolbar();

      ASPIREdb.EVENT_BUS.on( 'LabelSubejct_selected', this.LabelOwnerSetSelectHandler, this );

   },

   /**
    * Store the LabelOwner value object when selected
    * 
    * @param LabelOwnerSetValueObject
    *           selLabelOwnerSet
    */
   LabelOwnerSetSelectHandler : function(selLabelOwnerSet) {
      this.selectedLabelOwnerSet = selLabelOwnerSet;
   },

   /**
    * Enable the tool bar in LabelOwner grid
    * 
    */
   enableToolbar : function() {

      this.getDockedComponent( 'LabelOwnerGridToolbar' ).removeAll();

      this.getDockedComponent( 'LabelOwnerGridToolbar' ).add( {
         xtype : 'combo',
         id : 'LabelOwnerName',
         emptyText : 'Type Subject',
         width : 200,
         displayField : 'displayName',
         triggerAction : 'query',
         // minChars : 0,
         matchFieldWidth : false,
         hideTrigger : true,
         triggerAction : 'query',
         autoSelect : false,
         enableKeyEvents : true,
       /**  store : Ext.create( 'ASPIREdb.subjectSuggestionStore', {
            remoteFunction : VariantService.suggestSubejctValues
         } ),*/
         listConfig : {
            loadingText : 'Searching...',
            emptyText : 'No results found.',

         },
         listeners : {
            select : {
               fn : function(obj, records) {
                  // ASPIREdb.EVENT_BUS.fireEvent('query_update');

               },
               scope : this,
            }
         },

      } );

      this.getDockedComponent( 'LabelOwnerGridToolbar' ).add( '-' );

      var ref = this;

      this.getDockedComponent( 'LabelOwnerGridToolbar' ).add( {
         xtype : 'button',
         id : 'addLabelOwner',
         text : '',
         tooltip : 'Add LabelOwners to selected LabelOwner set',
         icon : 'scripts/ASPIREdb/resources/images/icons/add.png',
         handler : function() {

            // TODO: have to populate human taxon LabelOwner list auto complete features
            var LabelOwnername = ref.down( '#LabelOwnerName' ).getValue();
            console.log( 'added LabelOwners name  : ' + LabelOwnername );
            var LabelOwnerSetName = ref.selectedLabelOwnerSet[0].data.LabelOwnerSetName;
            var panel = ASPIREdb.view.LabelOwnerManagerWindow.down( '#ASPIREdb_LabelOwnermanagerpanel' );
            var grid = panel.down( '#LabelOwnerGrid' );

            /**   SubjectService.isLabelOwnerInLabelOwnerSet( LabelOwnerSetName, LabelOwnername, {
               callback : function(gvoSta) {
                  if ( gvoSta ) {
                     Ext.Msg.alert( 'Gen Set', 'LabelOwner already exist in LabelOwner set' );
                     grid.down( '#LabelOwnerName' ).setValue( '' );
                  } else if ( ref.selectedLabelOwnerSet[0] != null ) {
                     
                     SubjectService.addsubjects( LabelOwnerSetName, LabelOwnername, {
                        callback : function(gvo) {

                           var data = [];
                           var row = [ LabelOwnername, gvo.LabelOwnerBioType, gvo.name, '' ];
                           data.push( row );

                           // TODO : refresh grid when loaded
                           grid.store.add( data );
                           grid.getView().refresh( true );
                           grid.setLoading( false );
                           grid.down( '#LabelOwnerName' ).setValue( '' );

                           ASPIREdb.EVENT_BUS.fireEvent( 'labelSubject_added', data );
                           
                           // update the LabelOwner set grid size
                      /**     var panel = ASPIREdb.view.LabelOwnerManagerWindow.down( '#ASPIREdb_LabelOwnermanagerpanel' );
                           var LabelOwnerSetGrid = panel.down( '#LabelOwnerSetGrid' );

                           var selection = LabelOwnerSetGrid.getView().getSelectionModel().getSelection()[0];
                           if ( selection ) {
                              var oldSize = selection.data.LabelOwnerSetSize;
                              selection.set( 'LabelSize', parseInt( oldSize ) + 1 );
                           }*/

                     /**   },
                        errorHandler : function(er, exception) {
                           Ext.Msg.alert( "Subject Grid Error", er + "\n" + exception.stack );
                           console.log( exception.stack );
                        }
                     } );
                  } else
                     Ext.Msg.alert( 'Error', 'select the Label Name to add subject ' );

               }
            } );*/

         }
      } );

      this.getDockedComponent( 'LabelOwnerGridToolbar' ).add( {
         xtype : 'button',
         id : 'removeLabelOwner',
         text : '',
         tooltip : 'Remove the selected subject',
         icon : 'scripts/ASPIREdb/resources/images/icons/delete.png',
         handler : function() {
            var LabelOwnername = ref.selectedLabelOwner[0].data.name;
            
        /**    SubjectService.deleteLabelSubject( ref.selectedLabelOwnerSet[0].data.LabelOwnerSetName, LabelOwnername, {
               callback : function() {

                  var panel = ASPIREdb.view.LabelOwnerManagerWindow.down( '#ASPIREdb_Labelmanagerpanel' );
                  var LabelOwnerGrid = panel.down( '#LabelOwnerGrid' );

                  var selection = LabelOwnerGrid.getView().getSelectionModel().getSelection()[0];
                  if ( selection ) {
                     LabelOwnerGrid.store.remove( selection );
                  }

                  // resize the LabelOwner size in LabelOwner set grid
                  // update the LabelOwner set grid size
               /**   var panel = ASPIREdb.view.LabelOwnerManagerWindow.down( '#ASPIREdb_Labelmanagerpanel' );
                  var LabelGrid = panel.down( '#LabelGrid' );

                  var selection = LabelGrid.getView().getSelectionModel().getSelection()[0];
                  if ( selection ) {
                     var oldSize = selection.data.LabelSize;
                     selection.set( 'LabelOwnerSetSize', parseInt( oldSize ) - 1 );
                  }
                  */
                  
        /**       }

            } );*/
         }
      } );

   }
} );
