Ext.require([
    'ASPIREdb.model.Operator'
]);

Ext.define('ASPIREdb.model.Property', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'displayName',  type: 'string'},
        {name: 'exampleValues',   type: 'string'},
        {name: 'operators', type: 'ASPIREdb.model.Operator'}
    ]
});