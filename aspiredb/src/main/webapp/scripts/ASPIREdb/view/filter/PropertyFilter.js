Ext.require([
    'Ext.layout.container.*',
    'ASPIREdb.view.filter.multicombo.MultiValueCombobox',
    'ASPIREdb.model.Operator'
]);

Ext.define('ASPIREdb.view.filter.PropertyFilter', {
    extend: 'Ext.Container',
    alias: 'widget.filter_property',
    width: 690,
    layout: {
        type: 'hbox'
    },
    config: {
        propertyStore: null, /* property suggestions */
        suggestValuesRemoteFunction: null
    },

    isMultiValue: true,
    selectedProperty: null,

    getRestrictionExpression: function() {
        var propertyComboBox = this.getComponent("propertyComboBox");
        var operatorComboBox = this.getComponent("operatorComboBox");
        var multicombo_container = this.getComponent("multicombo_container");
        var multicombo = multicombo_container.getComponent("multicombo");
        var singleValueField = multicombo_container.getComponent("singleValueField");

        if (this.isMultiValue) {
            var setRestriction = new SetRestriction();
            setRestriction.property = this.selectedProperty;
            setRestriction.operator = operatorComboBox.getValue();
            setRestriction.values = multicombo.getValues();
            return setRestriction;
        } else {
            var simpleRestriction = new SimpleRestriction();
            simpleRestriction.property = this.selectedProperty;
            simpleRestriction.operator = operatorComboBox.getValue();
            var value = new NumericValue();
            value.value = singleValueField.getValue();
            simpleRestriction.value = value;
            return simpleRestriction;
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
                        height: 20,
                        suggestValuesRemoteFunction: me.getSuggestValuesRemoteFunction()
                    },
                    {
                        xtype: 'textfield',
                        itemId: 'singleValueField',
                        width: 400,
                        height: 20,
                        hidden: true
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
        var singleValueField = multicombo_container.getComponent("singleValueField");
        var example = multicombo_container.getComponent("example");

        me.getComponent("propertyComboBox").on('select',
            function(obj, records) {
                var record = records[0];

                // update examples
                var queryExample = record.data.exampleValues;
                example.setText(queryExample, false);

                // update operators
                var operators = record.data.operators;
                var operatorModels = Ext.Array.map ( operators, function(x) {
                    return {displayLabel: x, operator: x};
                });
                var store = operatorComboBox.getStore();
                store.removeAll();
                store.add( operatorModels );

                var property = record.raw;
                if (property.dataType instanceof NumericalDataType) {
                    me.isMultiValue = false;
                    multicombo.hide();
                    singleValueField.reset();
                    singleValueField.show();
                } else {
                    me.isMultiValue = true;
                    // update multicombobox
                    multicombo.setProperty(property);
                    multicombo.reset();
                    multicombo.show();
                    singleValueField.hide();
                }

                me.selectedProperty = property;
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
