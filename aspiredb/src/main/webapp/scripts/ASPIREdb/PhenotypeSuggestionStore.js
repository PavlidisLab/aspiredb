Ext.require([
    'Ext.data.Store',
    'ASPIREdb.model.PropertyValue'
]);

Ext.define('ASPIREdb.PhenotypeSuggestionStore', {
    extend:'Ext.data.Store',
    model: 'ASPIREdb.model.PhenotypeProperty',

    suggestionContext: null,

    constructor: function (config) {
        config.proxy = {
            type: 'dwr',
            dwrFunction: PhenotypeService.suggestPhenotypes,
            reader: {
                type: 'json',
                root: 'data',
                totalProperty: 'count'
            }
        };
        this.callParent(arguments);
        this.setActiveProjectIds([1]);
    },

    setActiveProjectIds: function(activeProjectIds) {
        this.suggestionContext = new SuggestionContext();
        this.suggestionContext.activeProjectIds = activeProjectIds;
    },

    load: function(options) {
        this.suggestionContext.valuePrefix = options.params.query;
        this.proxy.dwrParams = [this.suggestionContext];
        this.callParent(options);
    }
});