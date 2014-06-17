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
Ext.require( [ 'Ext.Window','ASPIREdb.view.UploadDataManagerWindow','ASPIREdb.view.ProjectManagerWindow','ASPIREdb.view.UserManagerWindow'] );

Ext.define( 'ASPIREdb.view.DashboardWindow', {
   extend : 'Ext.Window',
   alias : 'widget.dashboardWindow',
   id : 'dashboardWindow',
   singleton : true,
   title : 'Dashboard',
   closable : true,
   closeAction : 'hide',
   width : 400,
   height : 250,
   layout : {
      type : 'vbox',
      align : 'center'
   },
   bodyStyle : 'padding: 5px;',
   border : false,

   config : {
      // active project ID values holder
      activeProjectIds : [],

   },
   dockedItems : [ {
      xtype : 'toolbar',
      itemId : 'dashboardToolbar',
      dock : 'top'
   } ],

   initComponent : function() {

      this.callParent();
      this.enableToolbar();
      var ref = this;
      var projectStore = Ext.create( 'Ext.data.Store', {
         proxy : {
            type : 'dwr',
            dwrFunction : ProjectService.getProjects,
            model : 'ASPIREdb.model.Project',
            reader : {
               type : 'json',
               root : 'name'
            }
         }
      } );

      var projectComboBox = Ext.create( 'Ext.form.ComboBox', {
         id : 'projectField',
         name : 'unit',
         fieldLabel : 'Project',
         store : projectStore,
         editable : false,
         displayField : 'name',
         allowBlank : false,
         valueField : 'id',
         forceSelection : true,
         emptyText : "Choose project...",
         msgTarget : 'qtip'
      } );

      this.add( projectComboBox );

      projectComboBox.on( 'select', function() {

         ProjectService.numSubjects( [ projectComboBox.getValue() ], {
            callback : function(numSubjects) {

               ref.getComponent( 'numSubjects' ).setText( 'Number of Subjects: ' + numSubjects );

            }

         } );

         ProjectService.numVariants( [ projectComboBox.getValue() ], {
            callback : function(numVariants) {

               ref.getComponent( 'numVariants' ).setText( 'Number of Variants:  ' + numVariants );

            }

         } );

      } );

      var okButton = Ext.create( 'Ext.Button', {
         text : 'ok',
         handler : function() {
            // var selectedProjectId=0;

            if ( !projectComboBox.getValue() ) {

               projectComboBox.setActiveError( 'Please select project' );
               return;
            } else {
               var selectedProjectId = projectComboBox.getValue();
               // TODO : Now only one project is loaded at a time, but in future this might change
               if ( selectedProjectId != ref.activeProjectIds[0] ) {

                  ASPIREdb.ActiveProjectSettings.setActiveProject( [ {
                     id : projectComboBox.getValue(),
                     name : projectComboBox.getRawValue(),
                     description : ''
                  } ] );

                  var filterConfigs = [];

                  ref.activeProjectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds();
                  var projectFilter = new ProjectFilterConfig;
                  projectFilter.projectIds = ref.activeProjectIds;
                  filterConfigs.push( projectFilter );
                  console.log( "filter_submit event from DashBoard window" );
                  ASPIREdb.EVENT_BUS.fireEvent( 'filter_submit', filterConfigs );

                  console.log( "query_update event from DashboardWindow" );
                  ASPIREdb.EVENT_BUS.fireEvent( 'query_update' );
                  ASPIREdb.EVENT_BUS.fireEvent( 'project_select' );

               }
               ref.close();

            }
         }
      } );

      this.add( {
         xtype : 'label',
         itemId : 'numSubjects',
         text : 'Number of Subjects:',
         margin : '20 20 5 20'
      }, {
         xtype : 'label',
         itemId : 'numVariants',
         text : 'Number of Variants:',
         margin : '5 20 20 20'
      } );

      this.add( okButton );
      
     ASPIREdb.EVENT_BUS.on( 'new_project_created', ref.refreshDashboardHandler, ref );

   },
   /**
    * Enable the tool bar in dash board
    * 
    */
   enableToolbar : function() {
      var me=this;
      this.getDockedComponent( 'dashboardToolbar' ).removeAll();
      
      LoginStatusService.isUserAdministrator({
         callback : function(admin) {
            if (admin){
               //add upload project button
               me.getDockedComponent( 'dashboardToolbar' ).add( {
                  xtype : 'button',
                  id : 'createProject',
                  text : 'New Project',
                  tooltip : 'Craete new Project',
                  icon : 'scripts/ASPIREdb/resources/images/icons/page_upload.png',
                  handler : function() {
                     ASPIREdb.view.UploadDataManagerWindow.initGridAndShow();
          
                  }
               } );
               
               me.getDockedComponent( 'dashboardToolbar' ).add( '-' );
               
               //add project manager button
               me.getDockedComponent( 'dashboardToolbar' ).add( {
                  xtype : 'button',
                  id : 'manageProject',
                  text : 'Manage Project',
                  tooltip : 'Add users and upload variants or upload phenotypes',
                  icon : 'scripts/ASPIREdb/resources/images/icons/wrench.png',
                  handler : function() {
                     ASPIREdb.view.ProjectManagerWindow.initGridAndShow();
          
                  }
               } );
               
 me.getDockedComponent( 'dashboardToolbar' ).add( '-' );
               
               //add project manager button
               me.getDockedComponent( 'dashboardToolbar' ).add( {
                  xtype : 'button',
                  id : 'manageManager',
                  text : 'Manage User',
                  tooltip : 'Create groups and invite users to group',
                  icon : 'scripts/ASPIREdb/resources/images/icons/wrench.png',
                  handler : function() {
                     ASPIREdb.view.UserManagerWindow.initGridAndShow();
          
                  }
               } );
               
               
               
            }
               
         }
      });
      
      
      
      

   },
   /**
    * Refresh the dash board
    */
   refreshDashboardHandler : function(){
     this.getView().refresh( true );
    //  Ext.getCmp('projectField').store.load();
      
   }

} );