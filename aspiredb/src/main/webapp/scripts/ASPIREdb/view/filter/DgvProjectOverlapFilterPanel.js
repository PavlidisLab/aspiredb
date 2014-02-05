Ext.require([
    'Ext.layout.container.*',
    'ASPIREdb.view.filter.AndFilterContainer',
    'ASPIREdb.view.filter.OrFilterContainer',
    'ASPIREdb.view.filter.FilterPanel',
    'ASPIREdb.view.filter.PhenotypeFilter',
    'ASPIREdb.view.filter.DgvProjectOverlapFilterContainer'
]);

Ext.define('ASPIREdb.view.filter.DgvProjectOverlapFilterPanel', {
    extend: 'ASPIREdb.view.filter.FilterPanel',
    alias: 'widget.filter_dgvprojectoverlappanel',
    title: 'DGV Overlap Filter',
    bodyStyle: 'background: #A3C1DD;',
    items: [{
		xtype : 'filter_dgvprojectoverlap',
		itemId : 'dgvProjectOverlapFilterContainer'
	}
    ],

    getFilterConfig: function() {
    	//TODO fix this
        var projectOverlapFilterContainer = this.getComponent('dgvProjectOverlapFilterContainer');
        return  projectOverlapFilterContainer.getRestrictionExpression();        
    },
    
    setFilterConfig: function(config) {
        
        var projectOverlapFilterContainer = this.down('#dgvProjectOverlapFilterContainer');
        projectOverlapFilterContainer.setRestrictionExpression(config.restriction);
        
    },
    
    handleCloseImageClick: function(){
    	this.close();
    },

    initComponent: function () {
        this.callParent();
    }
});
