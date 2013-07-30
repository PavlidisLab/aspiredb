Ext.require([
    'Ext.layout.container.*',
    'ASPIREdb.view.AndFilterContainer',
    'ASPIREdb.view.OrFilterContainer',
    'ASPIREdb.common.FilterPanel'
]);

Ext.define('ASPIREdb.view.VariantFilterPanel', {
    extend: 'ASPIREdb.common.FilterPanel',
    alias: 'widget.filter_variant',
    title: 'Variant Filter',
    bodyStyle: 'background: #FFFFD0;',
    items: [
        {
            xtype: 'filter_and',
            title: 'Variant Location:',
            itemId: 'locationFilterContainer',
            filterItemType: 'ASPIREdb.view.OrFilterContainer'
        },
        {
            xtype: 'label',
            text: 'Variant characteristics:'
        },
        {
            xtype: 'panel',
            bodyStyle: 'background: #FFFFD0;',
            title: 'CNV:',
            collapsible: true,
            collapsed: true,
            animCollapse: false,
            items: {
                xtype: 'filter_and',
                itemId: 'cnvCharacteristicFilterContainer',
                filterItemType: 'ASPIREdb.view.PropertyFilter'
            }
        },
        {
            xtype: 'panel',
            bodyStyle: 'background: #FFFFD0;',
            title: 'Indel:',
            collapsible: true,
            collapsed: true,
            animCollapse: false,
            items: {
                xtype: 'filter_and',
                itemId: 'indelCharacteristicFilterContainer',
                filterItemType: 'ASPIREdb.view.PropertyFilter'
            }
        }
    ],

    initComponent: function() {
        this.callParent();
//        this.getComponent("locationFilterContainer").add(Ext.create('ASPIREdb.view.OrFilterContainer'));
    }

});
