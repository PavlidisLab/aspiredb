Ext.require([
    'Ext.panel.Panel',
    'ASPIREdb.view.Ideogram'
]);

Ext.define('ASPIREdb.MainPanel',{
    extend: 'Ext.panel.Panel',
    alias: 'widget.ASPIREdb_mainpanel',
    layout: 'border',
    items:[
        {
            region: 'west',
            xtype:'panel',
            width: 300,
            title:'Subject'
        },
        {
            region: 'center',
            xtype: 'panel',
            title:'Variant'
        },
        {
            region: 'east',
            xtype:'panel',
            width: 700,
            title:'Phenotype'
        }
    ]
});