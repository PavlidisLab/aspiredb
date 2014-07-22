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

Ext.require( [ 'ASPIREdb.store.UserGroupStore', 'ASPIREdb.TextDataDownloadWindow', 'Ext.grid.*', 'Ext.data.*', 'Ext.util.*', 'Ext.state.*', 'Ext.form.*' ] );

var rowEditing = Ext.create( 'Ext.grid.plugin.RowEditing', {
   // clicksToMoveEditor: 1,
   clicksToEdit : 2,
   autoCancel : false
} );

// TODO js documentation
Ext.define( 'ASPIREdb.view.UserGroupGrid', {
   extend : 'Ext.grid.Panel',
   alias : 'widget.userGroupGrid',
   emptyText : 'No user groups found',
   id : 'userGroupGrid',
   border : true,
   store : Ext.create( 'ASPIREdb.store.UserGroupStore' ),

   config : {
      // collection of all the PhenotypeSummaryValueObject loaded
      LoadedUserGroupNames : [],
      // collection of selected user group value objects
      selUserGroup : [],
      geneValueObjects : [],
   },

   dockedItems : [ {
      xtype : 'toolbar',
      itemId : 'userGroupGridToolbar',
      dock : 'top'
   } ],

   columns : [ {

      header : 'Name',
      dataIndex : 'groupName',
      flex : 1,
      editor : {
         // defaults to text field if no xtype is supplied
         allowBlank : false
      }
   }, {
      header : 'Description',
      dataIndex : 'groupDescription',
      flex : 1,
      editor : {
         // defaults to text field if no xtype is supplied
         allowBlank : true
      }
   }, {
      header : 'size',
      dataIndex : 'groupSize',
      flex : 1,

   } ],

   plugins : [ rowEditing ],
   listeners : {
      'selectionchange' : function(view, records) {
         this.down( '#removeUserGroup' ).setDisabled( !records.length );
      }
   },

   initComponent : function() {

      this.callParent();

      this.enableToolbar();

      this.on( 'select', this.userGroupSelectHandler, this );

      ASPIREdb.EVENT_BUS.on( 'group_member_added', this.geneAddedHandler, this );

      this.on( 'edit', function(editor, e) {
         var record = e.record;
         var me = this;
         /**
          * UserUserGroupService.findUserUserGroup( me.selUserGroup[0].data.userGroupName, { callback : function(gsvo) {
          * console.log( 'found user group name ' + gsvo.name + ' description' + gsvo.description ); gsvo.name =
          * record.data.userGroupName; gsvo.description = record.data.geneDescription; console.log( 'AFTER UPDATE -
          * found user group name ' + gsvo.name + ' description' + ' to string ' + gsvo.description + gsvo.id );
          *  // /////////////////////////////// /** UserUserGroupService.updateUserUserGroup( ug, { callback :
          * function() { console.log('testing the update user user group**********'); // me.getView().refresh();
          * ASPIREdb.EVENT_BUS.fireEvent( 'geneset_updated' ); }, errorHandler : function(er, exception) {
          * Ext.Msg.alert( "Update user user group Error", er + "\n" + exception.stack ); console.log( exception.stack ); } } );
          */

         /**
          * }, errorHandler : function(er, exception) { Ext.Msg.alert( "find user user group Error", er + "\n" +
          * exception.stack ); console.log( exception.stack ); } } );
          */

      } );

   },

   updateUserGroupGridHandler : function(userGroupName) {

      var panel = ASPIREdb.view.GeneManagerWindow.down( '#ASPIREdb_UserManagerpanel' );
      var geneGrid = panel.down( '#userGroupGrid' );

      // TODO : refresh grid when loaded
      this.store.add( userGroupName );
      this.getView().refresh( true );
      this.setLoading( false );
   },

   geneAddedHandler : function(gvo) {
      this.geneValueObjects.push( gvo );

   },

   userGroupSelectHandler : function(ref, record, index, eOpts) {

      var me = this;
      this.selUserGroup = this.getSelectionModel().getSelection();
      var userGroupName = this.selUserGroup[0].data.groupName;

     //find members of the selected user group
      UserManagerService.findGroupMemebers(userGroupName, {
         callback : function(gms) { 
            me.populateGeneGrid( gms );
         }
      });
     
      ASPIREdb.EVENT_BUS.fireEvent( 'userGroup_selected', this.selUserGroup );

   },

   // Populate gens in gene grid
   populateGeneGrid : function(gms) {

      var panel = ASPIREdb.view.UserManagerWindow.down( '#ASPIREdb_UserManagerpanel' );
      var grid = panel.down( '#groupMemeberGrid' );

      var data = [];
      for (var i = 0; i < gms.length; i++) {
         var gm= gms[i];
         var row = [ gm, '' ];
         data.push( row );
      }

      grid.store.loadData( data );
      grid.setLoading( false );
      grid.getView().refresh();

   },

   enableToolbar : function() {

      this.getDockedComponent( 'userGroupGridToolbar' ).removeAll();

      this.getDockedComponent( 'userGroupGridToolbar' ).add( {
         xtype : 'textfield',
         id : 'userGroupName',
         text : '',
         scope : this,
         allowBlank : true,
         emptyText : 'Type user group Name',

      } );

      this.getDockedComponent( 'userGroupGridToolbar' ).add( '-' );

      var ref = this;

      this.getDockedComponent( 'userGroupGridToolbar' ).add( {
         xtype : 'button',
         id : 'addUserGroup',
         text : '',
         tooltip : 'Add new user group',
         icon : 'scripts/ASPIREdb/resources/images/icons/group_add.png',
         // TODO: Need a better workaround
         handler : function() {

            var newUserGroupName = ref.down( '#userGroupName' ).getValue();
            var ref=this; 

            geneValueObjects = [];
            geneValueObjects.push( new GeneValueObject() );
/**
            UserManagerService.createUserGroup( newUserGroupName, {
               callback : function(status) {
                  if (status =="Success"){
                    // Ext.Msg.alert( 'Success','You have successfully created the group' );
                     
                     var panel = ASPIREdb.view.UserManagerWindow.down( '#ASPIREdb_UserManagerpanel' );
                     var userGroupGrid = panel.down( '#userGroupGrid' );
                     
                     // add gene set name to geneset grid
                     var data = [];
                     var row = [ newUserGroupName, '', 0 ];
                     data.push( row );
                     userGroupGrid.store.add( data );
                     userGroupGrid.getView().refresh( true );
                     userGroupGrid.setLoading( false );

                     var panel = ASPIREdb.view.UserManagerWindow.down( '#ASPIREdb_UserManagerpanel' );;
                     var userGroupMemeberGrid = panel.down( '#groupMemeberGrid' );
                     
                     userGroupMemeberGrid.store.removeAll( true );
                     ref.down( '#userGroupName' ).setValue( '' );
                   //  console.log( 'returned gene value object : ' + gvoId );
                     ASPIREdb.view.UserManagerWindow.fireEvent( 'new_user_group' );
                     
                     
                  }
               }
            } );*/
    
         }
      } );

      this.getDockedComponent( 'userGroupGridToolbar' ).add( {
         xtype : 'button',
         id : 'removeUserGroup',
         text : '',
         tooltip : 'Remove the selected user group',
         icon : 'scripts/ASPIREdb/resources/images/icons/group_delete.png',
         handler : function() {
            // Delete user group
            UserManagerService.deleteUserGroup( ref.selGeneSet[0].data.geneSetName, {
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
