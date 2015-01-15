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
   width : 400,
   height : 300,
   closable : true,
   modal : true,

   initComponent : function() {
      this.callParent();
   },

   constructData : function(variantStore, variantGenes) {
      var data = [];

      for (id in variantGenes) {
         var genes = variantGenes[id];

         for (i = 0; i < genes.length; i++) {
            var gene = genes[i];
            var varRec = variantStore.findRecord( 'id', id );

            data.push( [ id, varRec.get( 'patientId' ), varRec.get( 'variantType' ), varRec.get( 'genomeCoordinates' ),
                        gene['symbol'] ] );
         }

      }

//      console.log( Ext.JSON.encode( data ) );
      return data;
   },

   constructFields : function() {
      var fields = [ 'id', 'patientId', 'variantType', 'genomeCoordinates', 'gene' ];
      return fields;
   },

   constructColumns : function() {
      var columns = [ {
         header : 'Patient Id',
         dataIndex : 'patientId',
         flex : 1,
      }, {
         header : 'Type',
         dataIndex : 'variantType',
         flex : 1,
      }, {
         header : 'Genome Coordinates',
         dataIndex : 'genomeCoordinates',
         flex : 1,
      }, {
         header : 'Gene',
         dataIndex : 'gene',
         flex : 1,
         renderer : function(value, meta, rec, rowIndex, colIndex, store) {
            meta.tdAttr = 'data-qtip="Click to apply a label"';
            return value + "&nbsp;&nbsp; <i class='fa fa-tags'></i>";
         }
      } ];

      return columns;
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
         columns : columns
      } );

      me.add( grid );
      me.show();
   },

} );