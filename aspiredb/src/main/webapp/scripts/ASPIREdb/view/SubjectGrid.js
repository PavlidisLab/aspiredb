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
Ext.require( [ 'ASPIREdb.store.SubjectStore', 'ASPIREdb.view.CreateLabelWindow', 'ASPIREdb.view.ApplyLabelWindow', 'ASPIREdb.view.LabelControlWindow',
              'ASPIREdb.TextDataDownloadWindow', 'ASPIREdb.view.report.BurdenAnalysisWindow', 'ASPIREdb.Utils' ] );

/**
 * Queries Subject values and loads them into a {@link Ext.grid.Panel}
 * 
 */
Ext.define( 'ASPIREdb.view.SubjectGrid', {
   /**
    * @memberOf ASPIREdb.view.SubjectGrid
    */
   extend : 'Ext.grid.Panel',
   alias : 'widget.subjectGrid',
   title : 'Subject',
   id : 'subjectGrid',
   multiSelect : true,   
   titlePosition: '0',
   header: {
      items: [{
          xtype: 'image',
          src: 'scripts/ASPIREdb/resources/images/qmark.png',
          height: '14px',
          width: '15px',
          listeners: {
             afterrender: function(c) {
                 Ext.create('Ext.tip.ToolTip', {
                     target: c.getEl(),
                     html: 'Displays the subjects that meet current query criteria (\'Filter\' button). Selecting a subject highlights associated variants (Variant panel) and shows phenotype values (Phenotype panel).'
                 });
             }
         }
      }]
  },
                 
   store : Ext.create( 'ASPIREdb.store.SubjectStore' ),
   config : {

      // member variables

      // labels that are displayable
      // { label.id : label.valueObject }
      visibleLabels : {},

      // all the subject value objects in the grid
      valueObjects : [],

      // selected subjects in the grid
      selSubjects : [],

      // the current filters used
      filterConfigs : [],

      // subject select all status holder
      selectAllStatus : 'No'
   },
   constructor : function(cfg) {
      this.initConfig( cfg );
      this.callParent( arguments );
   },
   plugins : {
      ptype : 'bufferedrenderer',
      trailingBufferZone : 20, // Keep 20 rows rendered in the table behind scroll
      leadingBufferZone : 50
   // Keep 50 rows rendered in the table ahead of scroll
   },
   pageSize : 100,
   listeners : {
      /**
       * Prevents the browser handling the right-click on the control.
       */
      itemcontextmenu : function(view, record, item, index, e) {
         // Stop the browser getting the event
         e.preventDefault();

         var contextMenu = new Ext.menu.Menu( {
            items : [ {
               text : 'Create label',
               handler : this.makeLabelHandler,
               scope : this,
            }, {
               text : 'Apply label',
               handler : this.applyLabelHandler,
               scope : this,
            }, {
               text : 'Remove label',
               handler : this.labelManagerHandler,
               scope : this,
            } ]
         } );

         contextMenu.showAt( e.getX(), e.getY() );
      }
   },

   columns : [ {
      text : "Subject Id",
      dataIndex : 'patientId',
   // flex : 1
   }, {
      text : "Labels",
      tooltip : 'Organize your subjects or variants using tags.', 
      dataIndex : 'labelIds',
      // This is very slow we need to rethink this
      renderer : function(value, metaData, record, row, col, store, gridView) {

         return ASPIREdb.Utils.renderLabel( this.visibleLabels, value, metaData );

      },
      flex : 1
   }, {
      text : "# of variants",
      dataIndex : 'varientNos',
      tooltip : 'Total # of unfiltered variants',
      
      renderer : function(value) {
         return value;
      },
      flex : 1
   }, {
      text : "# of phenotypes",
      dataIndex : 'phenotypeNos',
      tooltip : 'Total # of unfiltered phenotypes',
      
      flex : 1
   } ],

   bbar : [ {
      xtype : 'label',
      itemId : 'statusbar',
      html : ''
   } ], // bbar

   /**
    * Create tool bar and buttons on top of subject grid
    */
   initComponent : function() {

      this.callParent();

      var me = this;

      this.labelsMenu = Ext.create( 'Ext.menu.Menu', {
         items : [ {
            itemId : 'makeLabel',
            text : 'Create label',
            disabled : true,
            handler : this.makeLabelHandler,
            scope : this,
            tooltip : 'Create and apply a new label to the selected subjects.',            
         }, {
            itemId : 'applyLabel',
            text : 'Apply labels',
            disabled : true,
            handler : this.applyLabelHandler,
            scope : this,
            tooltip : 'Apply existing labels to the selected subjects.', 
         }, {
            itemId : 'labelManager',
            text : 'Manage labels',
            disabled : false,
            handler : this.labelManagerHandler,
            scope : this,
            tooltip : 'Edit labels for the selected subjects.',            
         } ]
      } );

      this.labelsButton = Ext.create( 'Ext.button.Split', {
         text : '<b>Labels</b>',
         tooltip : 'Organize your subjects or variants using tags.',
         itemId : 'labelsButton',
         menu : this.labelsMenu
      } );

      this.selectAllButton = Ext.create( 'Ext.Button', {
         itemId : 'selectAll',
         text : 'Select all',
         handler : this.selectAllHandler,
         scope : this
      } );

      this.deselectAllButton = Ext.create( 'Ext.Button', {
         itemId : 'deselectAll',
         text : 'Clear all',
         handler : this.deselectAllHandler,
         scope : this
      } );

      this.saveButton = Ext.create( 'Ext.Button', {
         itemId : 'saveButton',
         text : '',
         tooltip : 'Download table contents as text',
         
         icon : 'scripts/ASPIREdb/resources/images/icons/disk.png',
      } );

      this.burdenAnalysisButton = Ext.create( 'Ext.Button', {
         itemId : 'burdenAnalysisButton',
         text : 'Burden analysis',
         handler : this.burdenAnalysisHandler,
         scope : this,
         tooltip : 'Compare variant distribution in subject groups.',
         
      } );

      this.toolbar = Ext.create( 'Ext.toolbar.Toolbar', {
         itemId : 'subjectGridToolbar',
         dock : 'top'
      } );

      Ext.apply( this, {
         bbar : new Ext.Toolbar( {
            itemId : 'statusBar',
            text : '',
            scope : this,
         } )
      } );

      this.toolbar.add( this.labelsButton );
      this.toolbar.add( this.burdenAnalysisButton );
      this.toolbar.add( this.selectAllButton );
      this.toolbar.add( this.deselectAllButton );
      this.toolbar.add( Ext.create( 'Ext.toolbar.Fill' ) );
      this.toolbar.add( this.saveButton );
      this.addDocked( this.toolbar );

      // When Save button is clicked open text data downlaod
      // window
      this.saveButton.on( 'click', function() {
         ASPIREdb.TextDataDownloadWindow.showSubjectDownload( me.valueObjects );
      }, this );

      // when subject filter submit
      ASPIREdb.EVENT_BUS.on( 'construct_subject_grid', this.constructGrid, this );

      // when subject selected
      // this.on( 'selectionchange', me.selectionChangeHandler, me );
      this.on( 'cellclick', me.selectionChangeHandler, me );

      // when subject label is removed
      ASPIREdb.EVENT_BUS.on( 'subject_label_removed', this.labelRemovedHandler, this );

      // when subject label is removed
      ASPIREdb.EVENT_BUS.on( 'subject_label_updated', this.labelUpdateHandler, this );

      ASPIREdb.EVENT_BUS.on( 'select_subject_from_variant_grid', this.selectSubjectHandler, this );
      
      ASPIREdb.EVENT_BUS.on( 'select_subject_from_ideogram', this.selectSubjectHandler, this );

      // when subject label is shown
      ASPIREdb.EVENT_BUS.on( 'subject_label_changed', function() {

         me.getView().refresh();

      }, this );

   },

   selectSubjectHandler : function(subjectIds) {

      var grid = this;

      grid.getSelectionModel().deselectAll();

      for (var i = 0; i < subjectIds.length; i++) {
		var subj = subjectIds[i];
		grid.selModel.select( grid.store.find( 'id', subj ), true );
	  }
      

      var selectedRecords = grid.getSelectionModel().getSelection();
      grid.getView().bufferedRenderer.scrollTo( grid.store.indexOfId( selectedRecords[0].data.id ) );

   },

   /**
    * Load subject labels created by the user
    * 
    * @return visibleLabels
    */
   createVisibleLabels : function(vvos) {
      var visibleLabels = [];

      for (var i = 0; i < vvos.length; i++) {
         var labels = vvos[i].labels;
         for (var j = 0; j < labels.length; j++) {
            var label = labels[j];
            visibleLabels[label.id] = label;
         }
      }

      return visibleLabels;
   },

   /**
    * Populate grid with Subjects and Labels
    * 
    * @param :
    *           subject filter configurations
    * 
    */
   constructGrid : function(filterConfigs, vos) {

      var me = this;
      me.valueObjects = vos;
      me.setLoading( true );
      me.getStore().removeAll();

      // load existing subject labels
      me.visibleLabels = me.createVisibleLabels( me.valueObjects );

      var data = [];

      console.log( me.valueObjects.length + " subjects being processed into value objects" );
      // find the number of subjects
      // filtered

      ProjectService.numSubjects( filterConfigs[0].projectIds, {
         callback : function(NoOfSubjects) {
            me.down( '#statusbar' ).update( me.valueObjects.length + " / " + NoOfSubjects + " subjects loaded" );
         }
      } );

      for (var i = 0; i < me.valueObjects.length; i++) {
         var val = me.valueObjects[i];

         // create only one unique
         // label instance
         var labelIds = [];

         for (var j = 0; j < val.labels.length; j++) {
            var aLabel = me.visibleLabels[val.labels[j].id];

            // this happens when a
            // label has been
            // assigned
            // by the admin and the
            // user has no
            // permissions
            // to modify the label
            if ( aLabel === undefined ) {
               aLabel = val.labels[j];
            }

            labelIds.push( aLabel.id );
         }

         // create summary of number
         // of variants
         var row = [ val.id, val.patientId, labelIds, val.variants, val.numOfPhenotypes ];
         data.push( row );
      }

      me.store.loadData( data );

      me.setLoading( false );

      // refresh grid
      me.getView().refresh();

      var ids = [];
      for (var k = 0; k < me.valueObjects.length; k++) {
         var o = me.valueObjects[k];
         ids.push( o.id );
      }

      console.log( ids.length + " subjects loaded" );
      ASPIREdb.EVENT_BUS.fireEvent( 'subjects_loaded', ids );

   },

   /**
    * This method called when subject are selected in the subject grid
    */
   selectionChangeHandler : function() {
      this.selSubjects = this.getSelectionModel().getSelection();
      this.selectAllStatus = 'No';

      if ( this.selSubjects.length === 0 ) {
         this.down( '#makeLabel' ).disable();
         this.down( '#applyLabel' ).disable();
         return;
      } else {
         this.down( '#makeLabel' ).enable();
         this.down( '#applyLabel' ).enable();
      }

      if ( this.selSubjects.length >= 1 ) {
         var ids = [];

         for (var i = 0; i < this.selSubjects.length; i++) {
            ids.push( this.selSubjects[i].data.id );
         }
         ASPIREdb.EVENT_BUS.fireEvent( 'subject_selected', ids );
      } else {
         ASPIREdb.EVENT_BUS.fireEvent( 'subject_selected', this.store.collect( 'id' ), true );
      }
   },

   /**
    * Assigns a Label
    * 
    * @param :
    *           event
    */
   makeLabelHandler : function(event) {

      var me = this;

      me.selSubjects = me.getSelectionModel().getSelection();

      Ext.define( 'ASPIREdb.view.CreateLabelWindowSubject', {
         isSubjectLabel : true,
         title : 'Create Subject Label',
         header: {
            items: [{
                xtype: 'image',
                src: 'scripts/ASPIREdb/resources/images/qmark.png',
                listeners: {
                   afterrender: function(c) {
                       Ext.create('Ext.tip.ToolTip', {
                           target: c.getEl(),
                           html: 'Create and apply a label to the subjects selected in the Subject panel.'
                       });
                   }
               }
            }]
        },        
   
         extend : 'ASPIREdb.view.CreateLabelWindow',
         selectedIds : ASPIREdb.Utils.getSelectedIds( me.getSelectionModel().getSelection() ),

      } );

      var labelWindow = new ASPIREdb.view.CreateLabelWindowSubject();
      labelWindow.show();

   },
   
   applyLabelHandler : function(event) {
      var me = this;

      me.selSubjects = me.getSelectionModel().getSelection();

      Ext.define( 'ASPIREdb.view.ApplyLabelWindowSubject', {
         isSubjectLabel : true,
         title : 'Apply Subject Labels',
         header: {
            items: [{
                xtype: 'image',
                src: 'scripts/ASPIREdb/resources/images/qmark.png',
                listeners: {
                   afterrender: function(c) {
                       Ext.create('Ext.tip.ToolTip', {
                           target: c.getEl(),
                           html: 'Apply existing labels to the subjects selected in the Subject panel.'
                       });
                   }
               }
            }]
        },        
   
         extend : 'ASPIREdb.view.ApplyLabelWindow',
         selectedIds : ASPIREdb.Utils.getSelectedIds( me.getSelectionModel().getSelection() ),

      } );

      var labelWindow = new ASPIREdb.view.ApplyLabelWindowSubject();
      labelWindow.show();
   },

   /**
    * Remove labels from subjects in local store.
    */
   removeLabelsFromSubjects : function(subjects, labelsToRemove) {

      for (var i = 0; i < subjects.length; i++) {
         var labelIds = subjects[i].data.labelIds;
         for (var k = 0; k < labelsToRemove.length; k++) {
            var idx = labelIds.indexOf( labelsToRemove[k].id );
            if ( idx > -1 ) {
               labelIds.splice( idx, 1 );
            }
         }
      }
   },

   /**
    * Label changed. Update subjects label in grid.
    */
   labelUpdateHandler : function(selectedIds, addedLabel) {
      var ref = this;

      if ( selectedIds == undefined || selectedIds.length == 0 || addedLabel == undefined ) {
         return;
      }

      // update the label store cache for rendering
      var existingLab = ref.visibleLabels[addedLabel.id];
      if ( existingLab == undefined ) {
         ref.visibleLabels[addedLabel.id] = addedLabel;
      } else {
         existingLab.isShown = true;
         existingLab.name = addedLabel.name;
         existingLab.description = addedLabel.description;
         existingLab.colour = addedLabel.colour;
      }

      for (var i = 0; i < selectedIds.length; i++) {
         var labelIds = ref.store.findRecord( 'id', selectedIds[i] ).data.labelIds;
         if ( labelIds.indexOf( addedLabel.id ) == -1 ) {
            labelIds.push( addedLabel.id );
         }
      }

      ASPIREdb.EVENT_BUS.fireEvent( 'subjects_label_changed' );

      ref.getView().refresh();
   },

   /**
    * Label changed. Update labels in grid.
    */
   labelRemovedHandler : function(selSubjectIds, labelsToRemove) {
      var me = this;

      if ( selSubjectIds.length > 0 ) {
         me.removeLabelsFromSubjects( me.selSubjects, labelsToRemove );
      } else {
         var allSubjects = this.store.data.items;
         me.removeLabelsFromSubjects( allSubjects, labelsToRemove );
      }

      ASPIREdb.EVENT_BUS.fireEvent( 'subjects_label_changed' );

      me.getView().refresh();
   },

   updateVisibleLabelsFromStore : function() {
      var ret = {};
      for (var i = 0; i < this.store.data.items.length; i++) {
         var labelIds = this.store.data.items[i].data.labelIds;
         for (var j = 0; j < labelIds.length; j++) {
            var labelId = labelIds[j];
            ret[labelId] = this.visibleLabels[labelId];
         }
      }
      return ret;
   },

   /**
    * Display labelManagerWindow
    * 
    * @param :
    *           event
    */
   labelManagerHandler : function(event) {
      var me = this;

      var selectedSubjectIds = [];

      me.selSubjects = me.getSelectionModel().getSelection();

      // check visibleLabels and make sure it's up-to-date
      // use store instead of value objects because value objects may
      // already be stale
      me.visibleLabels = me.updateVisibleLabelsFromStore();

      var labelControlWindow = Ext.create( 'ASPIREdb.view.LabelControlWindow', {
         visibleLabels : me.visibleLabels,
         isSubjectLabel : true,
         selectedOwnerIds : ASPIREdb.Utils.getSelectedIds( me.getSelectionModel().getSelection() ),
         title : "Subject Label Manager",
         toolFirst: true,
         header: {
            items: [{
                xtype: 'image',
                src: 'scripts/ASPIREdb/resources/images/qmark.png',
                listeners: {
                   afterrender: function(c) {
                       var toolTip = Ext.create('Ext.tip.ToolTip', {
                           target: c.getEl(),
                           html: 'Use labels to assign custom tags to a group of subjects or variants. Labels can also be used in queries. Click <a href="http://aspiredb.chibi.ubc.ca/manual/labels/" target="_blank">here</a> for more details.',
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
      } );

      labelControlWindow.show();
   },

   /**
    * When all the subjects are sselected this is executed
    */
   selectAllHandler : function() {
      if ( this.selectAllStatus == 'No' ) {
         this.cancelBubble = true;
         // boolean true to suppressEvent
         this.getSelectionModel().selectAll( true );
         this.selectionChangeHandler();
         this.selectAllStatus = 'Yes';
      }

   },

   deselectAllHandler : function() {
      this.cancelBubble = true;
      this.getSelectionModel().deselectAll(); // calls selectionChangeHandler
      this.selectionChangeHandler();
      this.selectAllStatus = 'No';
      ASPIREdb.EVENT_BUS.fireEvent( 'subject_selection_cleared' );
   },

   burdenAnalysisHandler : function() {
      var reportWindow = Ext.create( 'ASPIREdb.view.report.BurdenAnalysisWindow' );
      var variantStore = Ext.StoreMgr.lookup( 'variantGrid' );
      reportWindow.createAndShow( variantStore );
   }

} );