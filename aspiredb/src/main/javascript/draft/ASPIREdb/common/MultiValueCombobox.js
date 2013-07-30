Ext.require(['Ext.Component',
             'Ext.form.field.Text'
]);


Ext.define('ASPIREdb.common.MultiValueCombobox', {
    extend: 'Ext.Component',
    alias: 'widget.multivalue_combo',
    html: '<ul class="multiValueSuggestBox-list"></ul>',

    width: 300,
    height: 20,

    addItem: function(item) {

    },

    initComponent: function() {

    }

//    autoEl: {
//        tag: 'input',
//        type: 'text'
//    },

//    initComponent: function() {
//        this.callParent();
//        this.on('render', function(component) {
//            var element = component.getEl();
//            var dom = Ext.getDom(element);
//            $(dom).tokenInput([
//                {"id":"8","name":"House"},
//                {"id":"15","name":"WAAAAAAH"}
//            ], {theme:'facebook'});
//        });
//    }

});
