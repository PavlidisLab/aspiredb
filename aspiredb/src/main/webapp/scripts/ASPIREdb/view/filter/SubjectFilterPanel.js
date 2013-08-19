Ext.require([
    'Ext.layout.container.*',
    'ASPIREdb.view.filter.AndFilterContainer',
    'ASPIREdb.view.filter.OrFilterContainer',
    'ASPIREdb.view.filter.FilterPanel'
]);

Ext.define('ASPIREdb.view.filter.SubjectFilterPanel', {
    extend: 'ASPIREdb.view.filter.FilterPanel',
    alias: 'widget.filter_subject',
    title: 'Subject Filter',
    bodyStyle: 'background: #FFE5B4;',
    items: {
        xtype: 'filter_and',
        itemId: 'subjectFilterContainer',
        filterItemType: 'ASPIREdb.view.filter.PropertyFilter'
    },

    initComponent: function () {
        this.callParent();
        var filterContainer = this.getComponent('subjectFilterContainer');


    }

});
