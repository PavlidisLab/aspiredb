Ext.define( 'ASPIREdb.model.VariantProperty', {
   extend : 'Ext.data.Model',
   fields : [ {
      name : 'displayName',
      type : 'string'
   }, {
      name : 'exampleValues',
      type : 'string'
   }, {
      name : 'supportsSuggestions',
      type : 'boolean'
   }, {
      name : 'operators',
      type : 'auto'
   } ]
} );