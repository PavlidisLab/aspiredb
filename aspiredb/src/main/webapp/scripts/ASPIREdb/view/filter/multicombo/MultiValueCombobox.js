Ext.require([
    'Ext.Component',
    'Ext.form.field.Text',
    'ASPIREdb.view.filter.multicombo.Item',
    'ASPIREdb.model.Property',
    'ASPIREdb.model.PropertyValue',
    'ASPIREdb.ValueSuggestionStore'
]);

Ext.define('ASPIREdb.view.filter.multicombo.MultiValueCombobox', {
    extend: 'Ext.Container',
    alias: 'widget.multivalue_combo',
    layout: 'column',
    autoEl: {
      tag:  'ul'
    },
    cls: 'multiValueSuggestBox-list',
    width: 200,
    config: {
        suggestValuesRemoteFunction: null
    },

    setProperty: function(propertyObj) {
        var comboBox = this.getComponent('invisibleCombo');

        var store = comboBox.getStore();
        store.setProperty(propertyObj);
    },

    /**
     * @returns {Array}
     */
    getValues: function() {
        var values = [];
        this.items.each(function(item){
            if (item instanceof ASPIREdb.view.filter.multicombo.Item) {
                values.push(item.getValue());
            }
        });
        return values;
    },

    reset: function() {

        this.items = this.items.filterBy(function(item){
            if (item instanceof ASPIREdb.view.filter.multicombo.Item) {
                item.destroy();
                return false;
            }
            return true;
        });

        this.doLayout();

        var comboBox = this.getComponent('invisibleCombo');
        comboBox.lastQuery = null;
    },

    /**
     * @private
     * @param item
     */
    addItem: function(item) {
        var itemElement = Ext.create('ASPIREdb.view.filter.multicombo.Item',
            {
                text: item.data.displayValue,
                value: item.raw.value
            }
        );
        itemElement.on('remove', function(itemToRemove) {
            this.items.remove(itemToRemove);
            itemToRemove.destroy();
        },this);

        var comboBox = this.getComponent('invisibleCombo');
        var items = this.items;
        items.insert(items.getCount() - 1, itemElement);
        comboBox.clearValue();
        this.doLayout();
    },

    /**
     * @private
     */
    removeItem: function () {
        if (this.items.getCount() > 1) {
            // second before last
            var item = this.items.removeAt(this.items.getCount() - 2);
            item.destroy();
            this.doLayout();
        }
    },

    initComponent: function() {
        this.items = [
            {
                xtype: 'combo',
                itemId:'invisibleCombo',
                width: 100,
                minChars: 0,
                matchFieldWidth: false,
                hideTrigger: true,
                cls: 'multiValueSuggestBox-list-input',
                triggerAction: 'query',
                autoSelect: true,
                enableKeyEvents: true,
                displayField: 'displayValue',
                store: Ext.create('ASPIREdb.ValueSuggestionStore',{
                    remoteFunction: this.getSuggestValuesRemoteFunction()
                }),
                listConfig: {
                    loadingText: 'Searching...',
                    emptyText: 'No results found.'
                }
            }
        ];

        this.callParent();

        var multiCombo = this;
        var comboBox = this.getComponent('invisibleCombo');

        comboBox.on('keydown', function(obj, event) {
            if (event.getKey() === event.BACKSPACE) {
                if (comboBox.getRawValue() === "") {
                    multiCombo.removeItem();
                    comboBox.collapse();
                }
            }
        });

        comboBox.on('select', function(obj, records) {
            multiCombo.addItem(records[0]);
        });

        this.on('afterrender', function() {
            this.getEl().on('click', function() {
                comboBox.focus();
            });
        }, this);
    }
});
