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

Ext.require( [ 'Ext.grid.*', 'ASPIREdb.store.GroupMemberStore', 'ASPIREdb.TextDataDownloadWindow', 'Ext.data.*',
              'Ext.util.*', 'Ext.state.*', 'Ext.form.*', 'ASPIREdb.model.PropertyValue' ] );

var rowEditing = Ext.create( 'Ext.grid.plugin.RowEditing', {
   // clicksToMoveEditor: 1,
   clicksToEdit : 2,
   autoCancel : false
} );

/**
 * Create user group members as a grid
 */
Ext.define( 'ASPIREdb.view.GroupMemeberGrid', {
   extend : 'Ext.grid.Panel',
   alias : 'widget.groupMemeberGrid',
   emptyText : 'No groupMembers found',
   id : 'groupMemeberGrid',
   border : true,
   store : Ext.create( 'ASPIREdb.store.GroupMemberStore' ),

   config : {
      // collection of all the PhenotypeSummaryValueObject loaded
      LoadedUserGroupNames : [],
      // collection of selected groupMember value objects
      selectedUser : [],
      gvos : [],
      selectedGroup : [],
      suggestionContext : null,

   },

   dockedItems : [ {
      xtype : 'toolbar',
      itemId : 'groupMemeberGridToolbar',
      dock : 'top'
   } ],

   columns : [ {
      header : 'Name',
      dataIndex : 'memberName',
      flex : 1,
      editor : {
         // defaults to textfield if no xtype is supplied
         allowBlank : false
      }
   }, ],

   initComponent : function() {
      this.callParent();
      var me = this;
      me.enableToolbar();

      this.on( 'select', this.userSelectHandler, this );

      ASPIREdb.EVENT_BUS.on( 'userGroup_selected', this.groupMemberSelectHandler, this );

   },

   /**
    * Store the groupMember value object when selected
    * 
    * @param GeneSetValueObject
    *           selGeneSet
    */
   groupMemberSelectHandler : function(selUserGroup) {
      this.selectedGroup = selUserGroup;
   },

   userSelectHandler : function(ref, record, index, eOpts) {
      this.selectedUser = record;
   },

   /**
    * Enable the tool bar in Gene grid
    * 
    */
   enableToolbar : function() {

      this.getDockedComponent( 'groupMemeberGridToolbar' ).removeAll();

      this.getDockedComponent( 'groupMemeberGridToolbar' ).add( {
         xtype : 'combo',
         id : 'groupMemberName',
         emptyText : 'Type username',
         width : 200,
         displayField : 'displayName',
         triggerAction : 'query',
         minChars : 0,
         matchFieldWidth : false,
         hideTrigger : true,
         triggerAction : 'query',
         autoSelect : true,
         forceSelection : true,
         enableKeyEvents : false,
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

      this.getDockedComponent( 'groupMemeberGridToolbar' ).add( '-' );

      var ref = this;

      this.getDockedComponent( 'groupMemeberGridToolbar' ).add( {
         xtype : 'button',
         id : 'addGroupMemeber',
         text : '',
         tooltip : 'Add users to group',         
         icon : 'scripts/ASPIREdb/resources/images/icons/user_add.png',
         handler : function() {

            if ( ref.selectedGroup[0] == null ) {
               Ext.Msg.alert( 'Error', 'Select a user group' );
               return;
            }

            var groupMember = ref.down( '#groupMemberName' ).lastQuery;
            if ( groupMember == null || groupMember.trim().length == 0 ) {
               Ext.Msg.alert( 'Error', 'Enter a valid user name' );
               return;
            }

            var groupName = ref.selectedGroup[0].data.groupName;

            UserManagerService.addUserToGroup( groupName, groupMember, {
               callback : function(status) {
                  if ( status == "Success" ) {
                     var panel = ASPIREdb.view.UserManagerWindow.down( '#ASPIREdb_UserManagerpanel' );
                     var groupMemberGrid = panel.down( '#groupMemeberGrid' );

                     var selection = ref.down( '#groupMemberName' ).lastQuery;
                     if ( selection ) {
                        var data = [];
                        var row = [ selection ];
                        data.push( row );
                        groupMemberGrid.store.add( data );
                     }

                  } else {
                     Ext.Msg.alert( "Server Reply", status );
                  }

               }
            } );

         }
      } );

      this.getDockedComponent( 'groupMemeberGridToolbar' ).add( {
         xtype : 'button',
         id : 'removeGroupMemeber',
         text : '',
         tooltip : 'Remove the selected user',         
         icon : 'scripts/ASPIREdb/resources/images/icons/user_delete.png',
         handler : function() {

            if ( ref.selectedUser == null ) {
               Ext.Msg.alert( 'Error', 'Select a user name<br/>' );
               return;
            }

            var groupMember = ref.selectedUser.data.memberName;
            var groupName = ref.selectedGroup[0].data.groupName;

            UserManagerService.deleteUserFromGroup( groupName, groupMember, {
               callback : function(status) {
                  var panel = ASPIREdb.view.UserManagerWindow.down( '#ASPIREdb_UserManagerpanel' );
                  var groupMemberGrid = panel.down( '#groupMemeberGrid' );

                  // var selection = ref.selectedUser[0].data.memberName;
                  var selection = groupMemberGrid.getView().getSelectionModel().getSelection()[0];
                  if ( selection ) {
                     groupMemberGrid.store.remove( selection );
                  }

                  console.log( 'selected user :' + groupMember + ' deleted' );
               }
            } );
         }
      } );

   }
} );
