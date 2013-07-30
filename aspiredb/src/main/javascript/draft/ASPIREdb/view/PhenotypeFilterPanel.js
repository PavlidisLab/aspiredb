Ext.require([
    'Ext.layout.container.*',
    'ASPIREdb.view.AndFilterContainer',
    'ASPIREdb.view.OrFilterContainer',
    'ASPIREdb.common.FilterPanel'
]);

Ext.define('ASPIREdb.view.PhenotypeFilterPanel', {
    extend: 'ASPIREdb.common.FilterPanel',
    alias: 'widget.filter_phenotype',
    title: 'Phenotype Filter',
    bodyStyle: 'background: #C3FDB8;',
    items: {
        xtype: 'filter_and',
        itemId: 'phenotypeFilterContainer',
        filterItemType: 'ASPIREdb.view.PropertyFilter'
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
