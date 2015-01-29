Ext.require( [ 'Ext.data.ArrayStore' ] );

Ext.apply( Ext.data.SortTypes, {
   asFraction : function(fraction) {
      var tokens = fraction.split( '/' );
      if ( tokens.length != 2 ) {
         return fraction;
      }
      return parseInt( tokens[0] ) / parseInt( tokens[1] ) * 1.0;
   }
} );

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