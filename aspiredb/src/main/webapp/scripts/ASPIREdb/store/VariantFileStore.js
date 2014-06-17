Ext.require( [ 'Ext.data.ArrayStore' ] );

Ext.define( 'ASPIREdb.store.VariantFileStore', {
   extend : 'Ext.data.ArrayStore',
   alias : 'store.variantFileStore',

   fields : [
   // a PhenotypeSummary object
   {
      name : 'filename',
      type : 'auto',    
   },

   // the same PhenotypeSummary object
   // TODO find a more elegant way of doing this ...
   {
      name : 'size',
      type : 'auto',
    
   }, {
      name : 'NoVariants',
      type : 'auto',
   
   }, {
      name : 'status',
      type : 'auto',

   } ],

   storeId : 'variantFile'

} );