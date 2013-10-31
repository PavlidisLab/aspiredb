Ext.require([
    'Ext.layout.container.*',
    'ASPIREdb.view.filter.AndFilterContainer',
    'ASPIREdb.view.filter.OrPhenotypeFilterContainer',
    'ASPIREdb.view.filter.FilterPanel'
]);

Ext.define('ASPIREdb.view.filter.PhenotypeFilterPanel', {
    extend: 'ASPIREdb.view.filter.FilterPanel',
    alias: 'widget.filter_phenotype',
    title: 'Phenotype Filter',
    bodyStyle: 'background: #C3FDB8;',
    items: {
        //xtype: 'filter_and',
        //itemId: 'phenotypeFilterContainer',
        //filterItemType: 'ASPIREdb.view.filter.PhenotypeFilter'
    	
    	xtype : 'filter_and',
		
		itemId : 'phenotypeFilterContainer',
		filterItemType : 'ASPIREdb.view.filter.OrPhenotypeFilterContainer'
    },

    getFilterConfig: function() {
        var config = new PhenotypeFilterConfig();
        var phenotypeFilterContainer = this.getComponent('phenotypeFilterContainer');
        config.restriction = phenotypeFilterContainer.getRestrictionExpression();
        return config;
    },
    
    setFilterConfig: function(config) {
        
        var phenotypeFilterContainer = this.down('#phenotypeFilterContainer');
        phenotypeFilterContainer.setRestrictionExpression(config.restriction);
        
    },

    initComponent: function () {
        this.callParent();
    }

});
