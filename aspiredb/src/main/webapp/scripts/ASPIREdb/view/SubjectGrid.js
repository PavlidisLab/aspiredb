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
              'ASPIREdb.TextDataDownloadWindow' ] );

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
               text : 'Label settings',
               handler : this.labelSettingsHandler,
               scope : this,
            } ]
         } );

         contextMenu.showAt( e.getX(), e.getY() );
      }
   },

   columns : [
              {
                 text : "Subject Id",
                 dataIndex : 'patientId',
                 flex : 1
              },
              {
                 text : "Labels",
                 dataIndex : 'labelIds',
                 // This is very slow we need to rethink this
                 renderer : function(value) {

                    var ret = "";
                    for (var i = 0; i < value.length; i++) {
                       var label = this.visibleLabels[value[i]];
                       if ( label == undefined ) {
                          continue;
                       }
                       if ( label.isShown ) {
                          var fontcolor = (parseInt( label.colour, 16 ) > 0xffffff / 2) ? 'black' : 'white';
                          ret += "<font color=" + fontcolor + "><span style='background-color: " + label.colour
                             + "'>&nbsp&nbsp" + label.name + "&nbsp</span></font>&nbsp&nbsp&nbsp";
                       }
                    }
                    return ret;
                 },
                 flex : 1
              }, {
                 text : "# of variants",
                 dataIndex : 'varientNos',
                 renderer : function(value) {
                    return value;
                 },
                 flex : 1
              }, {
                 text : "# of phenotypes",
                 dataIndex : 'phenotypeNos',
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
            itemId : 'labelSettings',
            text : 'Settings...',
            disabled : false,
            handler : this.labelSettingsHandler,
            scope : this
         } ]
      } );

      this.labelsButton = Ext.create( 'Ext.Button', {
         text : '<b>Labels</b>',
         itemId : 'labelsButton',
         menu : this.labelsMenu
      } );

      this.selectAllButton = Ext.create( 'Ext.Button', {
         itemId : 'selectAll',
         text : 'Select All',
         handler : this.selectAllHandler,
         scope : this
      } );

      this.deselectAllButton = Ext.create( 'Ext.Button', {
         itemId : 'deselectAll',
         text : 'Clear All',
         handler : this.deselectAllHandler,
         scope : this
      } );

      this.saveButton = Ext.create( 'Ext.Button', {
         itemId : 'saveButton',
         text : '',
         tooltip : 'Download table contents as text',
         icon : 'scripts/ASPIREdb/resources/images/icons/disk.png',
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
      this.on( 'selectionchange', me.selectionChangeHandler, me );

      // when subject label change
      ASPIREdb.EVENT_BUS.on( 'subject_Label_changed', this.refreshGridView, this );

   },

   /**
    * Load subject labels created by the user
    * 
    * @return visibleLabels
    */
   createVisibleLabels : function() {
      var visibleLabels = [];
      var suggestionContext = new SuggestionContext();
      suggestionContext.activeProjectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds();

      // load all labels created by this user
      SubjectService.suggestLabels( suggestionContext, {
         callback : function(labels) {
            for ( var idx in labels) {
               var label = labels[idx];
               visibleLabels[label.id] = label;
            }
         }
      } );

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
      // load existing subject labels
      me.visibleLabels = me.createVisibleLabels();

      // DWR : get subjects match the subject filter
      // configuration
      QueryService.querySubjects( filterConfigs, {
         callback : function(pageLoad) {
            me.valueObjects = pageLoad.items;

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
                  if ( aLabel == undefined ) {
                     aLabel = val.labels[j];
                  }

                  labelIds.push( aLabel.id );
               }

               // create summary of number
               // of variants
               val.numOfPhenotypes;

               var row = [ val.id, val.patientId, labelIds, val.variants, val.numOfPhenotypes ];
               data.push( row );
            }

            me.store.loadData( data );

            me.setLoading( false );

            // refresh grid
            me.getView().refresh();

            var ids = [];
            for (var i = 0; i < me.valueObjects.length; i++) {
               var o = me.valueObjects[i];
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

      if ( this.selSubjects.length == 0 ) {
         this.down( '#makeLabel' ).disable();
         return;
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
         ASPIREdb.EVENT_BUS.fireEvent( 'subject_selected', null );

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
         extend : 'ASPIREdb.view.CreateLabelWindow',

         // override
         onOkButtonClick : function() {

            var labelCombo = this.down( "#labelCombo" );
            var vo = this.getLabel();
            if ( vo == null ) {
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
            if ( existingLab == undefined ) {
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
    * Refresh grid view by reloading data from database because it was updated
    */
   refreshGridView : function() {
      var me = this;
      me.filterSubmitHandler( me.filterConfigs );
      me.getView().refresh();

   },

   /**
    * Display LabelSettingsWindow
    * 
    * @param :
    *           event
    */
   labelSettingsHandler : function(event) {
      var me = this;

      var selectedSubjectIds = [];

      for (var i = 0; i < this.selSubjects.length; i++) {
         selectedSubjectIds.push( this.selSubjects[i].data.id );
      }

      var labelControlWindow = Ext.create( 'ASPIREdb.view.LabelControlWindow', {
         visibleLabels : me.visibleLabels,
         isSubjectLabel : true,
         selectedIds : selectedSubjectIds,
         title : "Subject Label Settings"
      } );

      labelControlWindow.show();
   },

   /**
    * When all the subjects are sselected this is executed
    */
   selectAllHandler : function() {
      // if (this.selectAllStatus=='No'){
      this.cancelBubble = true;
      // boolean true to suppressEvent
      this.getSelectionModel().selectAll( true );
      this.selectionChangeHandler();
      // this.selectAllStatus ='Yes';
      // }

   },

   deselectAllHandler : function() {
      this.cancelBubble = true;
      this.getSelectionModel().deselectAll();
      this.selectionChangeHandler();
   }

} );
