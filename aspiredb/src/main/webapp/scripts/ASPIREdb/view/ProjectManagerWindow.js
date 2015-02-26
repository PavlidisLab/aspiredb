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

Ext.require( [ 'Ext.Window', 'ASPIREdb.view.ProjectManagerPanel' ] );
/**
 * Project manager has Project Panel
 */
Ext.define( 'ASPIREdb.view.ProjectManagerWindow', {
   extend : 'Ext.Window',
   alias : 'widget.ProjectManagerWindow',
   singleton : true,
   title : 'Project Manager',
   closable : true,
   closeAction : 'hide',
   width : 800,
   height : 650,
   layout : 'fit',
   bodyStyle : 'padding: 5px;',

   items : [ {
      region : 'center',
      itemId : 'ASPIREdb_projectmanagerpanel',
      xtype : 'ASPIREdb_projectmanagerpanel',
   } ],

   config : {
      ProjectsetSize : [],
   },

   initComponent : function() {

      var ref = this;
      this.callParent();

   },

   /**
    * Show the Project manager window
    */
   initGridAndShow : function() {

      var ref = this;
      var panel = ASPIREdb.view.ProjectManagerWindow.down( '#ASPIREdb_projectmanagerpanel' );
      var grid = panel.down( '#ProjectGrid' );

      ref.show();
      grid.setLoading( true );

      ref.ProjectsetSize = []

      ProjectService.getProjects( {
         callback : function(pvos) {
            ASPIREdb.view.ProjectManagerWindow.populateProjectGrid( pvos );
         }
      } );

   },

   /**
    * Populate and Project set names in the Project set grid
    */
   populateProjectGrid : function(pvos) {

      var panel = ASPIREdb.view.ProjectManagerWindow.down( '#ASPIREdb_projectmanagerpanel' );
      var grid = panel.down( '#ProjectGrid' );

      var data = [];
      for (var i = 0; i < pvos.length; i++) {
         var row = [ pvos[i].name, '', 0 ];
         data.push( row );
      }

      grid.store.loadData( data );
      grid.setLoading( false );
      grid.getView().refresh();
      grid.enableToolbar();
   },

   clearGridAndMask : function() {
      ASPIREdb.view.ProjectManagerWindow.getComponent( 'ASPIREdb_projectmanagerpanel' ).store.removeAll();
      ASPIREdb.view.ProjectManagerWindow.getComponent( 'ASPIREdb_projectmanagerpanel' ).setLoading( true );
   }

} );