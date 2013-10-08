Ext.require([ 'Ext.data.ArrayStore' ]);

Ext.define('ASPIREdb.store.PhenotypeStore', {
    extend: 'Ext.data.ArrayStore',
    alias: 'store.phenotypeStore',
        
    fields: [
             {name: 'name', type: 'string'},
             
          // a PhenotypeSummary object
             {
            	 name: 'selectedPhenotype', 
            	 type: 'auto', 
            	 sortType : function(value) {
		     			var phenSummary = value.selectedPhenotype;
		    			if (phenSummary == null) return -1;
		    			return phenSummary.dbValue;
		    		},
		    }, 
		    
             {name: 'value',  type: 'string'}
         ],
	
    storeId: 'phenotypes'
    
    
});