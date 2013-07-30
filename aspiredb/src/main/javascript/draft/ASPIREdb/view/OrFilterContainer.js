Ext.require([
    'Ext.layout.container.*',
    'ASPIREdb.view.PropertyFilter'
]);

Ext.define('ASPIREdb.view.OrFilterContainer', {
    extend: 'Ext.Panel',
    alias: 'widget.filter_or',
    closable: true,
    title: 'OR Filter Container',
    layout: {
        type: 'vbox'
    },
/*
    border: 1,
    style: {
        border: "1px solid lightgray"
    },
*/
    items: [ {
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
                    xtype: 'filter_property'
                }
            ]
        }, {
            xtype: 'button',
            itemId: 'addButton',
            text: 'OR'
        }
    ],

    initComponent: function() {
        this.callParent();
        var me = this;
        me.getComponent("addButton").on('click', function (button, event) {
            var filterContainer = me.getComponent("filterContainer");
            filterContainer.add(Ext.create('ASPIREdb.view.PropertyFilter'));
            filterContainer.doLayout();
        });
    }
});
