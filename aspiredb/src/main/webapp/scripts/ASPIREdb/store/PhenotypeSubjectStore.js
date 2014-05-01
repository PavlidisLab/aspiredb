Ext.require( [ 'Ext.data.ArrayStore' ] );

Ext.define( 'ASPIREdb.store.PhenotypeSubjectStore', {
   extend : 'Ext.data.ArrayStore',
   alias : 'store.phenotypeSubjectStore',
   groupField : 'phenotype',

   fields : [
   // a PhenotypeSummary object
   {
      name : 'name',
      type : 'auto',
      sortType : function(value) {
         return value.name;
      },
   },

   // the same PhenotypeSummary object
   // TODO find a more elegant way of doing this ...
   {
      name : 'NoOfsubjects',
      type : 'auto',
      sortType : function(value) {

         return value;
      },
   }, {
      name : 'subjectIds',
      type : 'auto',

   }, {
      name : 'phenotype',
      type : 'auto',
      sortType : function(value) {
         return value.name;
      },
   } ],

   storeId : 'subjectPhenotypes',

} );