Ext.require(['Ext.Component',
             'Ext.form.field.Text'
]);


Ext.define('ASPIREdb.common.MultiValueCombobox', {
    extend: 'Ext.Container',
    alias: 'widget.multivalue_combo',
    layout: 'column',
    autoEl:{
      tag:  'ul'
    },
    cls: 'multiValueSuggestBox-list',
    width: 300,
   // height: 20,

    addItem: function(item) {
    },

    initComponent: function() {
        this.callParent();
        this.items.add(new Ext.Container(
            {
                autoEl: 'li',
                cls: 'multiValueSuggestBox-token',
                resizable: false,
                items: [
                    {
                        xtype:'component',
                        autoEl: {
                            tag:'p',
                            html:'meow'
                        },
                        cls: 'multiValueSuggestBox-token-label'
                    },
                    {
                        xtype:'component',
                        autoEl: {
                            tag:'span',
                            html:'x'
                        },
                        cls: 'multiValueSuggestBox-token-close'
                    }
                ]
            }
        ));

        this.items.add(new Ext.form.ComboBox(
            {
                hideTrigger: true,
                cls: 'multiValueSuggestBox-list-input'
        }));


    }
});
