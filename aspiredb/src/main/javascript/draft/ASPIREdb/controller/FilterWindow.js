Ext.require([
    'ASPIREdb.view.VariantFilterPanel',
    'ASPIREdb.view.SubjectFilterPanel',
    'ASPIREdb.view.PhenotypeFilterPanel'
]);

Ext.define('ASPIREdb.controller.FilterWindow', {
    extend: 'Ext.app.Controller',

    views: [
        'FilterWindow'
    ],

    refs: [
        {
            ref: 'filterContainer',
            selector: 'filterwindow > #filterContainer'
        }, {
            ref: 'filterTypeComboBox',
            selector: '#filterTypeComboBox'
        }

    ],

    init: function() {
        this.control({
            '#filterTypeComboBox': {
                'select': function (combo, records, eOpts) {
                    var record = records[0];
                    this.getFilterContainer().add (
                        Ext.create(record.raw[0])
                    );
                    this.getFilterTypeComboBox().setValue('FILTER_PLACEHOLDER');
                }
            }
        });
    }
});