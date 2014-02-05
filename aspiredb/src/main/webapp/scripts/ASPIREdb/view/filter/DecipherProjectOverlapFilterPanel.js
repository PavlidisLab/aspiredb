Ext.require([
    'Ext.layout.container.*',
    'ASPIREdb.view.filter.AndFilterContainer',
    'ASPIREdb.view.filter.OrFilterContainer',
    'ASPIREdb.view.filter.FilterPanel',
    'ASPIREdb.view.filter.PhenotypeFilter',
    'ASPIREdb.view.filter.DecipherProjectOverlapFilterContainer'
]);

Ext.define('ASPIREdb.view.filter.DecipherProjectOverlapFilterPanel', {
    extend: 'ASPIREdb.view.filter.FilterPanel',
    alias: 'widget.filter_decipherprojectoverlappanel',
    title: 'DECIPHER Overlap Filter',
    bodyStyle: 'background: #996633;',
    items: [{
		xtype : 'filter_decipherprojectoverlap',
		itemId : 'decipherProjectOverlapFilterContainer'
	}
    ],

    getFilterConfig: function() {
    	//TODO fix this
        var projectOverlapFilterContainer = this.getComponent('decipherProjectOverlapFilterContainer');
        return  projectOverlapFilterContainer.getRestrictionExpression();        
    },
    
    setFilterConfig: function(config) {
        
        var projectOverlapFilterContainer = this.down('#decipherProjectOverlapFilterContainer');
        projectOverlapFilterContainer.setRestrictionExpression(config.restriction);
        
    },
    
    handleCloseImageClick: function(){
    	this.close();
    },

    initComponent: function () {
        this.callParent();
    }
});
