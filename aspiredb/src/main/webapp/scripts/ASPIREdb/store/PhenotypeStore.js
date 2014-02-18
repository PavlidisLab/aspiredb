Ext.require([ 'Ext.data.ArrayStore' ]);

Ext.define('ASPIREdb.store.PhenotypeStore', {
	extend : 'Ext.data.ArrayStore',
	alias : 'store.phenotypeStore',

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
		name : 'selectedPhenotype',
		type : 'auto',
		sortType : function(value) {
			var phenSummary = value.selectedPhenotype;
			if (phenSummary == null)
				return -1;
			return phenSummary.dbValue;
		},
	},
	{
		name : 'selectedSubjectPhenotypes',
		type :'string'
	
	},

	{
		name : 'value',
		type : 'string'
	} ],

	storeId : 'phenotypes'

});