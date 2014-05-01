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
Ext.require( [ 'Ext.Window' ] );

Ext.define( 'ASPIREdb.view.DashboardWindow', {
   extend : 'Ext.Window',
   alias : 'widget.dashboardWindow',
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

   initComponent : function() {

      this.callParent();

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

   }

} );