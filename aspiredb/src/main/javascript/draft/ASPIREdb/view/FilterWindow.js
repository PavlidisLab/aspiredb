Ext.require([
    'Ext.window.*',
    'Ext.layout.container.Border',
    'ASPIREdb.view.AndFilterContainer',
    'ASPIREdb.view.VariantFilterPanel',
    'ASPIREdb.view.SubjectFilterPanel',
    'ASPIREdb.view.PhenotypeFilterPanel'
]);

Ext.define('ASPIREdb.view.FilterWindow', {
        extend: 'Ext.Window',
        alias: 'widget.filterwindow',

        title: 'Filter',
        closable: true,
        closeAction: 'hide',
        width: 700,
        height: 350,
        layout: 'border',
        bodyStyle: 'padding: 5px;',
        items: [
            {
                region: 'north',
                width: 600,
                items: [
                    {
                        xtype: 'container',
                        layout: {
                            type: 'hbox',
                            defaultMargins: {
                                top: 5,
                                right: 5,
                                left: 5,
                                bottom: 5
                            }
                        },
                        items: [
                            {
                                xtype: 'label',
                                text: 'Add new: '
                            },
                            {
                                xtype: 'combo',
                                itemId: 'filterTypeComboBox',
                                editable: false,
                                forceSelection: true,
                                value : 'FILTER_PLACEHOLDER',
                                store: [
                                    ['FILTER_PLACEHOLDER','<Filter>'],
                                    ['ASPIREdb.view.SubjectFilterPanel', 'Subject Filter'],
                                    ['ASPIREdb.view.VariantFilterPanel', 'Variant Filter'],
                                    ['ASPIREdb.view.PhenotypeFilterPanel', 'Phenotype Filter']
                                ]
                            },
                            {
                                xtype: 'label',
                                text: 'or load saved query: '
                            },
                            {
                                xtype: 'combo',
                                itemId: 'savedQueryComboBox'
                            }
                        ]
                    }
                ]
            },
            {
                region: 'center',
                xtype: 'container',
                itemId: 'filterContainer',
                overflowY: 'auto',
                layout: {
                    type: 'vbox'
                },
                items : [
                    {
                        xtype :'filter_variant'
                    }
                ]
            },
            {
                region: 'south',
                /*
                 xtype: 'container',
                 */
                layout: {
                    type: 'hbox',
                    defaultMargins: {
                        top: 5,
                        right: 5,
                        left: 5,
                        bottom: 5
                    }
                },
                items: [
                    { xtype: 'container',
                        flex: 1,
                        layout: {
                            type: 'hbox',
                            defaultMargins: {
                                top: 5,
                                right: 5,
                                left: 5,
                                bottom: 5
                            }
                        },items:[
                        {
                            xtype: 'label',
                            itemId: 'numberOfSubjectsLabel'
                        },
                        {
                            xtype: 'label',
                            text: ' subjects and '
                        },
                        {
                            xtype: 'label',
                            itemId: 'numberOfVariantsLabel'
                        },
                        {
                            xtype: 'label',
                            text: ' variants will be returned.'
                        }]
                    }, {
                        xtype: 'container',
                        flex: 1,
                        layout: {
                            type: 'hbox',
                            defaultMargins: {
                                top: 5,
                                right: 5,
                                left: 5,
                                bottom: 5
                            }
                        },
                        items:[
                            {
                                xtype: 'button',
                                flex: 1,
                                text: 'Submit',
                                itemId: 'applyButton'
                            },
                            {
                                xtype: 'button',
                                flex: 2,
                                text: 'Save query',
                                itemId: 'saveQueryButton'
                            },
                            {
                                xtype: 'button',
                                flex: 1,
                                text: 'Clear',
                                itemId: 'clearButton'
                            },
                            {
                                xtype: 'button',
                                flex: 1,
                                text: 'Cancel',
                                itemId: 'cancelButton'
                            }
                        ]
                    }

                ]
            }
        ],

        initComponent: function () {
            this.callParent();
        },

        initializeFilterProperties: function () {
            var filterWindow = this;

            var callback = {
                callback: function (properties) {
                    //filterWindow.getComponent('');
                }
            };
            VariantService.suggestProperties('CNV', callback);
        }
    }
);
