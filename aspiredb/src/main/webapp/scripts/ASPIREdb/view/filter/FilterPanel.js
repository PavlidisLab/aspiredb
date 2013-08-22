Ext.require([
    'Ext.layout.container.*'
]);

Ext.define('ASPIREdb.view.filter.FilterPanel', {
    extend: 'Ext.Panel',
    closable: true,
    collapsible: true,
    width: 800,
    layout: {
        type: 'vbox',
        align: 'stretch'
    }
});
