Ext.require( [ 'Ext.data.ArrayStore' ] );


Ext.define( 'ASPIREdb.store.PhenotypeEnrichmentStore', {
   extend : 'Ext.data.ArrayStore',
   alias : 'store.phenotypeEnrichmentStore',
   fields : [ {
      name : 'name',
      type : 'string'
   }, {
      name : 'inGroupPresent',
      sortType : 'asFraction'
   }, {
      name : 'outGroupPresent',
      sortType : 'asFraction'
   }, {
      name : 'pValue',
      type : 'string'
   }, {
      name : 'corrpValue',
      type : 'string'
   } ],

   storeId : 'phenotypeEnrichments'

} );