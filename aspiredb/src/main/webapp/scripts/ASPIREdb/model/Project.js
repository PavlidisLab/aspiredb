Ext.require([
    'Ext.data.Model'
]);

Ext.define('ASPIREdb.model.Project', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id',   type: 'number'},
        {name: 'name',  type: 'string'},
        {name: 'description',  type: 'string'}
    ]
});