Ext.require([
    'Ext.layout.container.*',
    'ASPIREdb.view.filter.AndFilterContainer',
    'ASPIREdb.view.filter.OrFilterContainer',
    'ASPIREdb.view.filter.FilterPanel'
]);

Ext.define('ASPIREdb.view.filter.PhenotypeFilterPanel', {
    extend: 'ASPIREdb.view.filter.FilterPanel',
    alias: 'widget.filter_phenotype',
    title: 'Phenotype Filter',
    bodyStyle: 'background: #C3FDB8;',
    items: {
        xtype: 'filter_and',
        itemId: 'phenotypeFilterContainer',
        filterItemType: 'ASPIREdb.view.filter.PropertyFilter'
/*

        getNewItem: function () {
            return Ext.create('ASPIREdb.view.PropertyFilter');
        }
*/
    },

    initComponent: function () {
        this.callParent();
//        this.getComponent("locationFilterContainer").add(Ext.create('ASPIREdb.view.OrFilterContainer'));
    }

});
