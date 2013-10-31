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
    items: [{
        xtype: 'filter_and',
        itemId: 'subjectFilterContainer',
        filterItemType: 'ASPIREdb.view.filter.PropertyFilter',
        suggestValuesRemoteFunction: SubjectService.suggestValues,
        propertyStore: {
            autoLoad: false,
            proxy : {
                type: 'dwr',
                dwrFunction : SubjectService.suggestProperties,
                model: 'ASPIREdb.model.Property',
                reader : {
                    type: 'json',
                    root: 'data',
                    totalProperty: 'count'
                }
            }
        }}
    ],

    getFilterConfig: function() {
        var config = new SubjectFilterConfig();
        var subjectFilterContainer = this.getComponent('subjectFilterContainer');
        config.restriction = subjectFilterContainer.getRestrictionExpression();
        return config;
    },
    
    setFilterConfig: function(config) {
        
        var subjectFilterContainer = this.down('#subjectFilterContainer');
        subjectFilterContainer.setRestrictionExpression(config.restriction);
        
    },
    
    handleCloseImageClick: function(){
    	this.close();
    },

    initComponent: function () {
        this.callParent();
        
       
        
    }
});
