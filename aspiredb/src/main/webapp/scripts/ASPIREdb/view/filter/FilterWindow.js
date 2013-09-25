Ext.require([
    'Ext.window.*',
    'Ext.layout.container.Border',
    'ASPIREdb.view.filter.AndFilterContainer',
    'ASPIREdb.view.filter.VariantFilterPanel',
    'ASPIREdb.view.filter.SubjectFilterPanel',
    'ASPIREdb.view.filter.PhenotypeFilterPanel'
]);

Ext.define('ASPIREdb.view.filter.FilterWindow', {
        extend: 'Ext.Window',
        alias: 'widget.filterwindow',
        singleton: true,
        title: 'Filter',
        closable: true,
        closeAction: 'hide',
        width: 1000,
        height: 500,
        layout: 'border',
        bodyStyle: 'padding: 5px;',

        initComponent: function () {
            var me = this;
            this.items = [
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
                                    value: 'FILTER_PLACEHOLDER',
                                    store: [
                                        ['FILTER_PLACEHOLDER', '<Filter>'],
                                        ['ASPIREdb.view.filter.SubjectFilterPanel', 'Subject Filter'],
                                        ['ASPIREdb.view.filter.VariantFilterPanel', 'Variant Filter'],
                                        ['ASPIREdb.view.filter.PhenotypeFilterPanel', 'Phenotype Filter']
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
                    items: [
                        {
                            xtype: 'filter_variant'
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
                            }, items: [
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
                            }
                        ]
                        },
                        {
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
                            items: [
                                {
                                    xtype: 'button',
                                    flex: 1,
                                    text: 'Submit',
                                    itemId: 'applyButton',
                                    handler: function () {
                                        var filterConfigs = me.getFilterConfigs();
                                        ASPIREdb.EVENT_BUS.fireEvent('filter_submit', filterConfigs);
                                        me.close();
                                    }
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
            ];

            this.callParent();
            var filterTypeComboBox = this.down('#filterTypeComboBox');
            var filterContainer = this.down('#filterContainer');

            filterTypeComboBox.on('select', function (combo, records) {
                var record = records[0];
                filterContainer.add(
                    Ext.create(record.raw[0])
                );
                filterTypeComboBox.setValue('FILTER_PLACEHOLDER');
            });
        },

        getFilterConfigs: function () {
            /**
             * @type {Array.RestrictionFilterConfig}
             */
            var filterConfigs = [];
            var projectFilter = new ProjectFilterConfig;
            projectFilter.projectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds();
            filterConfigs.push(projectFilter);
            var filterContainer = this.down('#filterContainer');
            filterContainer.items.each(function (item, index, length) {
                filterConfigs.push(item.getFilterConfig());
            });

            return filterConfigs;
        },

        /**
         *
         */
//        updateResultCounts: function() {
//            numPreviewQueriesInProgress = 2;
//            numberOfSubjectsLabel.setText("...");
//            numberOfVariantsLabel.setText("...");
//
//            AspireDbPagingLoadConfig config = new AspireDbPagingLoadConfigBean();
//            config.getFilters().add( new ProjectFilterConfig( ActiveProjectSettings.getActiveProjects() ) );
//            config.setOffset(0);
//            config.setLimit(2000);
//            config.getFilters().addAll( getFilterConfigs() );
//            queryService.getSubjectCount(config, new AspireAsyncCallback<Integer>(){
//            @Override
//            public void onSuccess(Integer count) {
//                numberOfSubjectsLabel.setText(count.toString());
//                numPreviewQueriesInProgress--;
//                if (numPreviewQueriesInProgress == 0 && runPreviewQuery) {
//                    updateResultCounts();
//                    runPreviewQuery = false;
//                }
//            }
//        });
//
//        queryService.getVariantCount(config, new AspireAsyncCallback<Integer>(){
//            @Override
//            public void onSuccess(Integer count) {
//                numberOfVariantsLabel.setText(count.toString());
//                numPreviewQueriesInProgress--;
//                if (numPreviewQueriesInProgress == 0 && runPreviewQuery) {
//                    updateResultCounts();
//                    runPreviewQuery = false;
//                }
//            }
//        });
//    },

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
