Ext.require([
    'Ext.data.Store',
    'ASPIREdb.model.Subject',
    'ASPIREdb.ActiveProjectSettings'
]);

Ext.define('ASPIREdb.store.SubjectStore', {
    extend: 'Ext.data.ArrayStore',
    alias: 'store.subjectStore',
    autoLoad: true,
    autoSync: true,
    proxy: {
    	type: 'localstorage',
    	id: 'subjectId'
    },   
	
    fields : [ {
		name : 'id',
		type : 'int'
	}, {
		name : 'patientId',
		type : 'string'
	}, {
		name : 'labelIds',
		type : 'array'
	} ],
	
    storeId: 'subjectStore'
    
});