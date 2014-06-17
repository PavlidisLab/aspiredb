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

Ext.require( [ 'Ext.grid.Panel', 'ASPIREdb.store.VariantFileStore' ]);

// TODO js documentation
Ext.define( 'ASPIREdb.view.VariantFileUploadGrid', {
   extend : 'Ext.grid.Panel',
   alias : 'widget.variantFileUploadGrid',
   emptyText : 'No files uploaded',
   id : 'variantFileUploadGrid',
   border : false,
   multiSelect : true,

   dockedItems : [ {
      xtype : 'toolbar',
      itemId : 'variantFileUploadGridToolbar',
      dock : 'top'
   } ],

   columns : [
              {
                 header : 'File Name',
                 dataIndex : 'filename',
                 flex : 1
              },
              {
                 header : 'File Size',
                 dataIndex : 'size',
                 flex : 1
              },
              {
                 header : 'No of Variants',
                 dataIndex : 'NoVariants',
                 flex : 1
              },
              {
                 header : 'Staus',
                 dataIndex : 'status',
                 flex : 1
              },
            ],

   selModel : Ext.create( 'Ext.selection.CheckboxModel', {
      mode : 'MULTI',
   } ),

   store : Ext.create( 'ASPIREdb.store.VariantFileStore' ),

   initComponent : function() {
      this.callParent();
      var me = this;
     // this.on( 'select', me.geneSelectHandler, me );

   },

   variantFileSelectHandler : function(ref, record, index, eOpts) {
     // var selGenes = this.getSelectionModel().getSelection();
  //   this.selectedgenes = [];
   //   for (var i = 0; i < selGenes.length; i++) {
  //      delete selGenes[i].data.pheneName;
   //      this.selectedgenes.push( selGenes[i].data );
  //    }

      // ASPIREdb.EVENT_BUS.fireEvent('new_geneSet_selected', this.selectedgenes);
  //   this.down( '#saveButtonGeneSet' ).enable();
   },

   setLodedvariantvalueObjects : function(vvo) {

      this.LoadedVariantValueObjects = vvo;
   },

   enableToolbar : function(vos) {

      if ( vos.length < 1 ) {
         return;
      }


      // this.getDockedComponent('geneHitsByVariantGridToolbar').remove('viewCoexpressionNetworkButton');
      // this.getDockedComponent('geneHitsByVariantGridToolbar').remove('saveButtonGeneHits');

      // make sure we don't add to any existing items
      this.getDockedComponent( 'variantFileUploadGridToolbar' ).removeAll();

      
      this.getDockedComponent( 'variantFileUploadGridToolbar' ).add( {
         xtype : 'button',
         id : 'variantUpload',
         text : 'Upload',
         tooltip : 'Click to upload variants',
         disabled : true,
         handler : function() {
           //upload the file

         }
      } );

   }
} );
