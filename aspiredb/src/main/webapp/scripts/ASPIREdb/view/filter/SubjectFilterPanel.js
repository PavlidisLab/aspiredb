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
        filterItemType: 'ASPIREdb.view.filter.PropertyFilter',
        suggestValuesRemoteFunction: SubjectServiceOld.suggestValues,
        propertyStore: {
            proxy : {
                type: 'dwr',
                dwrFunction : SubjectServiceOld.suggestProperties,
                model: 'ASPIREdb.model.Property',
                reader : {
                    type: 'json',
                    root: 'data',
                    totalProperty: 'count'
                }
            }
        }
    },

    getFilterConfig: function() {
        var config = new SubjectFilterConfig();
        var subjectFilterContainer = this.getComponent('subjectFilterContainer');
        config.restriction = subjectFilterContainer.getRestrictionExpression();
        return config;
    },

    initComponent: function () {
        this.callParent();
    }
});
