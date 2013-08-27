Ext.require([ 'Ext.data.Model' ]);

Ext.define('ASPIREdb.model.Subject', {
	extend : 'Ext.data.Model',
	fields : [ {
		name : 'id'
	}, {
		name : 'patientId'
	}, {
		name : 'label'
	} ],
});