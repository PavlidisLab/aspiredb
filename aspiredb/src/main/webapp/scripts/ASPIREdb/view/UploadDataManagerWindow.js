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
Ext.require( [ 'Ext.Window','ASPIREdb.view.UploadManagerFormPanel' ] );

Ext.define( 'ASPIREdb.view.UploadDataManagerWindow', {
   extend : 'Ext.Window',
   alias : 'widget.uploadDataManagerWindow',
   singleton : true,
   title : 'Upload Data Manager',
   closable : true,
   closeAction : 'hide',
   width : 800,
   height : 600,
   layout : 'fit',
   bodyStyle : 'padding: 5px;',

   config : {

      // active project ID values holder
      activeProjectIds : [],

   },
   items : [ {
      region : 'center',
      itemId : 'uploadManagerFormPanel',
      xtype : 'uploadManagerFormPanel',
   } ],

   initComponent : function() {

      this.callParent();

   },
   /**
    * Show the upload manager panel
    */
   initGridAndShow : function() {

      var ref = this;
      var panel = ASPIREdb.view.UploadDataManagerWindow.down( '#uploadManagerFormPanel' );

      ref.show();

   },

} );