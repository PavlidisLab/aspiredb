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
			var pvo = value.selectedPhenotype;
			if (pvo == null)
				return -1;
			return pvo.dbValue;
		},
	},
	{
		name : 'selectedPhenotypeMulti',
		type : 'auto',
		sortType : function(value) {
			var pvo = value.selectedPhenotypeMulti;
			if (pvo == null)
				return -1;
			return pvo.dbValue;
		},
	},
	{
		name : 'value',
		type : 'string'
	} ],

	storeId : 'phenotypes'

});