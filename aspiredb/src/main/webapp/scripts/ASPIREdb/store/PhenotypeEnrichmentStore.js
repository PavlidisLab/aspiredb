Ext.require( [ 'Ext.data.ArrayStore' ] );

Ext.define( 'ASPIREdb.store.PhenotypeEnrichmentStore', {
   extend : 'Ext.data.ArrayStore',
   alias : 'store.phenotypeEnrichmentStore',

   fields : [ {
      name : 'name',
      type : 'string'
   }, {
      name : 'inGroupPresent',
      type : 'string'
   }, {
      name : 'outGroupPresent',
      type : 'string'
   }, {
      name : 'pValue',
      type : 'string'
   }, {
      name : 'corrpValue',
      type : 'string'
   } ],

   storeId : 'phenotypeEnrichments'

} );