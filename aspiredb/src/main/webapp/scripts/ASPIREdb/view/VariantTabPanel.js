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

Ext.require( [ 'ASPIREdb.view.Ideogram', 'Ext.tab.Panel', 'Ext.selection.RowModel',
              'ASPIREdb.view.GeneHitsByVariantWindow', 'ASPIREdb.ActiveProjectSettings',
              'ASPIREdb.view.VariantGridCreator', 'ASPIREdb.view.GeneGridCreator', 'ASPIREdb.IdeogramDownloadWindow',
              'Ext.data.ArrayStore', 'Ext.form.ComboBox', 'ASPIREdb.view.SubjectGrid',
              'ASPIREdb.view.report.VariantReportWindow', 'ASPIREdb.view.VariantCompoundHeterozygoteWindow' ] );

/**
 * Variant Tab Panel contains both Ideogram view and Variant table view
 */
Ext.define( 'ASPIREdb.view.VariantTabPanel', {
   /**
    * @memberOf ASPIREdb.view.VariantTabPanel
    */
   extend : 'Ext.tab.Panel',
   alias : 'widget.variantTabPanel',
   title : 'Variant',

   dockedItems : [ {
      xtype : 'toolbar',
      itemId : 'variantTabPanelToolbar',
      dock : 'top'

   } ],

   id : 'variantTabPanel',

   items : [ {
      xtype : 'ideogram',
      itemId : 'ideogram'
   } ],

   config : {
      // selected subjects records in the grid
      selectedVariants : [],
      loadedSubjects : [],
      selectedSubjects : [],
      loadedVariants : [],
      property : new VariantTypeProperty(),
      // the current filters used
      filterConfigs : [],
   },

   bbar : [ {
      xtype : 'label',
      itemId : 'statusbar',
      html : ''
   } ], // bbar

   constructor : function(cfg) {
      this.initConfig( cfg );
      this.callParent( arguments );
   },

   initComponent : function() {
      this.callParent();

      var ref = this;

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

      this.actionsMenu = Ext.create( 'Ext.menu.Menu', {
         items : [ {
            itemId : 'viewInUCSC',
            text : 'View in UCSC Genome Browser',
            disabled : true,
            handler : this.viewInUCSCHandler,
            scope : this
         }, {
            itemId : 'viewGenes',
            text : 'View genes',
            disabled : true,
            handler : this.viewGenesHandler,
            scope : this
         }, {
            itemId : 'viewCompoundHeterozygotes',
            text : 'View compound heterozygotes',
            disabled : false,
            handler : this.viewCompoundHeterozygotes,
            scope : this
         } ]
      } );

      this.actionsButton = Ext.create( 'Ext.button.Split', {
         text : '<b>Actions</b>',
         itemId : 'actionsButton',
         menu : this.actionsMenu
      } );

      this.reportButton = Ext.create( 'Ext.Button', {
         itemId : 'reportButton',
         text : 'Reports',
         disabled : false,
         handler : this.showReportHandler,
         scope : this,
         tooltip : 'Generate reports on the filtered variants',
         tooltipType : 'title',
      } );

      this.selectAllButton = Ext.create( 'Ext.Button', {
         itemId : 'selectAll',
         text : 'Select All',
         handler : this.selectAllHandler,
         scope : this,
         hidden : true,
      } );

      this.deselectAllButton = Ext.create( 'Ext.Button', {
         itemId : 'deselectAll',
         text : 'Clear All',
         disabled : false,
         handler : this.deselectAllHandler,
         scope : this,
         hidden : true,
      } );

      this.tbSeparator = Ext.create( 'Ext.toolbar.Separator' );

      this.tbSpacer = Ext.create( 'Ext.toolbar.Spacer' );

      this.tbFill = Ext.create( 'Ext.toolbar.Fill' );

      this.saveButton = Ext.create( 'Ext.Button', {
         id : 'saveButton',
         text : '',
         tooltip : 'Download table contents as text',
         tooltipType : 'title',
         icon : 'scripts/ASPIREdb/resources/images/icons/disk.png',
         hidden : true,
      } );

      this.exportButton = Ext.create( 'Ext.Button', {
         id : 'exportButton',
         text : '',
         tooltip : 'Download ideogram as png',
         tooltipType : 'title',
         icon : 'scripts/ASPIREdb/resources/images/icons/export.png'

      } );

      this.zoomInButton = Ext.create( 'Ext.Button', {
         id : 'zoomOutButton',
         text : '',
         tooltip : 'Zoom out ideogram',
         tooltipType : 'title',
         icon : 'scripts/ASPIREdb/resources/images/icons/zoom_in.png'

      } );

      this.zoomOutButton = Ext.create( 'Ext.Button', {
         id : 'zoomInButton',
         text : '',
         hidden : true,
         tooltip : 'Zoom in ideogram',
         tooltipType : 'title',
         icon : 'scripts/ASPIREdb/resources/images/icons/zoom_out.png'

      } );

      var data = [ [ 'type', 'Variant type' ], [ 'cnvType', 'CNV type' ], [ 'commonCNV', 'Common CNV' ],
                  [ 'characteristics', 'Characteristics' ], [ 'inheritance', 'Inheritance' ],
                  [ 'arrayReport', 'Array Report' ], [ 'arrayPlatform', 'Array Platform' ],
                  [ 'labels', 'Variant Labels' ], [ 'subjectLabels', 'Subject Labels' ] ];

      var variantCharacteristics = Ext.create( 'Ext.data.ArrayStore', {
         storeId : 'varChar',
         fields : [ {
            name : 'id',
            type : 'string'
         }, {
            name : 'name',
            type : 'string'
         } ],
         data : data,
         autoLoad : true,
         autoSync : true,
      } );

      this.colourVariantByCombo = Ext.create( 'Ext.form.ComboBox', {
         itemId : 'colorCode',
         fieldLabel : 'Color code',
         store : variantCharacteristics,
         displayField : 'name',
         valueField : 'id',
         queryMode : 'local',
         editable : false,
         forceSelection : true,
      } );

      this.colourVariantByCombo.on( 'select', this.colourVariantByHandler, this );

      // adding buttons to toolbar in filterSubmitHandler with
      // the grid
      // because extJS was
      // bugging out when we added the dynamically created
      // grid afterwords
      ASPIREdb.EVENT_BUS.on( 'filter_submit', this.filterSubmitHandler, this );

      // when subject label change
      ASPIREdb.EVENT_BUS.on( 'variant_label_changed', this.variantLabelUpdateHandler, this );

      // when variant label created
      ASPIREdb.EVENT_BUS.on( 'variant_label_created', this.variantLabelUpdateHandler, this );

      // when variant label removed
      ASPIREdb.EVENT_BUS.on( 'variant_label_removed', this.variantLabelRemoved, this );

      // when variant label changes
      ASPIREdb.EVENT_BUS.on( 'subject_label_changed', this.subjectLabelUpdateHandler, this );

      // when subject label created
      ASPIREdb.EVENT_BUS.on( 'subject_label_created', this.subjectLabelUpdateHandler, this );

      // when variant label removed
      ASPIREdb.EVENT_BUS.on( 'subject_label_removed', this.subjectLabelUpdateHandler, this );

      // update local store
      ASPIREdb.EVENT_BUS.on( 'variant_label_updated', function(selectedIds, addedLabel) {

         if ( selectedIds == undefined || addedLabel == undefined ) {
            return;
         }

         // update the label store cache for rendering
         var grid = ref.down( '#variantGrid' );
         var existingLab = grid.visibleLabels[addedLabel.id];
         if ( existingLab == undefined ) {
            grid.visibleLabels[addedLabel.id] = addedLabel;
         } else {
            existingLab.isShown = true;
         }

         for (var i = 0; i < selectedIds.length; i++) {
            var labelIds = grid.store.findRecord( 'id', selectedIds[i] ).data.labelIds;
            labelIds.push( addedLabel.id )
         }
         /*
          * var currentlySelectedRecords = ref.getVariantRecordSelection(); for (var i = 0; i <
          * currentlySelectedRecords.length; i++) { var labelIds = currentlySelectedRecords[i].get( 'labelIds' ); //
          * console.log('labelIds = ' + Ext.JSON.encode(labelIds) + " rec = " + currentlySelectedRecords[i].get( 'id' //
          * )); labelIds.push( addedLabel.id ); }
          */

         if ( ref.getActiveTab().itemId == 'ideogram' ) {
            ref.newIdeogramLabel = true;
         }

         ASPIREdb.EVENT_BUS.fireEvent( 'variant_label_created' );
      } );

      ASPIREdb.EVENT_BUS.on( 'property_changed', function(property) {
         ref.property = [];
         ref.property = property;
      } );

      ASPIREdb.EVENT_BUS.on( 'subjects_loaded', function(subjectIds) {
         ref.loadedSubjects = [];
         ref.loadedSubjects = subjectIds;
      } );

      // when subjects selected it is focused in variant grid
      ASPIREdb.EVENT_BUS.on( 'subject_selected', this.subjectSelectionHandler, this );

      this.saveButton.on( 'click', function() {
         ref.saveButtonHandler();
      } );

      this.exportButton.on( 'click', function() {
         ref.exportButtonHandler();
      } );

      this.zoomInButton.on( 'click', function() {
         ref.zoomInButtonHandler();
      } );

      this.zoomOutButton.on( 'click', function() {
         ref.zoomOutButtonHandler();
      } );

      // selection is GenomicRange{baseEnd, baseStart,
      // chromosome}
      this.getComponent( 'ideogram' ).on( 'GenomeRegionSelectionEvent', function(selection) {
         ref.ideogramSelectionChangeHandler( null, ref.getVariantRecordSelection() );
      } );

      // activate/deactiveButtons based on activeTab
      this.on( 'beforetabchange', function(tabPanel, newCard, oldCard, eOpts) {

         var currentlySelectedRecords = [];
         var ideogram = ref.getComponent( 'ideogram' );

         if ( newCard.itemId == 'ideogram' ) {

            currentlySelectedRecords = this.getIdeogramVariantRecordSelection();
            this.selectAllButton.hide();
            this.deselectAllButton.hide();
            ideogram.showColourLegend();
            this.colourVariantByCombo.show();
            this.zoomOutButton.show();
            this.zoomInButton.show();
            this.exportButton.show();
            this.saveButton.hide();

         } else {
            // newCard is the grid
            currentlySelectedRecords = this.getSelectedVariants();
            this.selectAllButton.show();
            this.deselectAllButton.show();
            ideogram.hideColourLegend();
            this.colourVariantByCombo.hide();
            this.zoomOutButton.hide();
            this.zoomInButton.hide();
            this.exportButton.hide();
            this.saveButton.show();
         }

         this.enableActionButtonsBySelectedRecords( currentlySelectedRecords );

      } );

   },

   getSelectedVariants : function() {
      var grid = this.down( '#variantGrid' );
      return grid.getSelectionModel().getSelection();
   },

   createVariantGrid : function(vvos, properties, variantGenes) {
      var ref = this;

      var grid = ASPIREdb.view.VariantGridCreator.createVariantGrid( vvos, properties, variantGenes );
      // var grid2 = ASPIREdb.view.GeneGridCreator.createGeneGrid( vos, properties );

      grid.on( 'itemcontextmenu', function(view, record, item, index, e) {
         // Stop the browser getting the event
         e.preventDefault();

         var contextMenu = new Ext.menu.Menu( {
            items : [ {
               text : 'Make label',
               handler : ref.makeLabelHandler,
               scope : ref,
            }, {
               text : 'Label Manager',
               handler : ref.labelManagerHandler,
               scope : ref,
            } ]
         } );

         contextMenu.showAt( e.getX(), e.getY() );
      }, this );

      ref.remove( 'variantGrid', true );
      ref.remove( 'geneGrid', true );

      // when subjects are
      // selected
      grid.on( 'selectionchange', ref.selectionChangeHandler, ref );

      grid.on( 'show', function() {
         if ( ref.newIdeogramLabel ) {
            grid.getView().refresh();
            ref.newIdeogramLabel = undefined;
         }
         ref.focusSelectedVariants();
      } );

      ref.add( grid );
      // ref.add( grid2 );
   },

   /**
    * Filter the variants of the subject selected. Initially it loads all the variants associated with all the subjects.
    * 
    * @param filterConfigs -
    *           variant filters
    * @param legendProperty -
    *           which legend to display in the ideogram (e.g. Variant Label)
    */
   filterSubmitHandler : function(filterConfigs, legendProperty) {

      var ref = this;

      ref.filterConfigs = filterConfigs;

      ref.setLoading( true );

      VariantService.suggestProperties( function(properties) {

         QueryService.queryVariants( filterConfigs, {
            callback : function(pageLoad) {

               var vvos = pageLoad.items;
               ref.loadedVariants = vvos;
               var variantPatientIds = [];
               var projectId = ASPIREdb.ActiveProjectSettings.getActiveProjectIds()[0];

               for (var i = 0; i < vvos.length; i++) {
                  if ( variantPatientIds.indexOf( vvos[i].patientId ) == -1 )
                     variantPatientIds.push( VariantService.getSubjectVariants( projectId, vvos[i].patientId ) );

               }

               var variantIds = [];

               for (var k = 0; k < vvos.length; k++) {
                  variantIds.push( vvos[k].id );
               }

               // GeneService.getGeneValueObjectsInsideVariants( variantIds, {
               // callback : function(vos) {
               // console.log( 'variant gene value objects' + vos );

               // ASPIREdb.view.GeneHitsByVariantWindow.getComponent( 'geneHitsByVariantGrid'
               // ).setLodedvariantvalueObjects(vos );
               // ASPIREdb.view.GeneHitsByVariantWindow.populateGrid( vos );

               ProjectService.numVariants( filterConfigs[0].projectIds, {
                  callback : function(NoOfVariants) {
                     /*
                      * if ( NoOfVariants > vvos.length ) { ref.setTitle( "Variant :" + vvos.length + " of " +
                      * NoOfVariants + " filtered" ); } else if ( NoOfVariants == vvos.length ) ref.setTitle( "Variant" );
                      */
                     ref.down( '#statusbar' ).update(
                        ref.loadedVariants.length + " / " + NoOfVariants + " variants loaded" );
                  }
               } );

               var ideogram = ref.getComponent( 'ideogram' );
               ideogram.drawChromosomes();
               ideogram.drawVariants( vvos );

               var d = new Date();
               GeneService.getGenesPerVariant( variantIds, {
                  callback : function(variantGenes) {
                     ref.createVariantGrid( vvos, properties, variantGenes )
                     ref.setLoading( false );
                     console.log( 'Getting genes for ' + variantIds.length + ' variants took ' + (new Date() - d)
                        + ' ms' )
                  },
                  errorHandler : function(message, exception) {
                     Ext.Msg.alert( 'Error', message )
                     console.log( message )
                     console.log( dwr.util.toDescriptiveString( exception.stackTrace, 3 ) )
                  }
               } );

               var toolbar = ref.getDockedComponent( 'variantTabPanelToolbar' );

               toolbar.add( ref.actionsButton );
               toolbar.add( ref.labelsButton );
               toolbar.add( ref.reportButton );
               toolbar.add( ref.tbSeparator );

               toolbar.add( ref.selectAllButton );
               toolbar.add( ref.deselectAllButton );

               toolbar.add( ref.colourVariantByCombo );
               toolbar.add( ref.tbSpacer );

               toolbar.add( ref.zoomInButton );
               toolbar.add( ref.zoomOutButton );

               toolbar.add( ref.tbFill );
               toolbar.add( ref.saveButton );
               toolbar.add( ref.exportButton );

               // refresh the legend (e.g. Variant Labels) in ideogram
               if ( legendProperty != null ) {
                  ASPIREdb.EVENT_BUS.fireEvent( 'colorCoding_selected' );
                  ASPIREdb.EVENT_BUS.fireEvent( 'property_changed', legendProperty );
                  ref.redrawIdeogram( legendProperty );
               }

               // }
               // } );
            }
         } );

      } );

   },

   /**
    * Remove labels from variants in local store.
    */
   removeLabelsFromVariants : function(variants, labelsToRemove) {
      for (var i = 0; i < variants.length; i++) {
         var labelIds = variants[i].data.labelIds;
         for (var k = 0; k < labelsToRemove.length; k++) {
            var idx = labelIds.indexOf( labelsToRemove[k].id );
            if ( idx > -1 ) {
               labelIds.splice( idx, 1 );
            }
         }
      }
   },

   /**
    * When a Variant Label is removed for a selected Ids
    */
   variantLabelRemoved : function(variantIds, labelsToRemove) {
      if ( variantIds.length > 0 ) {
         this.removeLabelsFromVariants( this.getVariantRecordSelection(), labelsToRemove );
      } else {
         var allVariants = this.getComponent( 'variantGrid' ).store.data.items;
         this.removeLabelsFromVariants( allVariants, labelsToRemove );
      }
      this.variantLabelUpdateHandler();
   },

   /**
    * Refresh grid view by reloading data from database because it was updated
    */
   variantLabelUpdateHandler : function() {

      var property = new VariantLabelProperty();
      property.name = 'Labels';
      property.displayName = 'Variant Labels';

      var me = this;
      // me.filterSubmitHandler( me.filterConfigs, property );

      // refresh the variant in grid
      me.down( '#variantGrid' ).getView().refresh();

   },

   /**
    * Display a variant summary report as charts
    */
   showReportHandler : function() {
      var reportWindow = Ext.create( 'ASPIREdb.view.report.VariantReportWindow' );
      reportWindow.createAndShow( this.down( '#variantGrid' ).store );
   },

   /**
    * Refresh the selected subjects in ideogram
    */
   subjectLabelUpdateHandler : function() {
      // this.filterSubmitHandler( this.filterConfigs );
      var ideogram = this.getComponent( 'ideogram' );

      if ( ideogram.colourLegend.isVisible() ) {
         ideogram.hideColourLegend();

         var property = new SubjectLabelProperty();
         property.name = 'Subject Labels';
         property.displayName = 'Subject Label';
         // ASPIREdb.EVENT_BUS.fireEvent('property_changed',property);
         this.redrawIdeogram( property );

      }
   },

   /**
    * 
    */
   colourVariantByHandler : function(combo, records, eOpts) {

      var ideogram = this.getComponent( 'ideogram' );

      if ( ideogram.isVisible() ) {
         var selectedValue = records[0].data.id;
         ASPIREdb.EVENT_BUS.fireEvent( 'colorCoding_selected' );

         switch (selectedValue) {
            case 'type': {
               var property = new VariantTypeProperty();
               property.name = 'type';
               property.displayName = 'Variant Type';
               ASPIREdb.EVENT_BUS.fireEvent( 'property_changed', property );
               this.redrawIdeogram( property );
               break;
            }
            case 'cnvType': {
               var property = new CNVTypeProperty();
               property.name = 'cnvType';
               property.displayName = 'CNV Type';
               ASPIREdb.EVENT_BUS.fireEvent( 'property_changed', property );
               this.redrawIdeogram( property );
               break;
            }
            case 'characteristics': {
               var property = new CharacteristicProperty();
               property.name = 'Characteristics';
               property.displayName = 'Characteristics';
               ASPIREdb.EVENT_BUS.fireEvent( 'property_changed', property );
               this.redrawIdeogram( property );
               break;
            }
            case 'inheritance': {
               var property = new CharacteristicProperty();
               property.name = 'Inheritance';
               property.displayName = 'Inheritance';
               ASPIREdb.EVENT_BUS.fireEvent( 'property_changed', property );
               this.redrawIdeogram( property );
               break;
            }
            case 'subjectLabels': {
               var property = new SubjectLabelProperty();
               property.name = 'Subject Labels';
               property.displayName = 'Subject Label';
               ASPIREdb.EVENT_BUS.fireEvent( 'property_changed', property );
               this.redrawIdeogram( property );
               break;
            }
            case 'labels': {
               var property = new VariantLabelProperty();
               property.name = 'Labels';
               property.displayName = 'Variant Labels';
               ASPIREdb.EVENT_BUS.fireEvent( 'property_changed', property );
               this.redrawIdeogram( property );
               break;
            }
            case 'commonCNV': {
               var property = new CharacteristicProperty();
               property.name = 'Common CNV';
               property.displayName = 'Common CNV';
               ASPIREdb.EVENT_BUS.fireEvent( 'property_changed', property );
               this.redrawIdeogram( property );
            }
            case 'arrayReport': {
               var property = new CharacteristicProperty();
               property.name = 'Array Report';
               property.displayName = 'Array Report';
               ASPIREdb.EVENT_BUS.fireEvent( 'property_changed', property );
               this.redrawIdeogram( property );
               break;
            }
            case 'arrayPlatform': {
               var property = new CharacteristicProperty();
               property.name = 'Array Platform';
               property.displayName = 'Array Platform';
               ASPIREdb.EVENT_BUS.fireEvent( 'property_changed', property );
               this.redrawIdeogram( property );
               break;
            }
         }

      }

   },

   /**
    * Redraw the ideogram based on colour coding
    */
   redrawIdeogram : function(property) {
      var ideogram = this.getComponent( 'ideogram' );

      ideogram.setDisplayedProperty( property );
      ideogram.drawChromosomes();
      ideogram.drawColouredVariants( this.loadedVariants, false );
      ideogram.showColourLegend();
   },

   /**
    * When subjects are selected in the subject grid highlight the variants of selected subjects in ideogram and in
    * table view
    */
   subjectSelectionHandler : function(subjectIds, unselectRows) {

      this.selectedSubjects = subjectIds;
      var grid = this.down( '#variantGrid' );

      grid.getSelectionModel().deselectAll();

      this.gridPanelSubjectSelection( subjectIds );
      this.ideogramSubjectSelection( subjectIds );

      grid.getView().refresh();
   },

   ideogramSubjectSelection : function(subjectIds) {

      var ideogram = this.getComponent( 'ideogram' );
      ideogram.drawChromosomes();
      var projectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds();
      var ref = this;

      // heighlight the selected subject in ideogram
      SubjectService.getSubjects( projectIds[0], subjectIds, {
         callback : function(subjectValueObjects) {

            if ( subjectValueObjects == null ) {
               return;
            }

            var subjectIDS = [];
            var patientIDS = [];
            for (var i = 0; i < subjectValueObjects.length; i++) {
               subjectIDS.push( subjectValueObjects[i].id );
               patientIDS.push( subjectValueObjects[i].patientId );
            }
            ideogram.drawVariantsWithSubjectsHighlighted( subjectIDS, ref.loadedVariants );

         }
      } );
   },

   focusSelectedVariants : function() {

      var grid = this.down( '#variantGrid' );
      var selectedRecords = grid.getSelectionModel().getSelection();

      if ( selectedRecords.length > 0 ) {
         grid.getView().focusRow( grid.store.indexOfId( selectedRecords[0].data.id ) );
         // use just to make sure selected record is at the top
         grid.getView().scrollBy( {
            x : 0,
            y : 1000
         } );
         grid.getView().focusRow( grid.store.indexOfId( selectedRecords[0].data.id ) );
      }
   },

   gridPanelSubjectSelection : function(subjectIds) {

      var me = this;

      var projectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds();

      var grid = this.down( '#variantGrid' );

      if ( grid.features != null && grid.features.length > 0 ) {
         // collapse all the grids first - to open only the
         // selected one
         grid.features[0].collapseAll();

         // expand only the selected subjects
         SubjectService.getSubjects( projectIds[0], subjectIds, {
            callback : function(selectedSubjectValueObjects) {
               for (var i = 0; i < selectedSubjectValueObjects.length; i++) {
                  var subject = selectedSubjectValueObjects[i];
                  grid.features[0].expand( subject.patientId, true );
               }
            }
         } );
      } else {

         // collapsable plugin disabled so no groups to expand / collapse
         // so just select the variants for the selected subjectIds
         // and scroll to view it

         SubjectService.getSubjects( projectIds[0], subjectIds, {
            callback : function(selectedSubjectValueObjects) {

               if ( selectedSubjectValueObjects == null ) {
                  return;
               }

               var selectedRecords = [];

               grid.store.sort( 'patientId' );

               for (var i = 0; i < selectedSubjectValueObjects.length; i++) {
                  var subject = selectedSubjectValueObjects[i];

                  grid.store.each( function(rec) {
                     if ( rec.get( 'patientId' ) == subject.patientId ) {
                        selectedRecords.push( rec );
                     }
                  } );
               }

               me.selectedVariants = selectedRecords;

               grid.selModel.select( selectedRecords );

               me.focusSelectedVariants();

            }
         } );

      }

   },
   selectionChangeHandler : function(model, records) {
      console.log( 'on grid selection change handler variant tab panel' );
      this.selectedVariants = records;

      this.enableActionButtonsBySelectedRecords( records );

   },

   selectAllHandler : function() {

      // boolean true to suppressEvent
      this.getComponent( 'variantGrid' ).getSelectionModel().selectAll( true );

      this.selectionChangeHandler( this.getComponent( 'variantGrid' ).getSelectionModel(), this.getComponent(
         'variantGrid' ).getSelectionModel().getSelection() );

   },

   deselectAllHandler : function() {
      // this.subjectSelectionHandler( this.loadedSubjects, true );

      this.getComponent( 'variantGrid' ).getSelectionModel().deselectAll();
   },

   ideogramSelectionChangeHandler : function(model, records) {

      this.enableActionButtonsBySelectedRecords( records );

   },

   saveButtonHandler : function() {

      var grid = this.getComponent( 'variantGrid' );

      if ( grid ) {
         ASPIREdb.TextDataDownloadWindow.showVariantsDownload( grid.getStore().getRange(), grid.columnHeaders );
      }

   },

   exportButtonHandler : function() {

      var ideogram = this.getComponent( 'ideogram' );
      var canvas = ideogram.getComponent( 'canvasBox' );
      var imgsrc = canvas.el.dom.toDataURL( 'image/png' );

      if ( imgsrc ) {
         ASPIREdb.IdeogramDownloadWindow.showIdeogramDownload( imgsrc );
      }

   },

   zoomInButtonHandler : function() {
      this.zoomInButton.setVisible( false );
      this.zoomOutButton.setVisible( true );
      var ideogram = this.getComponent( 'ideogram' );
      ideogram.changeZoom( 2, this.loadedVariants );

   },

   zoomOutButtonHandler : function() {
      this.zoomOutButton.setVisible( false )
      this.zoomInButton.setVisible( true );
      var ideogram = this.getComponent( 'ideogram' );
      ideogram.changeZoom( 1, this.loadedVariants );

   },

   viewGenesHandler : function() {
      ASPIREdb.view.GeneHitsByVariantWindow.clearGridAndMask();
      ASPIREdb.view.GeneHitsByVariantWindow.initGridAndShow( this.getSelectedVariantIds( this
         .getVariantRecordSelection() ) );
   },

   viewInUCSCHandler : function() {

      UCSCConnector.constructCustomTracksUrl( this.getSpanningGenomicRange( this.getVariantRecordSelection() ),
         ASPIREdb.ActiveProjectSettings.getActiveProjectIds(), function(ucscParams) {

            var ucscForm = Ext.create( 'Ext.form.Panel', {

            } );

            ucscForm.submit( {
               target : '_blank',
               url : ucscParams['url'],
               standardSubmit : true,
               method : "POST",
               params : {
                  clade : ucscParams['clade'],
                  org : ucscParams['org'],
                  db : ucscParams['db'],
                  hgct_customText : ucscParams['hgct_customText']
               },
               success : function() {
                  console.log( "ok" );
               },
               failure : function(response, opts) {
                  console.log( "failed" );
               },
               headers : {
                  'Content-Type' : 'multipart/form-data'
               }
            } );

         } );

   },

   viewCompoundHeterozygotes : function() {

      var ref = this;
      ref.setLoading( true );

      var variantStore = ref.down( '#variantGrid' ).store;

      var variantIds = ref.getSelectedVariantIds( ref.getVariantRecordSelection() );
      if ( variantIds.length == 0 ) {
         variantIds = grid.store.collect( 'id' );
      }

      var d = new Date();
      GeneService.getCompoundHeterozygotes( variantIds, {

         // variantGenes = Map<String.PatientId, Map<GeneValueObject, Collection<VariantValueObject>>>
         callback : function(variantGenes) {

            ref.setLoading( false );

            var myWin = Ext.create( "ASPIREdb.view.VariantCompoundHeterozygoteWindow" );

            myWin.initGridAndShow( variantStore, variantGenes );

            console.log( 'Found ' + variantGenes.length + ' subjects with potential compound heterozygotes which took '
               + (new Date() - d) + ' ms' )
         },
         errorHandler : function(message, exception) {
            Ext.Msg.alert( 'Error', message )
            console.log( message )
            console.log( dwr.util.toDescriptiveString( exception.stackTrace, 3 ) )
         }
      } );

   },

   enableActionButtonsBySelectedRecords : function(records) {

      if ( records.length > 0 ) {

         this.down( '#viewGenes' ).enable();
         this.down( '#makeLabel' ).enable();
      } else {

         this.down( '#viewGenes' ).disable();
         this.down( '#viewInUCSC' ).disable();
         this.down( '#makeLabel' ).disable();
         return;
      }

      if ( this.areOnSameChromosome( records ) ) {
         this.down( '#viewInUCSC' ).enable();
      } else {
         this.down( '#viewInUCSC' ).disable();
      }

   },

   getVariantRecordSelection : function() {

      if ( this.getActiveTab().itemId == 'ideogram' ) {

         return this.getIdeogramVariantRecordSelection();

      } else {
         return this.selectedVariants;
      }

   },

   getIdeogramVariantRecordSelection : function() {

      var ideogram = this.getComponent( 'ideogram' );

      var ideogramGenomicRange = ideogram.getSelection();

      if ( ideogramGenomicRange == null ) {
         return [];
      }

      var grid = this.getComponent( 'variantGrid' );

      var records = grid.getStore().getRange();

      var variantRecordsInsideRange = [];

      for (var i = 0; i < records.length; i++) {

         var genomicRange = {
            chromosome : records[i].data.chromosome,
            baseStart : records[i].data.baseStart,
            baseEnd : records[i].data.baseEnd
         };

         if ( this.secondGenomicRangeIsWithinFirst( genomicRange, ideogramGenomicRange ) ) {
            variantRecordsInsideRange.push( records[i] );
         }

      }

      return variantRecordsInsideRange;

   },

   /**
    * Assigns a Label
    * 
    */
   makeLabelHandler : function(event) {

      var me = this;

      var labelWin = Ext.create( 'ASPIREdb.view.CreateLabelWindow', {
         isSubjectLabel : false,
         title : 'Create Variant Label',
         extend : 'ASPIREdb.view.CreateLabelWindow',
         selectedIds : me.getSelectedVariantIds( me.getVariantRecordSelection() ),
      } );
      labelWin.show();
   },

   updateVisibleLabelsFromStore : function() {
      var ret = {};
      var visibleLabels = this.down( '#variantGrid' ).visibleLabels;
      var store = this.down( '#variantGrid' ).store;
      for (var i = 0; i < store.data.items.length; i++) {
         var labelIds = store.data.items[i].data.labelIds;
         for (var j = 0; j < labelIds.length; j++) {
            var labelId = labelIds[j];
            ret[labelId] = visibleLabels[labelId];
         }
      }
      return ret;
   },

   /**
    * Display labelManagerWindow
    */
   labelManagerHandler : function(event) {
      var me = this;
      var visibleLabels = me.down( '#variantGrid' ).visibleLabels;

      var currentlySelectedRecords = me.getVariantRecordSelection();
      var selectedVariantIds = [];
      for (var i = 0; i < currentlySelectedRecords.length; i++) {
         selectedVariantIds.push( currentlySelectedRecords[i].get( 'id' ) );
      }

      visibleLabels = me.updateVisibleLabelsFromStore();

      var labelControlWindow = Ext.create( 'ASPIREdb.view.LabelControlWindow', {
         visibleLabels : visibleLabels,
         isSubjectLabel : false,
         selectedOwnerIds : selectedVariantIds,
         title : 'Variant Label Manager'
      } );

      labelControlWindow.show();
   },

   getSelectedVariantIds : function(selectedVariantRecords) {

      var selectedVariantIds = [];

      for (var i = 0; i < selectedVariantRecords.length; i++) {
         selectedVariantIds.push( selectedVariantRecords[i].data.id );
      }

      return selectedVariantIds;
   },

   getSelectedPatientIds : function(selectedVariantRecords) {

      var selectedPatientIds = [];
      var me = this;

      for (var i = 0; i < selectedVariantRecords.length; i++) {
         selectedPatientIds.push( selectedVariantRecords[i].data.patientId );
      }

      return selectedPatientIds;
   },

   areOnSameChromosome : function(records) {

      if ( records.length < 1 )
         return false;

      var chromosome = records[0].data.chromosome;

      for (var i = 0; i < records.length; i++) {

         var otherChromosome = records[i].data.chromosome;

         if ( chromosome !== otherChromosome ) {
            return false;
         }

      }

      return true;

   },

   secondGenomicRangeIsWithinFirst : function(first, other) {
      if ( first.chromosome == other.chromosome ) {
         if ( first.baseStart >= other.baseStart && first.baseEnd <= other.baseEnd ) {
            return true;
         }
      }
      return false;
   },

   getSpanningGenomicRange : function(selectedVariants) {
      if ( !this.areOnSameChromosome( selectedVariants ) )
         return;

      var start = 2147483647;
      var end = -2147483648;
      var chromosome = selectedVariants[0].data.chromosome;
      for (var i = 0; i < selectedVariants.length; i++) {

         var variant = selectedVariants[i].data;

         if ( variant.chromosome == chromosome ) {
            if ( variant.baseStart < start )
               start = variant.baseStart;
            if ( variant.baseEnd > end )
               end = variant.baseEnd;
         }
      }

      // Genomic Range
      return {
         chromosome : chromosome,
         baseStart : start,
         baseEnd : end
      };

   }

} );
