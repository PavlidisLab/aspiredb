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
Ext.require( [ 'Ext.panel.Panel', 'ASPIREdb.view.ProjectGrid', 'ASPIREdb.view.ProjectUserGrid',
              'ASPIREdb.view.ProjectUploadGrid' ] );
/**
 * Project panel includes ProjectSetGrid and ProjectGrid
 */
Ext.define( 'ASPIREdb.ProjectManagerPanel', {
   extend : 'Ext.panel.Panel',
   alias : 'widget.ASPIREdb_projectmanagerpanel',
   layout : 'border',
   items : [ {
      region : 'center',
      xtype : 'ProjectGrid',
      id : 'ProjectGrid',
      width : '50%', //480,
      collapsible : true,
      split : true,
      title : 'Project'
   }, {
      region : 'south',
      xtype : 'ProjectUploadGrid',
      id : 'ProjectUploadGrid',
      width : '100%', //480,
      title : 'Upload files to project'
   }, 

   ],

   initComponent : function() {
      this.callParent();
      
      var me = this;
      LoginStatusService.isUserAdministrator( {
         callback : function(admin) {
            if ( admin ) {
               me.add({
                  region : 'east',
                  xtype : 'ProjectUserGrid',
                  id : 'ProjectUserGrid',
                  width : '50%', //480,
                  collapsible : true,
                  split : true,
                  title : 'Project Users'
               });
               me.down("#ProjectGrid").doLayout();
            }
         }
      });
   },
   
} );