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
Ext.require( [ 'ASPIREdb.store.SubjectStore', 'ASPIREdb.view.CreateLabelWindow', 'ASPIREdb.view.LabelControlWindow',
              'ASPIREdb.TextDataDownloadWindow', 'ASPIREdb.view.report.BurdenAnalysisWindow' ] );

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

   listeners : {
      /**
       * Prevents the browser handling the right-click on the control.
       */
      itemcontextmenu : function(view, record, item, index, e) {
         // Stop the browser getting the event
         e.preventDefault();

         var contextMenu = new Ext.menu.Menu( {
            items : [ {
               text : 'Make label',
               handler : this.makeLabelHandler,
               scope : this,
            }, {
               text : 'Label Manager',
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
      dataIndex : 'labelIds',
      // This is very slow we need to rethink this
      renderer : function(value, metaData, record, row, col, store, gridView) {

         var ret = "";
         for (var i = 0; i < value.length; i++) {
            var label = this.visibleLabels[value[i]];
            if ( label === undefined ) {
               continue;
            }
            if ( label.isShown ) {
               ret += label.htmlLabel;
            }
         }
         return ret;
      },
      flex : 1
   }, {
      text : "# of variants",
      dataIndex : 'varientNos',
      tooltip : 'Total # of unfiltered variants',
      tooltipType : 'title',
      renderer : function(value) {
         return value;
      },
      flex : 1
   }, {
      text : "# of phenotypes",
      dataIndex : 'phenotypeNos',
      tooltip : 'Total # of unfiltered phenotypes',
      tooltipType : 'title',
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
            text : 'Make label...',
            disabled : true,
            handler : this.makeLabelHandler,
            scope : this
         }, {
            itemId : 'labelManager',
            text : 'Label Manager',
            disabled : false,
            handler : this.labelManagerHandler,
            scope : this
         } ]
      } );

      this.labelsButton = Ext.create( 'Ext.button.Split', {
         text : '<b>Labels</b>',
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
         tooltipType : 'title',
         icon : 'scripts/ASPIREdb/resources/images/icons/disk.png',
      } );

      this.burdenAnalysisButton = Ext.create( 'Ext.Button', {
         itemId : 'burdenAnalysisButton',
         text : 'Burden analysis',
         handler : this.burdenAnalysisHandler,
         scope : this,
         tooltip : 'Perform burden analysis on the filtered variants',
         tooltipType : 'title',
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
      ASPIREdb.EVENT_BUS.on( 'filter_submit', this.filterSubmitHandler, this );

      // when subject selected
      // this.on( 'selectionchange', me.selectionChangeHandler, me );
      this.on( 'cellclick', me.selectionChangeHandler, me );

      // when subject label is removed
      ASPIREdb.EVENT_BUS.on( 'subject_label_removed', this.labelRemovedHandler, this );

      // when subject label is removed
      ASPIREdb.EVENT_BUS.on( 'subject_label_updated', this.labelUpdateHandler, this );

      ASPIREdb.EVENT_BUS.on( 'select_subject_from_variant_grid', this.selectSubjectHandler, this );

      // when subject label is shown
      ASPIREdb.EVENT_BUS.on( 'subject_label_changed', function() {

         me.getView().refresh();

      }, this );
   },

   selectSubjectHandler : function(subjectIds) {

      var grid = this;

      grid.getSelectionModel().deselectAll();

      grid.selModel.select( grid.store.find( 'id', subjectIds[0] ) );

      // use just to make sure selected record is at the top
      grid.getView().scrollBy( {
         x : 0,
         y : 1000
      } );
      grid.getView().focusRow( grid.store.find( 'id', subjectIds[0] ) );

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
   filterSubmitHandler : function(filterConfigs) {

      var me = this;
      me.filterConfigs = filterConfigs;
      me.setLoading( true );
      me.getStore().removeAll();

      // DWR : get subjects match the subject filter
      // configuration
      QueryService.querySubjects( filterConfigs, {
         callback : function(pageLoad) {
            me.valueObjects = pageLoad.items;

            // load existing subject labels
            me.visibleLabels = me.createVisibleLabels( me.valueObjects );

            var data = [];

            console.log( me.valueObjects.length + " subjects being processed into value objects" );
            // find the number of subjects
            // filtered

            ProjectService.numSubjects( filterConfigs[0].projectIds, {
               callback : function(NoOfSubjects) {
                  /*
                   * if ( NoOfSubjects > me.valueObjects.length ) { me.setTitle( "Subject :" + me.valueObjects.length + "
                   * of " + NoOfSubjects + " filtered" ); } else if ( NoOfSubjects == me.valueObjects.length )
                   * me.setTitle( "Subject" );
                   */
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

         }
      } );
   },

   /**
    * This method called when subject are selected in the subject grid
    */
   selectionChangeHandler : function() {
      this.selSubjects = this.getSelectionModel().getSelection();
      this.selectAllStatus = 'No';

      if ( this.selSubjects.length === 0 ) {
         this.down( '#makeLabel' ).disable();
         // return;
      } else {
         this.down( '#makeLabel' ).enable();
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

      var selSubjectIds = [];
      for (var i = 0; i < me.selSubjects.length; i++) {
         selSubjectIds.push( me.selSubjects[i].data.id );
      }

      Ext.define( 'ASPIREdb.view.CreateLabelWindowSubject', {
         isSubjectLabel : true,
         title : 'Create Subject Label',
         extend : 'ASPIREdb.view.CreateLabelWindow',

         // override
         onOkButtonClick : function() {

            var labelCombo = this.down( "#labelCombo" );
            var vo = this.getLabel();
            if ( vo === null ) {
               return;
            }
            var labelIndex = labelCombo.getStore().findExact( 'display', vo.name );
            if ( labelIndex != -1 ) {
               // activate confirmation
               // window
               Ext.MessageBox.confirm( 'Label already exist', 'Label already exist. Add into it ?', function(btn) {
                  if ( btn === 'yes' ) {
                     me.addLabelHandler( vo, selSubjectIds );
                     this.hide();
                     ASPIREdb.EVENT_BUS.fireEvent( 'subject_label_created' );
                  }

               }, this );

            } else {
               me.addLabelHandler( vo, selSubjectIds );
               this.hide();
               ASPIREdb.EVENT_BUS.fireEvent( 'subject_label_created' );
            }

         }
      } );

      var labelWindow = new ASPIREdb.view.CreateLabelWindowSubject();
      labelWindow.show();

   },

   /**
    * Add the label to the store
    * 
    * @param: label value object, selected subject Ids
    */
   addLabelHandler : function(vo, selSubjectIds) {

      var me = this;

      // store in database
      SubjectService.addLabel( selSubjectIds, vo, {
         callback : function(addedLabel) {

            addedLabel.isShown = true;
            LabelService.updateLabel( addedLabel );

            var existingLab = me.visibleLabels[addedLabel.id];
            if ( existingLab === undefined ) {
               me.visibleLabels[addedLabel.id] = addedLabel;
            } else {
               existingLab.isShown = true;
            }

            // update local store
            for (var i = 0; i < me.selSubjects.length; i++) {
               me.selSubjects[i].get( 'labelIds' ).push( addedLabel.id );
            }

            // refresh grid
            me.getView().refresh();
         }
      } );

   },

   /**
    * Remove labels from subjects in local store.
    */
   removeLabelsFromSubjects : function(subjects, labelsToRemove) {
      // removing to visible label to show in the subject grid
      // this.visibleLabels.pop(this.visibleLabels[labelsToRemove.id]);

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
    * Update Subject labels in local store.
    */
   updateSubjectsLabels : function(subjectIds, labelToUpdate) {
      // adding to visible label to show in the subject grid
      this.visibleLabels[labelToUpdate.id] = labelToUpdate;

      for (var i = 0; i < subjectIds.length; i++) {
         // select selected subjects in subject grid
         var selectedSubjectId = this.store.find( 'id', subjectIds[i] );
         var selectionModel = this.getSelectionModel();
         selectionModel.doSelect( this.store.data.items[selectedSubjectId] );
         var record = this.getSelectionModel().getSelection();

         var labelIds = record[0].get( 'labelIds' );
         labelIds.push( labelToUpdate.id );

      }
   },

   /**
    * Label changed. Update subjects label in grid.
    */
   labelUpdateHandler : function(selSubjectIds, labelToUpdate) {
      var me = this;

      if ( selSubjectIds.length > 0 ) {
         me.updateSubjectsLabels( selSubjectIds, labelToUpdate );
      }

      ASPIREdb.EVENT_BUS.fireEvent( 'subjects_label_changed' );

      me.getView().refresh();
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

      for (var i = 0; i < this.selSubjects.length; i++) {
         selectedSubjectIds.push( this.selSubjects[i].data.id );
      }

      // check visibleLabels and make sure it's up-to-date
      // use store instead of value objects because value objects may
      // already be stale
      me.visibleLabels = me.updateVisibleLabelsFromStore();

      var labelControlWindow = Ext.create( 'ASPIREdb.view.LabelControlWindow', {
         visibleLabels : me.visibleLabels,
         isSubjectLabel : true,
         selectedOwnerIds : selectedSubjectIds,
         title : "Subject Label Manager"
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
      this.selectAllStatus = 'No';
   },

   burdenAnalysisHandler : function() {
      var reportWindow = Ext.create( 'ASPIREdb.view.report.BurdenAnalysisWindow' );
      var variantStore = Ext.StoreMgr.lookup( 'variantGrid' );
      reportWindow.createAndShow( variantStore );
   }

} );
