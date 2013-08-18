Ext.require([
    'Ext.Component',
    'Ext.form.field.Text',
    'ASPIREdb.common.multicombo.Item'
]);


Ext.define('ASPIREdb.common.MultiValueCombobox', {
    extend: 'Ext.Container',
    alias: 'widget.multivalue_combo',
    layout: 'column',
    autoEl:{
      tag:  'ul'
    },
    cls: 'multiValueSuggestBox-list',
    width: 200,

    addItem: function(item) {
        var comboBox = this.getComponent('invisibleCombo');
        var items = this.items;
        items.insert(items.getCount() - 1,
            Ext.create('ASPIREdb.common.multicombo.Item',
                {
                    text: item.raw[0]
                }
            )
        );
        comboBox.clearValue();
        this.doLayout();
    },

    removeItem: function () {
        if (this.items.getCount() > 1) {
            // second before last
            var item = this.items.removeAt(this.items.getCount() - 2);
            item.destroy();
            this.doLayout();
        }
    },

    initComponent: function() {
        this.callParent();
        var multiCombo = this;
        this.items.add(
            Ext.create('ASPIREdb.common.multicombo.Item',
                {
                    text:'meow'
                }
        ));

        var comboBox = new Ext.form.field.ComboBox({
            itemId:'invisibleCombo',
            hideTrigger: true,
            cls: 'multiValueSuggestBox-list-input',
            triggerAction: 'all',
            displayField: 'text',
            store: [
                'AAAAA',
                'BBBB'
            ],
            autoSelect: true,
            enableKeyEvents: true
        });

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


        this.items.add(comboBox);
    }
});
