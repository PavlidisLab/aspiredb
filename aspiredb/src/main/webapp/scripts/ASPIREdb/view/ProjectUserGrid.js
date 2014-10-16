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
              'Ext.util.*', 'Ext.state.*', 'Ext.form.*', 'ASPIREdb.store.ProjectUserStore', 'ASPIREdb.model.Project',
              'ASPIREdb.model.PropertyValue' ] );

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
      selectedUser : [],
      suggestionContext : null,

   },

   columns : [ {
      header : 'User Name',
      dataIndex : 'userName',
      flex : 1,
      editor : {
         allowBlank : true
      }
   }, {
      header : 'User Group',
      dataIndex : 'userGroup',
      flex : 1,
      editor : {
         allowBlank : true
      }
   } ],

// TODO Implement record update
   /*plugins : [ rowEditing ], 
   listeners : {
      'selectionchange' : function(view, records) {
         this.down( '#removeProject' ).setDisabled( !records.length );
         this.selectedUser = this.getSelectionModel().getSelection();

      }
   },*/

   initComponent : function() {
      this.callParent();
      var me = this;

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

} );
