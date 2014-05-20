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
Ext.require( [ 'Ext.Window','ASPIREdb.UploadManagerPanel' ] );

Ext.define( 'ASPIREdb.view.UploadDataManagerWindow', {
   extend : 'Ext.Window',
   alias : 'widget.uploadDataManagerWindow',
   singleton : true,
   title : 'Upload Data Manager',
   closable : true,
  // closeAction : 'hide',
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
   items : [{
      region : 'center',
      itemId : 'uploadManagerPanel',
      xtype : 'uploadManagerPanel',
   }],

   initComponent : function() {

      this.callParent();
     

   },
   /**
    * Show the gene manager window
    */   
   initGridAndShow : function(){
      
      var ref = this;
      var panel = ASPIREdb.view.UploadDataManagerWindow.down('#uploadManagerPanel');
      
      ref.show();
     
   
   },
   

} );