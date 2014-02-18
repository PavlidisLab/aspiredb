Ext.require([
    'Ext.layout.container.*',
    'ASPIREdb.view.filter.AndFilterContainer',
    'ASPIREdb.view.filter.OrFilterContainer',
    'ASPIREdb.view.filter.FilterPanel',
    'ASPIREdb.view.filter.PhenotypeFilter',
    'ASPIREdb.view.filter.ProjectOverlapFilterContainer'
]);

Ext.define('ASPIREdb.view.filter.ProjectOverlapFilterPanel', {
    extend: 'ASPIREdb.view.filter.FilterPanel',
    alias: 'widget.filter_projectoverlappanel',
    title: 'Project Overlap Filter',
    bodyStyle: 'background: #FF9900;',
    items: [{
		xtype : 'filter_projectoverlap',
		itemId : 'projectOverlapFilterContainer'
	}
    ],

    getFilterConfig: function() {
    	//TODO fix this
        var projectOverlapFilterContainer = this.getComponent('projectOverlapFilterContainer');
        return  projectOverlapFilterContainer.getRestrictionExpression();        
    },
    
    setFilterConfig: function(config) {
        
        var projectOverlapFilterContainer = this.down('#projectOverlapFilterContainer');
        projectOverlapFilterContainer.setRestrictionExpression(config);
        
    },
    
    handleCloseImageClick: function(){
    	this.close();
    },

    initComponent: function () {
        this.callParent();
    }
});
