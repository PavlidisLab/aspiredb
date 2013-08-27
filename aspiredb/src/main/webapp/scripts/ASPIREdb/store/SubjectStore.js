Ext.require([
    'Ext.data.Store',
    'ASPIREdb.model.Subject',
    'ASPIREdb.ActiveProjectSettings'
]);

Ext.define('ASPIREdb.store.SubjectStore', {
    extend: 'Ext.data.ArrayStore',
    alias: 'store.subjectStore',
    model: 'ASPIREdb.model.Subject',
    autoLoad: true,
    autoSync: true,
    proxy: {
    	type: 'localstorage',
    	id: 'subjectId'
    },   
	
    storeId: 'subjectStore'
    
});