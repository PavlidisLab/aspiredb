Ext.require([
    'Ext.panel.Panel',
    'ASPIREdb.view.GeneSetGrid',
    'ASPIREdb.view.GeneGrid', 
]);

Ext.define('ASPIREdb.GeneManagerPanel',{
    extend: 'Ext.panel.Panel',
    alias: 'widget.ASPIREdb_genemanagerpanel',
    layout: 'border',
    items:[
        {
            region: 'west',
            xtype:'geneSetGrid',
            width: 480,
            collapsible: true,
            split: true,
            title:'Gene Sets'
        },
        {
            region: 'east',
            xtype:'geneGrid',
            width: 480,
            collapsible: true,
            split: true,
            title:'Associated Genes'
        }
    ]
});