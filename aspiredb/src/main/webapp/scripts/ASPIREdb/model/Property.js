Ext.require( [ 'ASPIREdb.model.Operator' ] );

Ext.define( 'ASPIREdb.model.Property', {
   extend : 'Ext.data.Model',
   fields : [ {
      name : 'displayName',
      type : 'string'
   }, {
      name : 'name',
      type : 'string',
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