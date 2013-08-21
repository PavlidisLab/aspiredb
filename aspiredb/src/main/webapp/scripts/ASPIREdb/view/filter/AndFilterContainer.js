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
    config: {
        propertyStore: null,
        filterItemType: null
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
            },
            getRestrictionExpression: function () {
                var conjunction = new Conjunction();
                conjunction.restrictions = [];
                this.items.each(function(item, index, length) {
                    conjunction.restrictions.push(item.getRestrictionExpression());
                });
                return conjunction;
            }
        },
        {
            xtype: 'button',
            itemId: 'addButton',
            text: 'AND'
        }
    ],

    getRestrictionExpression: function () {
        var filterContainer = this.getComponent('filterContainer');
        return filterContainer.getRestrictionExpression();
    },

    getNewItem: function () {
        return Ext.create(this.getFilterItemType(),{propertyStore: this.getPropertyStore()});
    },

    initComponent: function () {
        this.callParent();

        var me = this;
        var filterContainer = this.getComponent("filterContainer");

        // Add first item.
        filterContainer.insert(0, this.getNewItem());

        // Attach button listener
        me.getComponent("addButton").on('click', function (button, event) {
            filterContainer.add(me.getNewItem());
            filterContainer.doLayout();
        });
    }

});
