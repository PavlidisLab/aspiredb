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
    bodyStyle: 'background: #D1E88D;',
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
        
        //patrick needed this for a phenotype change, possible we could get rid of this and just use what is in the project filter
        config.activeProjectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds();
        
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
