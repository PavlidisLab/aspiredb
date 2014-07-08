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

Ext.require( [ 'Ext.Window', 'ASPIREdb.view.UserManagerPanel', 'ASPIREdb.GemmaURLUtils' ] );
/**
 * Gene manager has Gene Panel
 */
Ext.define( 'ASPIREdb.view.UserManagerWindow', {
   extend : 'Ext.Window',
   alias : 'widget.UserManagerWindow',
   singleton : true,
   title : 'User Manager',
   closable : true,
   closeAction : 'hide',
   width : 1000,
   height : 500,
   layout : 'fit',
   bodyStyle : 'padding: 5px;',

   items : [ {
      region : 'center',
      itemId : 'ASPIREdb_UserManagerpanel',
      xtype : 'ASPIREdb_UserManagerpanel',
   } ],

   config : {
      groupMemeberSize : [],
   },

   initComponent : function() {

      var ref = this;
      this.callParent();

   },

   /**
    * Show the gene manager window
    */
   initGridAndShow : function() {

      var ref = this;
      var panel = ASPIREdb.view.UserManagerWindow.down( '#ASPIREdb_UserManagerpanel' );
      var grid = panel.down( '#userGroupGrid' );

      ref.show();
      grid.setLoading( true );

      ref.groupMemeberSize = []
   
      UserManagerService.loadUserEditableGroups( {
         callback : function(ugs) { 
            console.log("user manager "+ugs);
           ref.populategroupMemeberGrid( ugs );
           
         },
         errorHandler : function(er, exception) {
            Ext.Msg.alert( "Load user group Error", er + "\n" + exception.stack );
            console.log( exception.stack );
         }
      } );

   },

   /**
    * Populate and gene set names in the gene set grid
    */
   populategroupMemeberGrid : function(ugs) {

      var panel = ASPIREdb.view.UserManagerWindow.down( '#ASPIREdb_UserManagerpanel' );
      var grid = panel.down( '#userGroupGrid' );

      var data = [];
      for (var i = 0; i < ugs.length; i++) {
         var row = [ ugs[i], '', 0 ];
         data.push( row );
      }

      grid.store.loadData( data );
      grid.setLoading( false );
      grid.getView().refresh();
      grid.enableToolbar();
   },

   clearGridAndMask : function() {
      ASPIREdb.view.UserManagerWindow.getComponent( 'ASPIREdb_UserManagerpanel' ).store.removeAll();
      ASPIREdb.view.UserManagerWindow.getComponent( 'ASPIREdb_UserManagerpanel' ).setLoading( true );
   }

} );