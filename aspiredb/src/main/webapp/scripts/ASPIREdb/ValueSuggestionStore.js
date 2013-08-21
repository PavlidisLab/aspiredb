Ext.require([
    'Ext.data.Store',
    'ASPIREdb.model.PropertyValue'
]);


Ext.define('ASPIREdb.ValueSuggestionStore', {
    extend:'Ext.data.Store',
    proxy: {
        type:'dwr',
        dwrFunction: VariantService.suggestValues,
        reader : {
            type: 'json',
            root: 'data',
            totalProperty: 'count'
        }
    },
    model: 'ASPIREdb.model.PropertyValue',

    suggestionContext: null,
    property: null,

    setActiveProjectIds: function(activeProjectIds) {
        this.suggestionContext = new SuggestionContext();
        this.suggestionContext.activeProjectIds = activeProjectIds;
    },

    setProperty: function(propertyObj) {
        this.property = propertyObj;
    },

    load: function(options) {
        this.suggestionContext.valuePrefix = options.params.query;
        this.proxy.dwrParams = [this.property, this.suggestionContext];
        this.callParent(options);
    }
});