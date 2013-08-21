Ext.require([
    'Ext.layout.container.*',
    'ASPIREdb.view.filter.PropertyFilter'
]);

Ext.define('ASPIREdb.view.filter.OrFilterContainer', {
    extend: 'Ext.Panel',
    alias: 'widget.filter_or',
    closable: true,
    title: 'OR Filter Container',
    layout: {
        type: 'vbox'
    },
    config: {
        propertyStore: null
    },
/*
    border: 1,
    style: {
        border: "1px solid lightgray"
    },
*/
    initComponent: function() {
        var me = this;
        this.items = [ {
            xtype: 'container',
            itemId: 'filterContainer',
            layout: {
                type: 'vbox',
                defaultMargins: {
                    top: 5,
                    right: 5,
                    left: 5,
                    bottom: 5
                }
            },
            items: [
                {
                    xtype: 'filter_property',
                    propertyStore: this.getPropertyStore()
                }
            ]
        }, {
            xtype: 'button',
            itemId: 'addButton',
            text: 'OR'
        }
        ];

        this.callParent();

        me.getComponent("addButton").on('click', function (button, event) {
            var filterContainer = me.getComponent("filterContainer");
            filterContainer.add(Ext.create('ASPIREdb.view.filter.PropertyFilter'));
            filterContainer.doLayout();
        });
    }
});
