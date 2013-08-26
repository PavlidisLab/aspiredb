

Ext.define('ASPIREdb.view.filter.PhenotypeFilter', {
    extend: 'Ext.Container',
    layout: 'hbox',
    /**
     * @private
     * @override
     */
    initComponent: function() {
        this.items = [
            {
                xtype: 'combo',
                itemId: 'nameCombo',
                matchFieldWidth: false,
                triggerAction: 'query',
                autoSelect: true,
                hideTrigger: true,
                displayField: 'displayName',
                store: Ext.create('ASPIREdb.PhenotypeSuggestionStore',{
                    remoteFunction: PhenotypeService.suggestPhenotypes
                }),
                listConfig: {
                    loadingText: 'Searching...',
                    emptyText: 'No results found.'
                },
                listeners: {
                    select: {
                        fn: function(obj, records) {
                            var record = records[0];
                            var valueCombo = this.getComponent('valueCombo');
                            valueCombo.getStore().setProperty(record.raw);
                        },
                        scope: this
                    }
                }
            },
            {
                xtype: 'combo',
                itemId: 'valueCombo',
                displayField: 'displayValue',
                store: Ext.create('ASPIREdb.ValueSuggestionStore',{
                    remoteFunction: PhenotypeService.suggestPhenotypeValues
                }),
                listConfig: {
                    loadingText: 'Searching...',
                    emptyText: 'No results found.'
                }
            }
        ];

        this.callParent();
    },

    getRestrictionExpression: function() {
        var nameCombo = this.getComponent("nameCombo");
        var valueCombo = this.getComponent("valueCombo");

        var phenotypeRestriction = new PhenotypeRestriction();
        phenotypeRestriction.name = nameCombo.getValue();
        phenotypeRestriction.value = valueCombo.getValue();
        return phenotypeRestriction;
    }
});