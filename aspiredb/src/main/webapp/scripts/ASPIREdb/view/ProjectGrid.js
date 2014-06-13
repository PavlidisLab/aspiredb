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

Ext.require( [ 'ASPIREdb.store.ProjectStore', 'Ext.grid.*', 'Ext.data.*', 'Ext.util.*', 'Ext.state.*', 'Ext.form.*' ] );

var rowEditing = Ext.create( 'Ext.grid.plugin.RowEditing', {
   // clicksToMoveEditor: 1,
   clicksToEdit : 2,
   autoCancel : false
} );

// TODO js documentation
Ext.define( 'ASPIREdb.view.ProjectGrid', {
   extend : 'Ext.grid.Panel',
   alias : 'widget.ProjectGrid',
   emptyText : 'No project found',
   id : 'ProjectGrid',
   border : true,
   store : Ext.create( 'ASPIREdb.store.ProjectStore' ),

   config : {
      // collection of all projects
      LoadedProjectNames : [],
      // collection of selected project value objects
      selProject : [],
      projectValueObjects : [],
   },

   dockedItems : [ {
      xtype : 'toolbar',
      itemId : 'ProjectGridToolbar',
      dock : 'top'
   } ],

   columns : [ {

      header : 'Name',
      dataIndex : 'ProjectName',
      flex : 1,
      editor : {
         // defaults to text field if no xtype is supplied
         allowBlank : false
      }
   }, {
      header : 'Description',
      dataIndex : 'projectDescription',
      flex : 1,
      editor : {
         // defaults to text field if no xtype is supplied
         allowBlank : true
      }
   } ],

   plugins : [ rowEditing ],
   listeners : {
      'selectionchange' : function(view, records) {
         this.down( '#removeProject' ).setDisabled( !records.length );
      }
   },

   initComponent : function() {

      this.callParent();

      this.on( 'select', this.ProjectSelectHandler, this );

      ASPIREdb.EVENT_BUS.on( 'project_added', this.projectAddedHandler, this );

      this.on( 'edit', function(editor, e) {
         var record = e.record;
         var me = this;

         ProjectService.findUserProject( me.selProject[0].data.ProjectName, {
            callback : function(psvo) {
               // console.log( 'found project name ' + psvo.name + ' decription' + psvo.description );
               psvo.name = record.data.ProjectName;
               psvo.description = record.data.projectDescription;
               // console.log( 'AFTER UPDATE - found project name ' + psvo.name + ' decription' + ' to string '
               // + psvo.description + psvo.id );

            },
            errorHandler : function(er, exception) {
               Ext.Msg.alert( "find user project Error", er + "\n" + exception.stack );
               console.log( exception.stack );
            }
         } );

      } );

   },

   updateProjectGridHandler : function(ProjectName) {
      var panel = ASPIREdb.view.ProjectManagerWindow.down( '#ASPIREdb_projectmanagerpanel' );
      var projectGrid = panel.down( '#ProjectGrid' );

      // TODO : refresh grid when loaded
      this.store.add( ProjectName );
      this.getView().refresh( true );
      this.setLoading( false );
   },

   projectAddedHandler : function(pvo) {
      this.projectValueObjects.push( pvo );

   },

   ProjectSelectHandler : function(ref, record, index, eOpts) {

      var me = this;
      this.selProject = this.getSelectionModel().getSelection();
      var ProjectName = this.selProject[0].data.ProjectName;

      ProjectService.getProjectUserNames( ProjectName, {
         callback : function(userNames) {
            console.log( 'project users :' + userNames );
            me.populateProjectGrid( userNames, ProjectName );
         },
         errorHandler : function(er, exception) {
            Ext.Msg.alert( "Project Grid : get User Error", er + "\n" + exception.stack );
            console.log( exception.stack );
         }
      } );
      ASPIREdb.EVENT_BUS.fireEvent( 'Project_selected', this.selProject );

   },

   // Populate projects in project grid
   populateProjectGrid : function(userNames, projectName) {
      var test = "";

      ProjectService.getProjectUserGroups( projectName, {
         callback : function(userGroupMap) {
            console.log( 'project user groups :' + userGroupMap );

            var panel = ASPIREdb.view.ProjectManagerWindow.down( '#ASPIREdb_projectmanagerpanel' );
            var grid = panel.down( '#ProjectUserGrid' );

            if ( userGroupMap != null ) {
               var data = [];

               for (var i = 0; i < userNames.length; i++) {
                  var userName = userNames[i];

                  var row = [ userName, userGroupMap[userName] ];
                  data.push( row );

               }

               grid.store.loadData( data );
               grid.setLoading( false );
               grid.getView().refresh();
            }
         },
         errorHandler : function(er, exception) {
            Ext.Msg.alert( "Project Grid : get User Group Error", er + "\n" + exception.stack );
            console.log( exception.stack );
         }

      } );

   },

   enableToolbar : function() {

      this.getDockedComponent( 'ProjectGridToolbar' ).removeAll();

      this.getDockedComponent( 'ProjectGridToolbar' ).add( {
         xtype : 'textfield',
         id : 'ProjectName',
         text : '',
         scope : this,
         allowBlank : true,
         emptyText : 'Type project Name',

      } );

      this.getDockedComponent( 'ProjectGridToolbar' ).add( '-' );

      var ref = this;

      this.getDockedComponent( 'ProjectGridToolbar' ).add( {
         xtype : 'button',
         id : 'addProject',
         text : '',
         tooltip : 'Add new project',
         icon : 'scripts/ASPIREdb/resources/images/icons/add.png',
         // TODO: Need a better workaround
         handler : function() {

            var newProjectName = ref.down( '#ProjectName' ).getValue();

            ProjectService.createUserProject( newProjectName, '', {
               callback : function(message) {
                  if (message=="Success"){
                     var panel = ASPIREdb.view.ProjectManagerWindow.down( '#ASPIREdb_projectmanagerpanel' );
                     var ProjectGrid = panel.down( '#ProjectGrid' );

                     // add project name to Project grid
                     var data = [];
                     var row = [ newProjectName, '', 0 ];
                     data.push( row );
                     ProjectGrid.store.add( data );
                     ProjectGrid.getView().refresh( true );
                     ProjectGrid.setLoading( false );
                  
                     ref.down( '#ProjectName' ).setValue( '' );
                    // console.log( 'returned project value object : ' + message );
                     ASPIREdb.EVENT_BUS.fireEvent( 'new_project_saved' );
                  }else {
                     Ext.Msg.alert( 'Failure','fail to create project!' );
                  }
                  
               }
            } );

         }
      } );

      this.getDockedComponent( 'ProjectGridToolbar' ).add( {
         xtype : 'button',
         id : 'removeProject',
         text : '',
         tooltip : 'Remove the selected project',
         icon : 'scripts/ASPIREdb/resources/images/icons/delete.png',
         handler : function() {
            var test =ref.selProject[0].data.ProjectName;
            // Delete project
            ProjectService.deleteProject( ref.selProject[0].data.ProjectName, {
               callback : function(message) {
                  if (message == "Success"){
                     var panel = ASPIREdb.view.ProjectManagerWindow.down( '#ASPIREdb_projectmanagerpanel' );
                     var ProjectGrid = panel.down( '#ProjectGrid' );

                     var selection = ProjectGrid.getView().getSelectionModel().getSelection()[0];
                     if ( selection ) {
                        ProjectGrid.store.remove( selection );
                     }

                     console.log( 'selected Project :' + ref.selProject[0].data.ProjectName + ' deleted' );
                  }else {
                     Ext.Msg.alert( 'Failure','fail to remove project!' );
                  }
                 
               }
            } );

         }
      } );

   }
} );
