Ext.require([
    'Ext.panel.Panel',
    'ASPIREdb.view.Ideogram',
    'ASPIREdb.view.subject.SubjectGrid'
]);

Ext.define('ASPIREdb.MainPanel',{
    extend: 'Ext.panel.Panel',
    alias: 'widget.ASPIREdb_mainpanel',
    layout: 'border',
    items:[
        {
            region: 'west',
            xtype:'subjectGrid',
            width: 300,
            collapsible: true,
            split: true,
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
            collapsible: true,
            split: true,
            title:'Phenotype'
        }
    ]
});