Ext.require([
    'Ext.layout.container.*',
    'ASPIREdb.view.filter.AndFilterContainer',
    'ASPIREdb.view.filter.OrFilterContainer',
    'ASPIREdb.view.filter.FilterPanel'
]);

Ext.define('ASPIREdb.view.filter.VariantFilterPanel', {
    extend: 'ASPIREdb.view.filter.FilterPanel',
    alias: 'widget.filter_variant',
    title: 'Variant Filter',
    bodyStyle: 'background: #FFFFD0;',
    items: [
        {
            xtype: 'filter_and',
            title: 'Variant Location:',
            itemId: 'locationFilterContainer',
            filterItemType: 'ASPIREdb.view.filter.OrFilterContainer',
            propertyStore: {
                proxy : {
                    type: 'dwr',
                    dwrFunction : VariantService.suggestVariantLocationProperties,
                    model: 'ASPIREdb.model.Property',
                    reader : {
                        type: 'json',
                        root: 'data',
                        totalProperty: 'count'
                    }
                }
            }
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
                propertyStore: {
                    proxy : {
                        type: 'dwr',
                        dwrFunction : VariantService.suggestProperties2,
                        dwrParams: ['CNV'],
                        model: 'ASPIREdb.model.Property',
                        reader : {
                            type: 'json',
                            root: 'data',
                            totalProperty: 'count'
                        }
                    }
                },
                filterItemType: 'ASPIREdb.view.filter.PropertyFilter'
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
                propertyStore: {
                    proxy : {
                        type: 'dwr',
                        dwrFunction : VariantService.suggestProperties2,
                        dwrParams: ['INDEL'],
                        model: 'ASPIREdb.model.Property',
                        reader : {
                            type: 'json',
                            root: 'data',
                            totalProperty: 'count'
                        }
                    }
                },
                filterItemType: 'ASPIREdb.view.filter.PropertyFilter'
            }
        }
    ],

    initComponent: function() {
        this.callParent();
    }

});
