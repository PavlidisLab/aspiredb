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
 * Grid that displays a 'Create or apply label' context menu when right-clicked
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
      isSubjectLabel : false,
   },

   constructor : function(cfg) {
      this.initConfig( cfg );
      this.callParent( arguments );
   },

   listeners : {

      itemcontextmenu : function(view, record, item, index, e) {
         // Stop the browser getting the event
         e.preventDefault();

         var me = this;

         var contextMenu = new Ext.menu.Menu( {
            items : [ {
               text : 'Create label',
               handler : function() {
                  me.makeLabelHandler( index );
               },
               scope : this,
            }, {
               text : 'Apply label',
               handler : function() {
                  me.applyLabelHandler( index );
               },
               scope : this,
            } ]
         } );

         contextMenu.showAt( e.getX(), e.getY() );
      }
   },

   selModel : Ext.create( 'Ext.selection.RowModel', {
      mode : 'MULTI',
   } ),

   /**
    * Override as necessary
    */
   getSelectedIds : function() {
      var rows = this.getSelectionModel().getSelection();
      if ( rows.length == 0 ) {
         return;
      }

      var ids = [];

      for (var i = 0; i < rows.length; i++) {
         ids.push( rows[i].data.id );
      }

      return ids;
   },

   makeLabelHandler : function(index) {

      var ids = this.getSelectedIds( index );

      // console.log( 'index=' + index + 'rec=' + Ext.JSON.encode( ids ) )

      var labelWin = Ext.create( 'ASPIREdb.view.CreateLabelWindow', {
         title : 'Create Variant Label',
         isSubjectLabel : this.isSubjectLabel,
         selectedIds : ids,
         header: {
            items: [{
                xtype: 'image',
                src: 'scripts/ASPIREdb/resources/images/qmark.png',
                listeners: {
                   afterrender: function(c) {
                       Ext.create('Ext.tip.ToolTip', {
                           target: c.getEl(),
                           html: 'Create and apply a label to the selected group of variants. Enter a name and choose a color.'
                       });
                   }
               }
            }]
        },
      } );
      labelWin.show();
   },
   
   applyLabelHandler : function(index) {
      var ids = this.getSelectedIds( index );
      
      var labelWin = Ext.create( 'ASPIREdb.view.ApplyLabelWindow', {
         isSubjectLabel : false,
         title : 'Apply Variant Labels',
         header: {
            items: [{
                xtype: 'image',
                src: 'scripts/ASPIREdb/resources/images/qmark.png',
                listeners: {
                   afterrender: function(c) {
                       Ext.create('Ext.tip.ToolTip', {
                           target: c.getEl(),
                           html: 'Apply existing labels to the selected group of variants.'
                       });
                   }
               }
            }]
        },         
         extend : 'ASPIREdb.view.ApplyLabelWindow',
         selectedIds : ids,
      } );
      labelWin.show();
   },

   initComponent : function() {
      this.callParent();
   },

} );