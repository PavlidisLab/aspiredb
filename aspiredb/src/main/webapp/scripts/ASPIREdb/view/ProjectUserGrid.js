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

Ext.require( [ 'Ext.grid.*', 'ASPIREdb.store.ProjectStore', 'ASPIREdb.TextDataDownloadWindow', 'Ext.data.*',
              'Ext.util.*', 'Ext.state.*', 'Ext.form.*', 'ASPIREdb.ProjectUserStore',
              'ASPIREdb.model.Project', 'ASPIREdb.model.PropertyValue' ] );

var rowEditing = Ext.create( 'Ext.grid.plugin.RowEditing', {
   clicksToEdit : 2,
   autoCancel : false
} );

/**
 * Create project Grid
 */
Ext.define( 'ASPIREdb.view.ProjectUserGrid', {
   extend : 'Ext.grid.Panel',
   alias : 'widget.ProjectUserGrid',
   emptyText : 'No projects found',
   id : 'ProjectUserGrid',
   border : true,
   store : Ext.create( 'ASPIREdb.store.ProjectUserStore' ),

   config : {
      // collection of all the ProjectValueObject loaded
      LoadedProjectNames : [],
      // collection of selected project value objects
      selectedProject : [],
      gvos : [],
      selectedproject : [],
      suggestionContext : null,

   },

   dockedItems : [ {
      xtype : 'toolbar',
      itemId : 'projectUserGridToolbar',
      dock : 'top'
   } ],

   columns : [ {
      header : 'User Name',
      dataIndex : 'userName',
      flex : 1,
      editor : {
         // defaults to textfield if no xtype is supplied
         allowBlank : true
      }
   },{
      header : 'User Password',
      dataIndex : 'userPassword',
      inputType:'password',
      flex : 1,
      editor : {
         // defaults to textfield if no xtype is supplied
         allowBlank : true
      }
   }, {
      header : 'User Group',
      dataIndex : 'userGroup',
      flex : 1,
      editor : {
         // defaults to textfield if no xtype is supplied
         allowBlank : true
      }
   }],

   plugins : [ rowEditing ],
   listeners : {
      'selectionchange' : function(view, records) {
         this.down( '#removeProject' ).setDisabled( !records.length );
         this.selectedProject = this.getSelectionModel().getSelection();

      }
   },

   initComponent : function() {
      this.callParent();
      var me = this;
      me.enableToolbar();

      ASPIREdb.EVENT_BUS.on( 'project_selected', this.projectSelectHandler, this );

   },

   /**
    * Store the project value object when selected
    * 
    * @param ProjectSetValueObject 
    * @param selproject
    */
   projectSelectHandler : function(selProject) {
      this.selectedProject = selProject;
   },
   

   /**
    * Enable the tool bar in project grid
    * 
    */
   enableToolbar : function() {

      this.getDockedComponent( 'projectUserGridToolbar' ).removeAll();

      this.getDockedComponent( 'projectUserGridToolbar' ).add( {
         xtype : 'combo',
         id : 'projectUserGroup',
         emptyText : 'Type user group',
         width : 200,
         displayField : 'displayName',
         triggerAction : 'query',
         matchFieldWidth : false,
         hideTrigger : true,
         triggerAction : 'query',
         autoSelect : false,
         enableKeyEvents : true,
      /**   store : Ext.create( 'ASPIREdb.ProjectUserSuggestionStore', {
            remoteFunction : ProjectService.suggestProjectUserGroups
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

      this.getDockedComponent( 'projectUserGridToolbar' ).add( '-' );

      var ref = this;
      
      this.getDockedComponent( 'projectUserGridToolbar' ).add( {
         xtype : 'combo',
         id : 'projectUserName',
         emptyText : 'Type user name',
         width : 200,
         displayField : 'displayName',
         triggerAction : 'query',
         matchFieldWidth : false,
         hideTrigger : true,
         triggerAction : 'query',
         autoSelect : false,
         enableKeyEvents : true,
      /**   store : Ext.create( 'ASPIREdb.ProjectUserSuggestionStore', {
            remoteFunction : ProjectService.suggestProjectUserGroups
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

      this.getDockedComponent( 'projectUserGridToolbar' ).add( '-' );
      
      this.getDockedComponent( 'projectUserGridToolbar' ).add( {
         xtype : 'button',
         id : 'addUser',
         text : '',
         tooltip : 'Add projects to selected project name ',
         icon : 'scripts/ASPIREdb/resources/images/icons/add.png',
         handler : function() {

            var userName = ref.selectedProject[0].data.projectUserName;
            var userGroup = ref.selectedProject[0].data.projectUserGroup;
            
            var panel = ASPIREdb.view.projectManagerWindow.down( '#ASPIREdb_projectmanagerpanel' );
            var grid = panel.down( '#projectUserGridToolbar' );

            ProjectService.isUserInGroup( userName, {
               callback : function(gvoSta) {
                  if ( gvoSta ) {
                     Ext.Msg.alert( 'Project', 'Project already exist' );
                     grid.down( '#name' ).setValue( '' );
                  } else if ( ref.selectedGeneSet[0] != null ) {
                     
                     ProjectService.addUser( userName, userGroup, {
                        callback : function(gvo) {

                           var data = [];
                           var row = [ genesymbol, gvo.geneBioType, gvo.name, '' ];
                           data.push( row );

                           // TODO : refresh grid when loaded
                           grid.store.add( data );
                           grid.getView().refresh( true );
                           grid.setLoading( false );
                           grid.down( '#name' ).setValue( '' );

                           ASPIREdb.EVENT_BUS.fireEvent( 'projectUser_added', data );
                           // update the project name grid size
                           var panel = ASPIREdb.view.GeneManagerWindow.down( '#ASPIREdb_genemanagerpanel' );
                           var projectUserGrid = panel.down( '#projectUserGrid' );

                           var selection = projectUserGrid.getView().getSelectionModel().getSelection()[0];
                           if ( selection ) {
                              var oldSize = selection.data.projectSetSize;
                              selection.removeUser( 'projectSetSize', parseInt( oldSize ) + 1 );
                           }

                        },
                        errorHandler : function(er, exception) {
                           Ext.Msg.alert( "project Grid Error", er + "\n" + exception.stack );
                           console.log( exception.stack );
                        }
                     } );
                  } else
                     Ext.Msg.alert( 'Error', 'select the project name to add users ' );

               }
            } );

         }
      } );

      this.getDockedComponent( 'projectUserGridToolbar' ).add( {
         xtype : 'button',
         id : 'removeUser',
         text : '',
         tooltip : 'Remove the selected project',
         icon : 'scripts/ASPIREdb/resources/images/icons/delete.png',
         handler : function() {
            
            var userGroup = ref.selectedGene[0].data.projectUserGroup;
            
            ProjectService.deleteUser( ref.selectedGeneSet[0].data.projectUserName, userGroup, {
               callback : function() {

                  var panel = ASPIREdb.view.GeneManagerWindow.down( '#ASPIREdb_projectmanagerpanel' );
                  var ProjectUserGrid = panel.down( '#ProjectUserGrid' );

                  var selection = ProjectUserGrid.getView().getSelectionModel().getSelection()[0];
                  if ( selection ) {
                     ProjectUserGrid.store.remove( selection );
                  }

                  // resize the project size in project name grid
                  // update the project name grid size
                  var panel = ASPIREdb.view.ProjectManagerWindow.down( '#ASPIREdb_projectmanagerpanel' );
                  var projectGrid = panel.down( '#projectGrid' );

                  var selection = projectGrid.getView().getSelectionModel().getSelection()[0];
                  if ( selection ) {
                     var oldSize = selection.data.projectSetSize;
                     selection.set( 'projectSetSize', parseInt( oldSize ) - 1 );
                  }
               }

            } );
         }
      } );

   }
} );
