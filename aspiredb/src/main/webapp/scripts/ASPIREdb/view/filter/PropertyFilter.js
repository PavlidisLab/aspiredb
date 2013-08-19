Ext.require([
    'Ext.layout.container.*',
    'ASPIREdb.view.filter.multicombo.MultiValueCombobox'
]);

Ext.define('ASPIREdb.view.filter.PropertyFilter', {
    extend: 'Ext.Container',
    alias: 'widget.filter_property',
    width: 690,
    ref: 'widgetContainer',
    layout: {
        type: 'hbox'
    },
    filterProperties: [],
    items: [
        {
            xtype: 'combo',
            itemId: 'propertyComboBox',
            store: this.filterProperties
        },
        {
            xtype: 'combo',
            itemId: 'operatorComboBox',
            store: [
                {value:'1',text: 'AAAAA'},
                {value:'2',text: 'BBBB'}
            ]

        },
        {
            /* multi value vs single value  */
            xtype: 'container',
            layout: {
                type: 'vbox'
            },
            items: [
                {
                    xtype: 'multivalue_combo',
                    width: 400,
                    height: 20
                },
                {
                    xtype: 'label',
                    text: "Example stuff",
                    style: {
                        'font-size': 'smaller',
                        'color': 'gray'
                    }
                }
            ]
        },
        {
            xtype: 'button',
            itemId: 'removeButton',
            text: 'X'
        }
    ],

    initComponent: function () {
        this.callParent();

        // List of allowed *properties* is set at construction.
        // TODO: initialize the rest of the combo boxes depending on that one.

        var me = this;

        me.getComponent("removeButton").on('click', function (button, event) {
            // TODO: fix with custom events
            var item = button.ownerCt;
            var filterContainer = item.ownerCt;
            filterContainer.remove(item);
            filterContainer.doLayout();
        });
    }

});
