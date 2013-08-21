Ext.require([
    'Ext.layout.container.*',
    'ASPIREdb.view.filter.multicombo.MultiValueCombobox',
    'ASPIREdb.model.Operator'
]);

Ext.define('ASPIREdb.view.filter.PropertyFilter', {
    extend: 'Ext.Container',
    alias: 'widget.filter_property',
    width: 690,
    ref: 'widgetContainer',
    layout: {
        type: 'hbox'
    },
    config: {
        propertyStore: null   /* property suggestions */
    },

    /**
     * @private
     * @param obj
     */
    pickSuggestionFunction: function (obj) {
        if (obj instanceof GeneProperty) {
            return function (prefix, callback) {
                VariantService.suggestValues(
                    {
                        $dwrClassName: 'GeneProperty'
                    }, {
                        valuePrefix: prefix,
                        $dwrClassName: 'SuggestionContext'
                    }, callback
                );
            }
        }
    },

    initComponent: function () {
        var me = this;
        this.items = [
            {
                xtype: 'combo',
                itemId: 'propertyComboBox',
                store: me.getPropertyStore(),
                displayField: 'displayName'
            },
            {
                xtype: 'combo',
                itemId: 'operatorComboBox',
                displayField: 'displayLabel',
                queryMode: 'local',
                store: {
                    proxy: {
                        type:'memory'
                    },
                    model:'ASPIREdb.model.Operator'
                }
            },
            {
                /* multi value vs single value  */
                xtype: 'container',
                itemId: 'multicombo_container',
                layout: {
                    type: 'vbox'
                },
                items: [
                    {
                        xtype: 'multivalue_combo',
                        itemId: 'multicombo',
                        width: 400,
                        height: 20
                    },
                    {
                        xtype: 'label',
                        itemId: 'example',
                        style: {
                            'padding-top': '5px',
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
        ];

        this.callParent();
        var multicombo_container = me.getComponent("multicombo_container");
        var operatorComboBox = me.getComponent("operatorComboBox");
        var multicombo = multicombo_container.getComponent("multicombo");
        var example = multicombo_container.getComponent("example");

        me.getComponent("propertyComboBox").on('select',
            function(obj, records) {
                // update examples
                var queryExample = records[0].data.exampleValues;
                example.setText(queryExample, false);

                // update operators
                var operators = records[0].data.operators;
                var store = operatorComboBox.getStore();
                store.removeAll();
                store.add( operators );

                // update multicombobox

            }
        );

        me.getComponent("removeButton").on('click', function (button, event) {
            // TODO: fix with custom events
            var item = button.ownerCt;
            var filterContainer = item.ownerCt;
            filterContainer.remove(item);
            filterContainer.doLayout();
        });
    }
});
