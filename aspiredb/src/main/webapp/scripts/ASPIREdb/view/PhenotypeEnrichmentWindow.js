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

Ext.require( [ 'Ext.Window', 'ASPIREdb.view.PhenotypeEnrichmentGrid' ] );

Ext.define( 'ASPIREdb.view.PhenotypeEnrichmentWindow',
   {
      extend : 'Ext.Window',
      alias : 'widget.phenotypeEnrichmentWindow',
      singleton : true,
      title : 'Phenotype Enrichment',
      closable : true,
      closeAction : 'hide',
      width : 800,
      height : 500,
      layout : 'fit',
      bodyStyle : 'padding: 5px;',

      items : [ {
         xtype : 'phenotypeEnrichmentGrid',
         itemId : 'phenotypeEnrichmentGrid'
      } ],

      initComponent : function() {
         var ref = this;

         this.callParent();

      },

      populateGrid : function(vos) {

         var grid = ASPIREdb.view.PhenotypeEnrichmentWindow.getComponent( 'phenotypeEnrichmentGrid' );

         grid.valueObjects = vos;

         var data = [];
         for (var i = 0; i < vos.length; i++) {
            var vo = vos[i];

            var row = [ vo.name, vo.inGroupTotalString, vo.outGroupTotalString, vo.PValueString,
                       vo.PValueCorrectedString ];
            data.push( row );
         }

         grid.store.loadData( data );

      },

      clearGrid : function() {

         var grid = ASPIREdb.view.PhenotypeEnrichmentWindow.getComponent( 'phenotypeEnrichmentGrid' );

         grid.getStore().removeAll();

      }

   } );