Ext.require([
    'Ext.panel.Panel',
    'ASPIREdb.view.Ideogram',
    'ASPIREdb.view.SubjectGrid',
    'ASPIREdb.view.PhenotypeGrid',
    'ASPIREdb.view.VariantTabPanel'
]);
/**
 * Main panel contains grid Panels "subjectGrid", "variantTabPanel" and "phenotypeGrid'
 */
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
            xtype: 'variantTabPanel',
            title:'Variant'
        },
        {
            region: 'east',
            xtype:'phenotypeGrid',
            width: 700,
            collapsible: true,
            split: true,
            title:'Phenotype'
        }
    ]
});