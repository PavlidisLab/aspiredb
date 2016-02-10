/*
 * The aspiredb project
 *
 * Copyright (c) 2013 University of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
Ext.require( [ 'Ext.window.*', 'Ext.layout.container.Border', 'ASPIREdb.view.filter.AndFilterContainer',
              'ASPIREdb.view.filter.VariantFilterPanel', 'ASPIREdb.view.filter.SubjectFilterPanel',
              'ASPIREdb.view.filter.PhenotypeFilterPanel', 'ASPIREdb.view.SaveQueryWindow',
              'ASPIREdb.view.filter.ProjectOverlapFilterPanel',
              'ASPIREdb.view.filter.DecipherProjectOverlapFilterPanel',
              'ASPIREdb.view.filter.DgvProjectOverlapFilterPanel', 'ASPIREdb.view.DeleteQueryWindow',
              'ASPIREdb.TextDataDownloadWindow' ] );

/**
 * The main filter window which contains various filter panels, load/save query, and preview and Submit buttons.
 */
Ext
   .define(
      'ASPIREdb.view.filter.FilterWindow',
      {
         extend : 'Ext.Window',
         alias : 'widget.filterwindow',
         singleton : true,
         title : 'Filter',
         closable : true,
         closeAction : 'hide',
         width : 1000,
         height : 500,
         layout : 'border',
         bodyStyle : 'padding: 5px;',
         border : false,
         constrain : true,
         header: {
            items: [{
                xtype: 'image',
                src: 'scripts/ASPIREdb/resources/images/qmark.png',
                listeners: {
                   afterrender: function(c) {
                       var toolTip = Ext.create('Ext.tip.ToolTip', {
                           target: c.getEl(),
                           html: 'Choose a filter type from the \"Add new:\" drop down box. Please click the "Help" button or <a href="http://aspiredb.chibi.ubc.ca/manual/expression-filter/" target="_blank">here</a> for more details.',
                           
                           dismissDelay: 0,
                           showDelay: 0,
                           autoHide: false
                   
                       }); 
                       toolTip.on('show', function(){

                          var timeout;

                          toolTip.getEl().on('mouseout', function(){
                              timeout = window.setTimeout(function(){
                                  toolTip.hide();
                              }, 500);
                          });

                          toolTip.getEl().on('mouseover', function(){
                              window.clearTimeout(timeout);
                          });

                          Ext.get(c.getEl()).on('mouseover', function(){
                              window.clearTimeout(timeout);
                          });

                          Ext.get(c.getEl()).on('mouseout', function(){
                              timeout = window.setTimeout(function(){
                                  toolTip.hide();
                              }, 500);
                          });

                      });
                       
                   }
               }
            }]
        },    
         config : {
            isOverlapedProjects : 'No',
            SUBJECT_IDS_KEY : 0,
            VARIANT_IDS_KEY : 1,
         },

         initComponent : function() {
            var me = this;
            this.items = [
                          {
                             region : 'north',
                             width : 900,
                             items : [ {
                                xtype : 'container',
                                layout : {
                                   type : 'hbox',
                                   defaultMargins : {
                                      top : 5,
                                      right : 5,
                                      left : 5,
                                      bottom : 5
                                   }
                                },
                                items : [
                                         {
                                            xtype : 'label',
                                            text : 'Add new: '
                                         },
                                         {
                                            xtype : 'combo',
                                            itemId : 'filterTypeComboBox',
                                            editable : false,
                                            forceSelection : true,
                                            // selectOnFocus:true,
                                            enableKeyEvents : true,
                                            value : 'FILTER_PLACEHOLDER',
                                            store : [
                                                     [ 'FILTER_PLACEHOLDER', '<Filter>' ],
                                                     [ 'ASPIREdb.view.filter.SubjectFilterPanel', 'Subject Filter' ],
                                                     [ 'ASPIREdb.view.filter.VariantFilterPanel', 'Variant Filter' ],
                                                     [ 'ASPIREdb.view.filter.PhenotypeFilterPanel', 'Phenotype Filter' ],
                                                     [ 'ASPIREdb.view.filter.DecipherProjectOverlapFilterPanel',
                                                      'Decipher Overlap' ],
                                                     [ 'ASPIREdb.view.filter.DgvProjectOverlapFilterPanel',
                                                      'DGV Overlap' ],
                                                     [ 'ASPIREdb.view.filter.ProjectOverlapFilterPanel',
                                                      'Project Overlap' ] ]
                                         }, {
                                            xtype : 'label',
                                            text : 'or load saved query: '
                                         }, {
                                            xtype : 'combo',
                                            itemId : 'savedQueryComboBox',
                                            editable : false,
                                            forceSelection : true,
                                            value : 'QUERY_NAME_PLACEHOLDER',
                                            store : [ [ 'QUERY_NAME_PLACEHOLDER', '<Query name>' ] ]
                                         }, {
                                            xtype : 'button',
                                            flex : 1,
                                            text : 'Save query',
                                            itemId : 'saveQueryButton',
                                            handler : me.saveQueryHandler,
                                            scope : me
                                         }, {
                                            xtype : 'button',
                                            flex : 1,
                                            text : 'Query Manager',
                                            id : 'querymanager',
                                            // shrinkWrap : 1,
                                            width : 30,
                                            disabled : true,
                                            itemId : 'deleteQueryButton',
                                            handler : me.deleteQueryHandler,
                                            scope : me
                                         } ]
                             } ]
                          }, {
                             region : 'center',
                             xtype : 'container',
                             itemId : 'filterContainer',
                             overflowY : 'auto',
                             layout : {
                                type : 'vbox'
                             },
                             items : [
                             // {
                             // xtype: 'filter_variant'
                             // }
                             ]
                          }, {
                             region : 'south',
                             /*
                               * xtype: 'container',
                               */
                             layout : {
                                type : 'hbox',
                                defaultMargins : {
                                   top : 5,
                                   right : 5,
                                   left : 5,
                                   bottom : 5
                                }
                             },
                             items : [ {
                                xtype : 'container',
                                flex : 1,
                                layout : {
                                   type : 'hbox',
                                   defaultMargins : {
                                      top : 5,
                                      right : 5,
                                      left : 5,
                                      bottom : 5
                                   }
                                },
                                items : [ {
                                   xtype : 'label',
                                   itemId : 'numberOfSubjectsLabel',
                                   style : {
                                      opacity : 0
                                   }
                                }, {
                                   xtype : 'label',
                                   text : ' subjects and ',
                                   itemId : 'numberOfSubjectsLabelText',
                                   style : {
                                      opacity : 0
                                   }
                                }, {
                                   xtype : 'label',
                                   itemId : 'numberOfVariantsLabel',
                                   style : {
                                      opacity : 0
                                   }

                                }, {
                                   xtype : 'label',
                                   text : ' variants will be returned.',
                                   itemId : 'numberOfVariantsLabelText',
                                   style : {
                                      opacity : 0
                                   }
                                } ]
                             }, {
                                xtype : 'container',
                                flex : 1,
                                layout : {
                                   type : 'hbox',
                                   defaultMargins : {
                                      top : 5,
                                      right : 5,
                                      left : 5,
                                      bottom : 5
                                   }
                                },
                                items : [ {
                                   xtype : 'button',
                                   flex : 1,
                                   text : 'Docs',
                                   itemId : 'helpButton',
                                   tooltip: 'Navigate to the user manual (opens in a new tab).',
                                   handler : function() {
                                      window.open( 'http://aspiredb.chibi.ubc.ca/manual/expression-filter/' );
                                   },
                                   scope : me,
                                }, {
                                   xtype : 'button',
                                   flex : 1,
                                   text : 'Preview',
                                   itemId : 'previewQueryButton',
                                   handler : me.previewQueryHandler,
                                   scope : me
                                }, {
                                   xtype : 'button',
                                   flex : 1,
                                   text : 'Submit',
                                   itemId : 'applyButton',
                                   handler : function() {
                                      var filterConfigs = me.getFilterConfigs();
                                      ASPIREdb.EVENT_BUS.fireEvent( 'filter_submit', filterConfigs );
                                      me.close();
                                   }
                                }, {
                                   xtype : 'button',
                                   flex : 1,
                                   text : 'Clear',
                                   itemId : 'clearButton',
                                   handler : me.clearButtonHandler,
                                   scope : me
                                }, {
                                   xtype : 'button',
                                   flex : 1,
                                   text : 'Cancel',
                                   itemId : 'cancelButton',
                                   handler : me.cancelButtonHandler,
                                   scope : me
                                } ]
                             }

                             ]
                          } ];

            this.callParent();
            var filterTypeComboBox = this.down( '#filterTypeComboBox' );
            var filterContainer = this.down( '#filterContainer' );

            this.updateSavedQueryCombo();
            this.enableDisableQueryManager();

            this.down( '#savedQueryComboBox' ).on( 'select', this.savedQueryComboBoxSelectHandler, this );

            ASPIREdb.EVENT_BUS.on( 'filter_submit', this.filterSubmitHandler, this );

            ASPIREdb.EVENT_BUS.on( 'project_select', this.clearButtonHandler, this );

            filterTypeComboBox.on( 'select', function(combo, records) {
               var record = records[0];
               filterContainer.add( Ext.create( record.raw[0] ) );
               filterTypeComboBox.setValue( 'FILTER_PLACEHOLDER' );
               me.invalidateResultCounts();
            } );

            ASPIREdb.view.DeleteQueryWindow.on( 'query_deleted', this.updateSavedQueryCombo, this );
            ASPIREdb.view.DeleteQueryWindow.on( 'query_deleted', this.enableDisableQueryManager, this );
            ASPIREdb.view.SaveQueryWindow.on( 'new_query_saved', this.updateSavedQueryCombo, this );
            ASPIREdb.view.SaveQueryWindow.on( 'new_query_saved', this.enableDisableQueryManager, this );

            ASPIREdb.EVENT_BUS.on( 'query_update', function(event) {
               me.invalidateResultCounts();
            } );

            this.updateSpecialProjectValues();

         },

         loadQueryHandler : function(filters) {

            // ASPIREdb.EVENT_BUS.fireEvent('filter_submit', filters);

            var filterContainer = this.down( '#filterContainer' );
            filterContainer.doLayout();

            filterContainer.removeAll( true );

            // first is filterconfig
            for (var i = 0; i < filters.length; i++) {

               if ( filters[i].restriction || filters[i].restriction1 ) {

                  if ( filters[i] instanceof SubjectFilterConfig ) {

                     var subjectFilterPanel = Ext.create( 'ASPIREdb.view.filter.SubjectFilterPanel' );
                     filterContainer.add( subjectFilterPanel );
                     filterContainer.doLayout();
                     subjectFilterPanel.setFilterConfig( filters[i] );

                  } else if ( filters[i] instanceof PhenotypeFilterConfig ) {

                     var phenotypeFilterPanel = Ext.create( 'ASPIREdb.view.filter.PhenotypeFilterPanel' );
                     filterContainer.add( phenotypeFilterPanel );
                     filterContainer.doLayout();
                     phenotypeFilterPanel.setFilterConfig( filters[i] );

                  } else if ( filters[i] instanceof VariantFilterConfig ) {

                     var variantFilterPanel = Ext.create( 'ASPIREdb.view.filter.VariantFilterPanel' );

                     filterContainer.add( variantFilterPanel );
                     filterContainer.doLayout();
                     variantFilterPanel.setFilterConfig( filters[i] );

                  } else if ( filters[i] instanceof ProjectOverlapFilterConfig ) {

                     // overlapProjectIds will only have 1 entry currently, however it is an array to allow for
                     // extension to multiple projects later
                     var overlapProjectId = filters[i].overlapProjectIds[0];

                     if ( overlapProjectId == this.decipherProjectValueObject.id ) {

                        var decipherProjectOverlapFilterPanel = Ext
                           .create( 'ASPIREdb.view.filter.DecipherProjectOverlapFilterPanel' );

                        filterContainer.add( decipherProjectOverlapFilterPanel );
                        filterContainer.doLayout();
                        decipherProjectOverlapFilterPanel.setFilterConfig( filters[i] );

                     } else if ( overlapProjectId == this.dgvProjectValueObject.id ) {

                        var dgvProjectOverlapFilterPanel = Ext
                           .create( 'ASPIREdb.view.filter.DgvProjectOverlapFilterPanel' );

                        filterContainer.add( dgvProjectOverlapFilterPanel );
                        filterContainer.doLayout();
                        dgvProjectOverlapFilterPanel.setFilterConfig( filters[i] );

                     } else {
                        // This will be the user project overlap functionality
                        var projectOverlapFilterPanel = Ext.create( 'ASPIREdb.view.filter.ProjectOverlapFilterPanel' );
                        this.isOverlapedProjects = 'Yes';
                        filterContainer.add( projectOverlapFilterPanel );
                        filterContainer.doLayout();
                        projectOverlapFilterPanel.setFilterConfig( filters[i] );

                     }

                  }

               }

            }

            ASPIREdb.EVENT_BUS.fireEvent( 'query_update' );

         },

         savedQueryComboBoxSelectHandler : function() {

            var combo = this.down( '#savedQueryComboBox' );

            if ( combo.getValue() && combo.getValue() != '' ) {

               // Ext.getCmp('querymanager').enable();

               QueryService.loadQuery( combo.getValue(), {
                  callback : this.loadQueryHandler,
                  errorHandler : function(message, exception) {
                     console.log( message )
                     console.log( dwr.util.toDescriptiveString( exception.stackTrace, 3 ) )
                  },
                  scope : this
               } );

            }

         },

         updateSavedQueryCombo : function() {

            var savedQueryComboBox = this.down( '#savedQueryComboBox' );

            QueryService.getSavedQueryNames( {
               callback : function(names) {

                  var storedata = [ [ 'QUERY_NAME_PLACEHOLDER', '<Query name>' ] ];

                  for (var i = 0; i < names.length; i++) {

                     storedata.push( [ names[i], names[i] ] );

                  }

                  savedQueryComboBox.getStore().loadData( storedata );
                  if ( names.length > 1 ) {
                     savedQueryComboBox.select( savedQueryComboBox.getStore().getAt( names.length ) );
                  }

               }
            } );

            this.down( '#savedQueryComboBox' ).clearValue();

         },

         enableDisableQueryManager : function() {
            QueryService.getSavedQueryNames( {
               callback : function(names) {

                  if ( names.length != 0 )
                     Ext.getCmp( 'querymanager' ).enable();
                  else
                     Ext.getCmp( 'querymanager' ).disable();
               }
            } );

         },

         saveQueryHandler : function() {

            ASPIREdb.view.SaveQueryWindow.initAndShow( this.getFilterConfigs() );

         },

         deleteQueryHandler : function() {

            ASPIREdb.view.DeleteQueryWindow.initAndShow( this.getFilterConfigs() );

         },

         enableOverlappButton : function() {
            // var projectOverlapFilterContainer =
            // ASPIREdb.view.filter.ProjectOverlapFilterPanel.getComponent('projectOverlapFilterContainer');
            var projectOverlapFilterPanel = Ext.create( 'ASPIREdb.view.filter.ProjectOverlapFilterPanel' );
            projectOverlapFilterPanel.down( '#overlappedVariants' ).enable();
         },

         previewQueryHandler : function() {
            var filterContainer = this.down( '#filterContainer' );
            if ( filterContainer.down( '#overlappedVariants' ) != undefined ) {
               // if project overlap filter panel, then enable the overlapped variants button in filter container
               filterContainer.down( '#overlappedVariants' ).enable();
            }

            var me = this;

            me.setLoading( true );

            QueryService.getSubjectVariantCounts( this.getFilterConfigs(), {
               callback : function(totalCounts) {
                  me.down( '#numberOfSubjectsLabel' ).setText( totalCounts[me.SUBJECT_IDS_KEY].toString() );
                  me.down( '#numberOfVariantsLabel' ).setText( totalCounts[me.VARIANT_IDS_KEY].toString() );

                  if ( me.down( '#numberOfSubjectsLabel' ).getEl() && me.down( '#numberOfVariantsLabel' ).getEl() ) {
                     me.down( '#numberOfSubjectsLabel' ).getEl().setOpacity( 1, true );
                     me.down( '#numberOfVariantsLabel' ).getEl().setOpacity( 1, true );
                     me.down( '#numberOfSubjectsLabelText' ).getEl().setOpacity( 1, true );
                     me.down( '#numberOfVariantsLabelText' ).getEl().setOpacity( 1, true );
                  }

                  me.setLoading( false );
               },
               errorHandler : function(errorString, exception) {
                  me.setLoading( false );
                  alert( errorString )
                  console.log( dwr.util.toDescriptiveString( exception, 2 ) )
                  console.log( dwr.util.toDescriptiveString( exception.stackTrace, 3 ) )
               }
            } );

         },

         filterSubmitHandler : function(filterConfigs) {

            var me = this;

            var myMask = new Ext.LoadMask( Ext.getBody(), {
               msg : "Loading..."
            } );
            myMask.show();

            QueryService.getSubjectsVariants( filterConfigs,
               {
                  callback : function(ret) {

                     ASPIREdb.EVENT_BUS.fireEvent( 'construct_variant_grid', filterConfigs, ret[me.VARIANT_IDS_KEY],
                        null );

                     ASPIREdb.EVENT_BUS.fireEvent( 'construct_subject_grid', filterConfigs, ret[me.SUBJECT_IDS_KEY] );

                     myMask.hide();

                  },
                  errorHandler : function(errorString, exception) {
                     myMask.hide();
                     alert( errorString )
                     console.log( dwr.util.toDescriptiveString( exception, 2 ) )
                     console.log( dwr.util.toDescriptiveString( exception.stackTrace, 3 ) )
                  }
               } );

         },

         cancelButtonHandler : function() {

            this.hide();

         },

         clearButtonHandler : function() {

            this.down( '#filterContainer' ).removeAll();
            this.down( '#savedQueryComboBox' ).clearValue();
            ASPIREdb.EVENT_BUS.fireEvent( 'query_update' );

         },

         getFilterConfigs : function() {
            /**
             * @type {Array.RestrictionFilterConfig}
             */
            var filterConfigs = [];
            var projectFilter = new ProjectFilterConfig();
            projectFilter.projectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds();
            filterConfigs.push( projectFilter );
            var filterContainer = this.down( '#filterContainer' );
            filterContainer.items.each( function(item, index, length) {
               filterConfigs.push( item.getFilterConfig() );
            } );

            return filterConfigs;
         },

         invalidateResultCounts : function() {

            if ( this.down( '#numberOfSubjectsLabel' ).getEl() && this.down( '#numberOfVariantsLabel' ).getEl() ) {
               this.down( '#numberOfSubjectsLabel' ).getEl().setOpacity( 0, true );
               this.down( '#numberOfVariantsLabel' ).getEl().setOpacity( 0, true );
               this.down( '#numberOfSubjectsLabelText' ).getEl().setOpacity( 0, true );
               this.down( '#numberOfVariantsLabelText' ).getEl().setOpacity( 0, true );
            }

         },

         /**
          * Populate the overlapped variants
          */
         overlappedVariantsHandler : function(projectId) {

            var me = this;

            VariantService.suggestProperties( function(properties) {
               // find the filer configs of the overlapped project
               var overlappedFilterConfigs = me.getFilterConfigs();
               var projectIds = [];
               overlappedFilterConfigs[1].projectsIds = projectIds;

               QueryService.queryVariants( overlappedFilterConfigs, {
                  callback : function(pageLoad) {

                     var vvos = pageLoad.items;
                     var data = [];
                     var headers = [ 'Patient Id     ', 'Genome Coordinates' ];

                     for (var i = 0; i < vvos.length; i++) {

                        var vvo = vvos[i];

                        var dataRow = [];

                        dataRow.push( vvo.id );
                        dataRow.push( vvo.patientId + "     " );
                        dataRow.push( vvo.genomicRange.chromosome + ":" + vvo.genomicRange.baseStart + "-"
                           + vvo.genomicRange.baseEnd );
                        data.push( dataRow );
                     }

                     ASPIREdb.TextDataDownloadWindow.initAndShow( data, headers );

                  }

               } );
            } );

         },

         updateSpecialProjectValues : function() {

            var ref = this;

            ProjectService.getDecipherProject( {

               callback : function(pvo) {

                  ref.decipherProjectValueObject = pvo;

               }
            } );

            ProjectService.getDgvProject( {

               callback : function(pvo) {

                  ref.dgvProjectValueObject = pvo;

               }
            } );

         }

      } );
