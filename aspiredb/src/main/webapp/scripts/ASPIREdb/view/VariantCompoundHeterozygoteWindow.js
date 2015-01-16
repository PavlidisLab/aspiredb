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

Ext.require( [ 'ASPIREdb.view.LabelApplyGrid' ] );

Ext.define( "ASPIREdb.view.VariantCompoundHeterozygoteWindow", {
   extend : 'Ext.Window',
   layout : 'fit',
   itemId : 'variantCompoundHeterozygoteWindow',
   title : 'Potential compound heterozygote variants',
   width : 500,
   height : 400,
   closable : true,
   modal : true,

   initComponent : function() {
      this.callParent();
   },

   // variantGenes = Map<String.PatientId, Map<GeneValueObject, Collection<VariantValueObject>>>
   constructData : function(variantStore, variantGenes) {
      var data = [];

      for (patientId in variantGenes) {
         for (gene in variantGenes[patientId]) { // dwr converted gene to a string

            var variantList = variantGenes[patientId][gene];

            data.push( [ patientId, gene, variantList ] );
         }

      }

      console.log( Ext.JSON.encode( data ) );
      return data;
   },

   constructFields : function() {
      var fields = [ 'patientId', 'gene', 'variants', ];
      return fields;
   },

   constructColumns : function() {
      var columns = [ {
         header : 'Patient Id',
         dataIndex : 'patientId',
         flex : 1,
      }, {
         header : 'Gene',
         dataIndex : 'gene',
         flex : 1,
      // renderer : function(value, meta, rec, rowIndex, colIndex, store) {
      // meta.tdAttr = 'data-qtip="Click to apply a variant label"';
      // return value + "&nbsp;&nbsp; <i class='fa fa-tags'></i>";
      // }
      }, {
         header : 'Genome Coordinates',
         dataIndex : 'variants',
         flex : 1,
         renderer : function(variantList, meta, rec, rowIndex, colIndex, store) {
            var coords = [];
            for (i = 0; i < variantList.length; i++) {
               coords.push( variantList[i]['genomeCoordinates'] );
            }
            return coords.join( '<br/>' );
         }
      }, ];

      return columns;
   },

   getSelectedIds : function(index) {

      if ( index == null ) {
         console.log( 'Warning index is null!' )
         return;
      }

      var store = Ext.StoreManager.lookup( '#cmpHetStore' );

      var variants = store.getAt( index ).get( 'variants' )

      if ( variants.length == 0 ) {
         return;
      }

      var ids = [];

      for (var i = 0; i < variants.length; i++) {
         ids.push( variants[i].id );
      }

      // console.log( 'return selected ids from VariantCompound! ' + Ext.JSON.encode( ids ) );

      return ids;
   },

   initGridAndShow : function(variantStore, variantGenes) {
      var me = this;

      // var data = [ ["991","45_L","CNV","6:32450499-32565205","RNU1-61P"] ];
      var data = me.constructData( variantStore, variantGenes );
      var fields = me.constructFields();
      var columns = me.constructColumns();

      var store = Ext.create( 'Ext.data.ArrayStore', {
         fields : fields,
         data : data,
         autoLoad : true,
         autoSync : true,
         storeId : '#cmpHetStore',
      } );

      var grid = Ext.create( 'ASPIREdb.view.LabelApplyGrid', {
         store : store,
         columns : columns,
         getSelectedIds : me.getSelectedIds,
         isSubjectLabel : false,
         itemId : '#cmpHetGrid',
         scope : me,
      } );

      me.add( grid );
      me.show();
   },

} );