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
 * Project panel includes a ProjectGrid and ProjectUploadGrid.
 */
Ext.define( 'ASPIREdb.ProjectManagerPanel', {
   extend : 'Ext.panel.Panel',
   alias : 'widget.ASPIREdb_projectmanagerpanel',
   layout : 'border',
   items : [ {
      region : 'west',
      xtype : 'ProjectGrid',
      id : 'ProjectGrid',
      width : '50%', // 480,
      collapsible : true,
      split : true,
      title : 'Project'
   }, {
      region : 'center',
      xtype : 'ProjectUploadGrid',
      id : 'ProjectUploadGrid',
      // width : '50%', // 480,
      split : true,
      collapsible : false,
      title : 'Upload files to project',
      header: {
         items: [{
             xtype: 'image',       
             style:'right: auto; left: 0px; top: 6px;',          
             src: 'scripts/ASPIREdb/resources/images/qmark.png',     
             height: '14px',
             width: '15px',
             listeners: {
                afterrender: function(c) {
                    var toolTip = Ext.create('Ext.tip.ToolTip', {
                        target: c.getEl(),
                        html: 'Uploading files to existing projects will cause the uploaded data to be added to the project.',                        
                        showDelay: 0,                        
                    });                    
                    
                }
            }
         }],
         layout: 'fit'
     },
   },

   ],

   initComponent : function() {
      this.callParent();

      var me = this;
      LoginStatusService.isUserAdministrator( {
         callback : function(admin) {
            if ( admin ) {
               me.add( {
                  region : 'east',
                  xtype : 'ProjectUserGrid',
                  id : 'ProjectUserGrid',
                  width : '50%', // 480,
                  collapsible : true,
                  split : true,
                  title : 'Project Users'
               } );
               me.down( "#ProjectGrid" ).doLayout();
            }
         }
      } );
   },

} );