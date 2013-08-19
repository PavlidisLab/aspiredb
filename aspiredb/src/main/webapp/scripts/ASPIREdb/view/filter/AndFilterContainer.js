Ext.require([
    'Ext.layout.container.*',
    'ASPIREdb.view.filter.OrFilterContainer'
]);

Ext.define('ASPIREdb.view.filter.AndFilterContainer', {
    extend: 'Ext.Container',
    alias: 'widget.filter_and',
    layout: {
        type: 'vbox'
    },
    items: [
        {
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
            }
        },
        {
            xtype: 'button',
            itemId: 'addButton',
            text: 'AND'
        }
    ],

    filterItemType: null,

    getNewItem: function () {
        return Ext.create(this.filterItemType);
    },

    initComponent: function () {
        this.callParent();

        var me = this;
        var filterContainer = this.getComponent("filterContainer");

        // Add first item.
        this.insert(0, this.getNewItem());

        // Attach button listener
        me.getComponent("addButton").on('click', function (button, event) {
            filterContainer.add(me.getNewItem());
            filterContainer.doLayout();
        });
    }

});
