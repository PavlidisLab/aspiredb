Ext.require([
    'Ext.layout.container.*'
]);

Ext.define('ASPIREdb.common.FilterPanel', {
    extend: 'Ext.Panel',
    closable: true,
    collapsible: true,
    width: 700,
    layout: {
        type: 'vbox',
        align: 'stretch'
    }
});
