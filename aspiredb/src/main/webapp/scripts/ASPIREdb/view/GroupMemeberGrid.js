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

Ext.require( [ 'Ext.grid.*', /**'ASPIREdb.store.GroupMemeberStore',*/ 'ASPIREdb.TextDataDownloadWindow', 'Ext.data.*', 'Ext.util.*',
              'Ext.state.*', 'Ext.form.*',/** 'ASPIREdb.GroupMemeberSuggestionStore', 'ASPIREdb.model.GroupMemeberProperty',*/
              'ASPIREdb.model.PropertyValue' ] );

var rowEditing = Ext.create( 'Ext.grid.plugin.RowEditing', {
   // clicksToMoveEditor: 1,
   clicksToEdit : 2,
   autoCancel : false
} );

/**
 * Create Group Memeber Grid
 */
Ext.define( 'ASPIREdb.view.GroupMemeberGrid', {
   extend : 'Ext.grid.Panel',
   alias : 'widget.groupMemeberGrid',
   emptyText : 'No groupMembers found',
   id : 'groupMemeberGrid',
   border : true,
  // store : Ext.create( 'ASPIREdb.store.GroupMemeberStore' ),

   config : {
      // collection of all the PhenotypeSummaryValueObject loaded
      LoadedGeneSetNames : [],
      // collection of selected groupMember value objects
      selectedGene : [],
      gvos : [],
      selectedGeneSet : [],
      suggestionContext : null,

   },

   dockedItems : [ {
      xtype : 'toolbar',
      itemId : 'groupMemeberGridToolbar',
      dock : 'top'
   } ],

   columns : [ {
      header : 'Name',
      dataIndex : 'name',
      flex : 1,
      editor : {
         // defaults to textfield if no xtype is supplied
         allowBlank : false
      }
   }, {
      header : 'Email',
      dataIndex : 'email',
      flex : 1,
      editor : {
         // defaults to textfield if no xtype is supplied
         allowBlank : true
      }
   } ],

   plugins : [ rowEditing ],
   listeners : {
      'selectionchange' : function(view, records) {
         this.down( '#removeGroupMemeber' ).setDisabled( !records.length );
         this.selectedGene = this.getSelectionModel().getSelection();

      }
   },

   initComponent : function() {
      this.callParent();
      var me = this;
      me.enableToolbar();

      ASPIREdb.EVENT_BUS.on( 'group_selected', this.groupMemberSetSelectHandler, this );

   },

   /**
    * Store the groupMember value object when selected
    * 
    * @param GeneSetValueObject
    *           selGeneSet
    */
   groupMemberSetSelectHandler : function(selGeneSet) {
      this.selectedGeneSet = selGeneSet;
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
         emptyText : 'Type group memeber email',
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
        // store : Ext.create( 'ASPIREdb.GroupMemeberSuggestionStore', {
       //     remoteFunction : VariantService.suggestGeneValues
       //  } ),
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
         id : 'addGroupMemebr',
         text : '',
         tooltip : 'Add group members to selected group',
         icon : 'scripts/ASPIREdb/resources/images/icons/user_add.png',
         handler : function() {

            // TODO: have to populate human taxon groupMember list auto complete features
            var groupMembersymbol = ref.down( '#groupMemberName' ).getValue();
            console.log( 'added groupMembers name  : ' + groupMembersymbol );
            var groupMemberSetName = ref.selectedGeneSet[0].data.groupMemberSetName;
            var panel = ASPIREdb.view.GeneManagerWindow.down( '#ASPIREdb_UserManagerpanel' );
            var grid = panel.down( '#groupMemeberGrid' );

          /**  UserGeneSetService.isGeneInGeneSet( groupMemberSetName, groupMembersymbol, {
               callback : function(gvoSta) {
                  if ( gvoSta ) {
                     Ext.Msg.alert( 'User Group', 'Group Memebr already exist in user group' );
                     grid.down( '#groupMemberName' ).setValue( '' );
                  } else if ( ref.selectedGeneSet[0] != null ) {
                     UserGeneSetService.addGenes( groupMemberSetName, groupMembersymbol, {
                        callback : function(gvo) {

                           var data = [];
                           var row = [ groupMembersymbol, gvo.groupMemberBioType, gvo.name, '' ];
                           data.push( row );

                           // TODO : refresh grid when loaded
                           grid.store.add( data );
                           grid.getView().refresh( true );
                           grid.setLoading( false );
                           grid.down( '#groupMemberName' ).setValue( '' );

                           ASPIREdb.EVENT_BUS.fireEvent( 'groupMember_added', data );
                           // update the groupMember set grid size
                           var panel = ASPIREdb.view.GeneManagerWindow.down( '#ASPIREdb_UserManagerpanel' );
                           var userGroupGrid = panel.down( '#userGroupGrid' );

                           var selection = userGroupGrid.getView().getSelectionModel().getSelection()[0];
                           if ( selection ) {
                              var oldSize = selection.data.userGroupSize;
                              selection.set( 'userGroupSize', parseInt( oldSize ) + 1 );
                           }

                        },
                        errorHandler : function(er, exception) {
                           Ext.Msg.alert( "Group Memeber Grid Error", er + "\n" + exception.stack );
                           console.log( exception.stack );
                        }
                     } );
                  } else
                     Ext.Msg.alert( 'Error', 'select the Group Name to add Memebers ' );

               }
            } );*/

         }
      } );

      this.getDockedComponent( 'groupMemeberGridToolbar' ).add( {
         xtype : 'button',
         id : 'removeGroupMemeber',
         text : '',
         tooltip : 'Remove the selected group member',
         icon : 'scripts/ASPIREdb/resources/images/icons/user_delete.png',
         handler : function() {
            var groupMemberSymbol = ref.selectedGene[0].data.symbol;
           /** UserGeneSetService.deleteGene( ref.selectedGeneSet[0].data.groupMemberSetName, groupMemberSymbol, {
               callback : function() {

                  var panel = ASPIREdb.view.GeneManagerWindow.down( '#ASPIREdb_UserManagerpanel' );
                  var groupMemeberGrid = panel.down( '#groupMemeberGrid' );

                  var selection = groupMemeberGrid.getView().getSelectionModel().getSelection()[0];
                  if ( selection ) {
                     groupMemeberGrid.store.remove( selection );
                  }

                  // resize the groupMember size in groupMember set grid
                  // update the groupMember set grid size
                  var panel = ASPIREdb.view.GeneManagerWindow.down( '#ASPIREdb_UserManagerpanel' );
                  var userGroupGrid = panel.down( '#userGroupGrid' );

                  var selection = userGroupGrid.getView().getSelectionModel().getSelection()[0];
                  if ( selection ) {
                     var oldSize = selection.data.userGroupSize;
                     selection.set( 'userGroupSize', parseInt( oldSize ) - 1 );
                  }
               }

            } );*/
         }
      } );

   }
} );
