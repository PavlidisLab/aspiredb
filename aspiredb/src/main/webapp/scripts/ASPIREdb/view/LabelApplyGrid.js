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

/**
 * Grid that prompts the user to apply a label to the selected rows when clicked. 
 */
Ext.define( 'ASPIREdb.view.LabelApplyGrid', {
   extend : 'Ext.grid.Panel',
   alias : 'widget.LabelApplyGrid',
   id : 'labelApplyGrid',
   
   config : {
      selectedSubjectIds : [],
      selSubjects : [],
      visibleLabels : [],
      gridPanelName : '',
   },
   
   constructor : function(cfg) {
      this.initConfig( cfg );
      this.callParent( arguments );
   },

   
   listeners : {
      cellclick : function(view, td, cellIndex, record, tr, rowIndex, e, eOpts) {
         var rows = this.getSelectionModel().getSelection();
         if ( rows.length == 0 ) {
            return;
         }
         
         var ids = [];

         for (var i = 0; i < rows.length; i++) {
            ids.push( rows[i].data.id );
         }
         

         console.log('rec=' + Ext.JSON.encode(ids))
         
         var labelWin = Ext.create( 'ASPIREdb.view.CreateLabelWindow', {
            isSubjectLabel : true,
            selectedIds : ids, 
         });
         labelWin.show();
      }
   },

   selModel : Ext.create( 'Ext.selection.RowModel', {
      mode : 'MULTI',
   } ),

   initComponent : function() {
      this.callParent();
   },

} );