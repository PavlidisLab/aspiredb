Ext.require([ 'Ext.data.ArrayStore' ]);

Ext.define('ASPIREdb.store.PhenotypeStore', {
    extend: 'Ext.data.ArrayStore',
    alias: 'store.phenotypeStore',
        
    fields: [
             {name: 'name', type: 'string'},
             {name: 'value',  type: 'string'}
         ],
	
    storeId: 'phenotypes'
    
    
});