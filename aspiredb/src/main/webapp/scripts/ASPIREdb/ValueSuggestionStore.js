Ext.require([
    'Ext.data.Store',
    'ASPIREdb.model.PropertyValue'
]);


Ext.define('ASPIREdb.ValueSuggestionStore', {
    extend:'Ext.data.Store',
    model: 'ASPIREdb.model.PropertyValue',

    suggestionContext: null,
    property: null,

    constructor: function (config) {
        config.proxy = {
            type: 'dwr',
            dwrFunction: config.remoteFunction,
            reader: {
                type: 'json',
                root: 'data',
                totalProperty: 'count'
            }
        };
        this.callParent(arguments);
    },

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