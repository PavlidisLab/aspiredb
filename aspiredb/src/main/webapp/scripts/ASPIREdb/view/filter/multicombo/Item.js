Ext.require([
    'Ext.container.Container',
    'Ext.Component'
]);

//TODO: use tpl to render, extend Component?
Ext.define('ASPIREdb.view.filter.multicombo.Item', {
    config: {
        text : null,
        value: null
    },
    extend: 'Ext.container.Container',
    autoEl: 'li',
    cls: 'multiValueSuggestBox-token',
    resizable: false,

    initComponent: function() {
        this.items = [
            {
                xtype: 'component',
                autoEl: {
                    tag: 'p',
                    html: this.getText()
                },
                cls: 'multiValueSuggestBox-token-label'
            },
            {
                xtype: 'component',
                autoEl: {
                    tag: 'span',
                    html: 'x'
                },
                listeners: {
                    click : {
                        element:'el',
                        fn: function() {
                            this.fireEvent('remove', this);
                            //ASPIREdb.EVENT_BUS.fireEvent('query_update');
                        },
                        scope: this
                    }
                },
                cls: 'multiValueSuggestBox-token-close'
            }
        ];

        this.callParent();
    }
});