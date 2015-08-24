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
Ext.require( [ 'Ext.Window', 'ASPIREdb.store.LabelStore', 'Ext.grid.column.Action', 'Ext.ux.CheckColumn',
              'Ext.form.field.*', 'Ext.picker.Color' ] );

var rowEditing = Ext.create( 'Ext.grid.plugin.RowEditing', {
   // clicksToMoveEditor: 1,
   clicksToEdit : 2,
   autoCancel : false
} );

var rowEditing = Ext.create( 'Ext.grid.plugin.RowEditing', {
   // clicksToMoveEditor: 1,
   clicksToEdit : 2,
   autoCancel : false
} );

var contextMenu = new Ext.menu.Menu( {
   itemId : 'contextMenu',
   displayField : 'colour',
   items : [ {
      text : colorPicker,
      scope : this,

   } ]
} );

var colorPicker = Ext.create( 'Ext.menu.ColorPicker', {
   displayField : 'colour',
   listeners : {
      select : function(picker, selColor) {
         console.log( 'picker value ' + picker.value );
         console.log( 'picker  :' + picker + 'cell color  ' + selColor );
         alert( selColor );
         // set the store label value
         ASPIREdb.EVENT_BUS.fireEvent( 'label_color_chnaged', selColor );

      }
   }
} );

/**
 * For editing, removing from Subjects / Variants, and deleting labels
 */
Ext.define( 'ASPIREdb.view.LabelControlWindow', {
   extend : 'Ext.Window',
   alias : 'widget.labelControlWindow',
   title : 'Label Manager',
   id : 'labelControlWindow',
   frame : 'true',
   closable : true,
   closeAction : 'destroy',
   layout : 'border',
   bodyStyle : 'padding: 5px;',
   layout : 'fit',
   width : 600,
   height : 400,
   renderTo : Ext.getBody(),
   modal : true,
   config : {
      visibleLabels : {}, // holds all the LabelValueObjects in memory
      isSubjectLabel : false,
      selectedOwnerIds : [],
      XValue : 0,
      YValue : 0,
   },
   listeners : {
      close : function() {
         if ( this.isSubjectLabel ) {
            ASPIREdb.EVENT_BUS.fireEvent( 'subject_label_changed' );
         } else {
            ASPIREdb.EVENT_BUS.fireEvent( 'variant_label_changed' );
         }
      }
   },
   constructor : function(cfg) {
      this.initConfig( cfg );
      this.callParent( arguments );
   },

   statics : {

      /**
       * Source : https://24ways.org/2010/calculating-color-contrast/
       */
      getContrastYIQ : function(hexcolor) {
         var r = parseInt( hexcolor.substr( 0, 2 ), 16 );
         var g = parseInt( hexcolor.substr( 2, 2 ), 16 );
         var b = parseInt( hexcolor.substr( 4, 2 ), 16 );
         var yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000;
         return (yiq >= 128) ? 'black' : 'white';
      },

      getHtmlLabel : function(label) {
         var fontColour = ASPIREdb.view.LabelControlWindow.getContrastYIQ( label.colour );
         var ret = "<font color=" + fontColour + "><span style='background-color: " + label.colour + "'>&nbsp&nbsp"
            + label.name + "&nbsp&nbsp</span></font>&nbsp&nbsp&nbsp";
         return ret;
      },
   },

   /**
    * dockedItems : [ { xtype : 'colorpicker', itemId : 'colorPicker', value : '00FFFF', // default dock : 'right' } ],
    */
   items : [ {
      xtype : 'container',
      layout : {
         type : 'border',
         defaultMargins : {
            top : 5,
            right : 5,
            left : 5,
            bottom : 5
         }
      },

      items : [
               {
                  xtype : 'grid',
                  region : 'center',
                  itemId : 'labelSettingsGrid',
                  store : Ext.create( 'ASPIREdb.store.LabelStore' ),
                  columns : [
                             {
                                header : 'Label',
                                dataIndex : 'id',
                                flex : 1,
                                renderer : function(id, meta, rec, rowIndex, colIndex, store) {
                                   // meta.tdAttr = 'data-qtip="Double-click to rename label"';
                                   var label = this.up( '#labelControlWindow' ).visibleLabels[id];
                                   var ret = ASPIREdb.view.LabelControlWindow.getHtmlLabel( label );
                                   return ret;
                                },

                             },
                             {
                                header : 'Description',
                                dataIndex : 'id',
                                flex : 2,
                                renderer : function(id, meta, rec, rowIndex, colIndex, store) {
                                   // meta.tdAttr = 'data-qtip="Double-click to rename label"';
                                   var label = this.up( '#labelControlWindow' ).visibleLabels[id];
                                   var ret = label.description;
                                   return ret;
                                },

                             },
                             {
                                header : 'Show',
                                dataIndex : 'isShown',
                                xtype : 'checkcolumn',
                                id : 'labelShowColumn',
                                width : 50,
                                sortable : false
                             },
                             {
                                header : 'Edit',
                                xtype : 'actioncolumn',
                                id : 'labelEditAction',
                                handler : function(view, rowIndex, colIndex, item, e) {
                                   var action = 'editLabel';
                                   this.fireEvent( 'itemclick', this, action, view, rowIndex, colIndex, item, e );
                                },

                                width : 30,
                                items : [ {
                                   icon : 'scripts/ASPIREdb/resources/images/icons/wrench.png',
                                   tooltip : 'Edit label',

                                } ]
                             },
                             {
                                header : '',
                                xtype : 'actioncolumn',
                                id : 'labelRemoveAction',

                                width : 30,
                                items : [ {
                                   id : 'deleteIcon',
                                   icon : 'scripts/ASPIREdb/resources/images/icons/eraser.png',
                                   tooltipType : 'title',
                                   // tooltip : 'Remove label from selected items',
                                   handler : function(view, rowIndex, colIndex, item, e) {
                                      var action = 'removeLabel';
                                      this.fireEvent( 'itemclick', this, action, view, rowIndex, colIndex, item, e );
                                   },
                                   getTip : function(v, metaData, r, rowIndex, colIndex, store) {
                                      var ret = 'Remove <b>' + r.get( 'name' ) + '</b> from the selected '
                                         + (this.up( '.window' ).isSubjectLabel ? 'subjects' : 'variants') + '?';

                                      metaData.tdAttr = 'data-qtip="' + ret + '" qwidth="auto"';

                                      return ret;
                                   },
                                } ]
                             }, {
                                header : '',
                                xtype : 'actioncolumn',
                                id : 'labelDeleteAction',

                                width : 30,
                                items : [ {
                                   icon : 'scripts/ASPIREdb/resources/images/icons/trash.png',
                                   tooltipType : 'title',
                                   getTip : function(v, metaData, r, rowIndex, colIndex, store) {
                                      var ret = 'Delete <b>' + r.get( 'name' ) + '</b> from system?';

                                      metaData.tdAttr = 'data-qtip="' + ret + '" qwidth="auto"';

                                      return ret;
                                   },
                                   handler : function(view, rowIndex, colIndex, item, e) {
                                      var action = 'deleteLabel';
                                      this.fireEvent( 'itemclick', this, action, view, rowIndex, colIndex, item, e );
                                   },
                                } ]
                             }, ],
                  // plugins : [ rowEditing ],

                  listeners : {
                     itemdblclick : function(e, record, item, index) {
                        // create edit label window

                        var ref = this;
                        var labelControlWindow = ref.up( '#labelControlWindow' );
                        var row = labelControlWindow.visibleLabels[record.data.id];
                        var selectedLabel = row;
                        var grid = labelControlWindow.down( '#labelSettingsGrid' );

                        labelControlWindow.editLabel( selectedLabel, labelControlWindow.isSubjectLabel );

                     }
                  }
               },// end of grid
      ]
   } ],

   initComponent : function() {
      this.callParent();

      var me = this;

      me.down( '#labelShowColumn' ).on( 'checkchange', me.onLabelShowCheckChange, this );

      me.down( '#labelDeleteAction' ).on( 'itemclick', me.onLabelDeleteActionClick, this );

      me.down( '#labelRemoveAction' ).on( 'itemclick', me.onLabelRemoveActionClick, this );

      me.down( '#labelEditAction' ).on( 'itemclick', me.onLabelEditActionClick, this );

      me.initGridAndShow();

      ASPIREdb.EVENT_BUS.on( 'label_color_changed', me.labelColorHandler, this );
   },

   initGridAndShow : function() {
      var me = this;

      if ( me.isSubjectLabel ) {
         me.service = SubjectService;
      } else {
         me.service = VariantService;
      }

      var projectId = ASPIREdb.ActiveProjectSettings.getActiveProjectIds()[0];

      // make sure to only show those labels which we have write permissions
      if ( me.isSubjectLabel ) {
         LabelService.getSubjectLabelsByProjectId( projectId, {
            callback : function(labels) {
               me.loadLabels( labels )
            },
            errorHandler : function(message, exception) {
               console.log( message )
               console.log( dwr.util.toDescriptiveString( exception.stackTrace, 3 ) )
            },
         } )
      } else {
         LabelService.getVariantLabelsByProjectId( projectId, {
            callback : function(labels) {
               me.loadLabels( labels )
            },
            errorHandler : function(message, exception) {
               console.log( message )
               console.log( dwr.util.toDescriptiveString( exception.stackTrace, 3 ) )
            },
         } )
      }

   },

   loadLabels : function(labels) {
      var me = this;
      var loadData = [];
      for (var i = 0; i < labels.length; i++) {
         var label = labels[i]
         loadData.push( [ label.id, label.name, label.colour, label.isShown, label.description ] );
      }

      me.down( '#labelSettingsGrid' ).store.loadData( loadData );
   },

   labelColorHandler : function(selColor) {
      this.down( '#labelSettingsGrid' ).getView().refresh();
   },

   xyPositionFoundHandler : function(e) {
      console.log( 'get X and Y :' + e );
      this.X = e.getPageX();
      this.Y = e.getPageY();
      ASPIREdb.EVENT_BUS.fireEvent( 'position_found', this.X, this.Y );
   },

   removeSubjectLabels : function(labels, rowIndex) {
      var me = this;
      if ( me.selectedOwnerIds.length > 0 ) {
         Ext.MessageBox.confirm( 'Remove Subject Label', 'Remove  <b>' + labels[0].name + '</b> from '
            + me.selectedOwnerIds.length + ' selected subject(s)?', function(btn) {
            if ( btn === 'yes' ) {
               LabelService.removeLabelsFromSubjects( labels, me.selectedOwnerIds, {
                  callback : function() {
                     ASPIREdb.EVENT_BUS.fireEvent( 'subject_label_removed', me.selectedOwnerIds, labels );
                     // me.down( '#labelSettingsGrid' ).store.removeAt( rowIndex );
                  }
               } );
            }
         } );
      } else {
         Ext.Msg.show( {
            title : 'Remove Subject Label',
            msg : 'Please select subject(s) to remove the label from',
            buttons : Ext.Msg.OK,
            minWidth : 350,
         } );
      }
   },

   /**
    * Returns true if label was removed
    */
   removeVariantLabels : function(labels, rowIndex) {
      if ( labels === null || rowIndex === null ) {

         return;
      }
      var me = this;
      if ( me.selectedOwnerIds.length > 0 ) {
         Ext.MessageBox.confirm( 'Remove Variant Label', 'Remove <b>' + labels[0].name + '</b> from '
            + me.selectedOwnerIds.length + ' selected variant(s)?', function(btn) {
            if ( btn === 'yes' ) {
               LabelService.removeLabelsFromVariants( labels, me.selectedOwnerIds, {
                  callback : function() {
                     ASPIREdb.EVENT_BUS.fireEvent( 'variant_label_removed', me.selectedOwnerIds, labels );
                     // me.down( '#labelSettingsGrid' ).store.removeAt( rowIndex );
                  }
               } );
            }
         } );
      } else {
         Ext.Msg.show( {
            title : 'Remove Variant Label',
            msg : 'Please select variant(s) to remove the label from',
            buttons : Ext.Msg.OK,
            minWidth : 350,
         } );
      }
   },

   /**
    * Delete label
    */
   deleteLabel : function(label, rowIndex) {
      if ( label === null || rowIndex === null ) {
         return;
      }
      var me = this;
      var labels = [ label ];
      if ( me.isSubjectLabel ) {
         Ext.MessageBox.confirm( 'Delete Subject Label', 'Delete <b>' + label.name + '</b> from system?',
            function(btn) {
               if ( btn === 'yes' ) {
                  LabelService.deleteSubjectLabels( labels, {
                     callback : function() {
                        ASPIREdb.EVENT_BUS.fireEvent( 'subject_label_removed', [], labels );
                        me.down( '#labelSettingsGrid' ).store.removeAt( rowIndex );
                     }
                  } );
               }
            } );
      } else {
         Ext.MessageBox.confirm( 'Delete Variant Label', 'Delete <b>' + label.name + '</b> from system?',
            function(btn) {
               if ( btn === 'yes' ) {
                  LabelService.deleteVariantLabels( labels, {
                     callback : function() {
                        ASPIREdb.EVENT_BUS.fireEvent( 'variant_label_removed', [], labels );
                        me.down( '#labelSettingsGrid' ).store.removeAt( rowIndex );
                     }
                  } );
               }
            } );
      }
   },

   onLabelEditActionClick : function(column, action, view, rowIndex, colIndex, item, e) {
      var me = this;
      var rec = view.store.getAt( rowIndex );
      var label = rec.data;
      me.editLabel( me.visibleLabels[label.id], me.isSubjectLabel );
   },

   onLabelRemoveActionClick : function(column, action, view, rowIndex, colIndex, item, e) {
      var me = this;
      var rec = view.store.getAt( rowIndex );
      var label = rec.data;
      me.removeLabels( [ me.visibleLabels[label.id] ], rowIndex );
   },

   onLabelDeleteActionClick : function(column, action, view, rowIndex, colIndex, item, e) {
      var me = this;
      var rec = view.store.getAt( rowIndex );
      var label = rec.data;
      me.deleteLabel( label, rowIndex );
   },

   editLabel : function(selectedLabel, isSubjectLabel) {
      var me = this;
      var grid = me.down( '#labelSettingsGrid' );

      Ext.define( 'ASPIREdb.view.CreateLabelWindowEdit', {
         isSubjectLabel : isSubjectLabel,
         title : 'Edit Label Manager',
         extend : 'ASPIREdb.view.CreateLabelWindow',
         selectedLabel : selectedLabel,
         isCreate : false,

         // override
         onOkButtonClick : function() {

            var vo = this.getLabel( selectedLabel );
            if ( vo == null ) {
               return;
            }

            LabelService.updateLabel( vo, {
               callback : function() {
                  grid.getView().refresh();
               },
               errorHandler : function(er, exception) {
                  Ext.Msg.alert( "Update Label Error", er + "\n" + exception.stack );
                  console.log( exception.stack );
               }
            } );

            if ( this.isSubjectLabel ) {
               ASPIREdb.EVENT_BUS.fireEvent( 'subject_label_changed' );
            } else {
               ASPIREdb.EVENT_BUS.fireEvent( 'variant_label_changed' );
            }
            this.hide();
         },

      } );

      var labelEditWindow = new ASPIREdb.view.CreateLabelWindowEdit();
      labelEditWindow.show();
   },

   removeLabels : function(labels, rowIndex) {
      var me = this;

      if ( labels != null && labels.length > 0 ) {
         if ( me.isSubjectLabel ) {
            me.removeSubjectLabels( labels, rowIndex );
         } else {
            me.removeVariantLabels( labels, rowIndex );
         }
      } else {
         me.destroy();
      }
   },

   /**
    * Show label?
    * 
    * @param checkColumn
    * @param rowIndex
    * @param checked
    * @param eOpts
    */
   onLabelShowCheckChange : function(checkColumn, rowIndex, checked, eOpts) {
      var me = this;
      var id = this.down( '#labelSettingsGrid' ).store.data.items[rowIndex].data.id;
      var label = this.visibleLabels[id];
      label.isShown = checked;
      LabelService.updateLabel( label, {
         callback : function() {
            // commented out for performance, do this once the window is closed
            // if ( me.isSubjectLabel ) {
            // ASPIREdb.EVENT_BUS.fireEvent( 'subject_label_changed' );
            // } else {
            // ASPIREdb.EVENT_BUS.fireEvent( 'variant_label_changed' );
            // }
         }
      } );
   }

} );
