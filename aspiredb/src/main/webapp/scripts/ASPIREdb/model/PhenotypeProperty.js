Ext.define('ASPIREdb.model.PhenotypeProperty', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'displayName',  type: 'string'},
        {name: 'name',   type: 'string'},
        {name: 'existInDatabase', type: 'boolean'}
    ]
});