Ext.require([
    'Ext.data.Store',
    'ASPIREdb.model.PropertyValue',
    'ASPIREdb.ActiveProjectSettings'
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
    },

    load: function(options) {
        this.suggestionContext = new SuggestionContext();
        this.suggestionContext.activeProjectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds() ;
        this.suggestionContext.valuePrefix = options.params.query;
        this.proxy.dwrParams = [this.suggestionContext];
        this.callParent(options);
    }
});