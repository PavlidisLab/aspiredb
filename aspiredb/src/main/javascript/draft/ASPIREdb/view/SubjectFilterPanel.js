Ext.require([
    'Ext.layout.container.*',
    'ASPIREdb.view.AndFilterContainer',
    'ASPIREdb.view.OrFilterContainer',
    'ASPIREdb.common.FilterPanel'
]);

Ext.define('ASPIREdb.view.SubjectFilterPanel', {
    extend: 'ASPIREdb.common.FilterPanel',
    alias: 'widget.filter_subject',
    title: 'Subject Filter',
    bodyStyle: 'background: #FFE5B4;',
    items: {
        xtype: 'filter_and',
        itemId: 'subjectFilterContainer',
        filterItemType: 'ASPIREdb.view.PropertyFilter'
    },

    initComponent: function () {
        this.callParent();
        var filterContainer = this.getComponent('subjectFilterContainer');


    }

});
