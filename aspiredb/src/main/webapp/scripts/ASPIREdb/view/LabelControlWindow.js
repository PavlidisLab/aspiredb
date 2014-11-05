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
clicksToEdit : 2, autoCancel :
 false } );


var rowEditing = Ext.create( 'Ext.grid.plugin.RowEditing', {
   // clicksToMoveEditor: 1,
   clicksToEdit : 2,
   autoCancel : false
} );

var contextMenu = new Ext.menu.Menu( {
   itemId : 'contextMenu',
   displayField : 'labelColour',
   items : [ {
      text : colorPicker,
      scope : this,

   } ]
} );

var colorPicker = Ext.create( 'Ext.menu.ColorPicker', {
   displayField : 'labelColour',
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
 * For removing and showing labels
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
   config : {
      visibleLabels : {},
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
                  flex : 1,
                  region : 'center',
                  itemId : 'labelSettingsGrid',
                  store : Ext.create( 'ASPIREdb.store.LabelStore' ),
                  columns : [ {
                     header : 'Label',
                     dataIndex : 'labelId',
                     width : 100,
                     flex : 1,
                     renderer : function(labelId, meta, rec, rowIndex, colIndex, store) {
                        meta.tdAttr = 'data-qtip="Double-click to rename label"';
                        var label = this.up( '#labelControlWindow' ).visibleLabels[labelId];
                        var ret = label.htmlLabel;
                        return ret;
                     },

                  }, {
                     header : 'Name',
                     dataIndex : 'labelName',
                     width : 100,
                     flex : 1,
                     renderer : function(val, meta, rec, rowIndex, colIndex, store) {
                        meta.tdAttr = 'data-qtip="Double-click to rename label"';
                        return val;
                     },

                  }, {
                     header : 'Show',
                     dataIndex : 'show',
                     xtype : 'checkcolumn',
                     id : 'labelShowColumn',
                     width : 50,
                     sortable : false
                  }, {
                     header : '',
                     xtype : 'actioncolumn',
                     id : 'labelActionColumn',
                     handler : function(view, rowIndex, colIndex, item, e) {
                        var action = 'removeLabel';
                        this.fireEvent( 'itemclick', this, action, view, rowIndex, colIndex, item, e );
                     },

                     width : 30,
                     items : [ {
                        icon : 'scripts/ASPIREdb/resources/images/icons/delete.png',
                        tooltip : 'Remove label',

                     } ]
                  } ],
                  // plugins : [ rowEditing ],

                  listeners : {
                     itemdblclick : function(e, record, item, index) {
                        // create edit label window

                        var ref = this;
                        var row = e.store.data.items[index].data;
                        var labelcolour = row.labelColour;
                        var labelName = row.labelName;
                        var labelid = row.labelId;

                        Ext.define( 'ASPIREdb.view.CreateLabelWindowEdit', {
                           isSubjectLabel : true,
                           extend : 'Ext.window.Window',
                           title : 'Edit label',
                           closable : true,
                           closeAction : 'destroy',
                           layout : 'border',
                           bodyStyle : 'padding: 5px;',
                           flex : 1,

                           layout : {
                              type : 'hbox',
                              defaultMargins : {
                                 top : 5,
                                 right : 5,
                                 left : 5,
                                 bottom : 5,
                              },
                           },

                           initComponent : function() {
                              var me = this;

                              me.items = [ {
                                 xtype : 'textfield',
                                 id : 'labelName',
                                 value : labelName
                              }, {
                                 xtype : 'colorpicker',
                                 itemId : 'colorPicker',
                                 value : labelcolour,
                                 flex : 2,
                              }, {
                                 xtype : 'button',
                                 itemId : 'okButton',
                                 text : 'OK',
                                 flex : 1,
                                 handler : function() {
                                    me.onOkButtonClick();
                                 }
                              }, {
                                 xtype : 'button',
                                 itemId : 'cancelButton',
                                 text : 'Cancel',
                                 flex : 1,
                                 handler : function() {
                                    me.hide();
                                 }
                              }, ], this.callParent();

                           },

                           // override
                           onOkButtonClick : function() {

                              var vo = this.getLabel();
                              if ( vo == null ) {
                                 return;
                              }
                              var label = ref.up( '#labelControlWindow' ).visibleLabels[labelid];
                              label.name = vo.name;
                              label.colour = vo.colour;
                              label.htmlLabel = "<font color=black><span style='background-color:" + vo.colour
                                 + "'>&nbsp&nbsp" + vo.name + "&nbsp</span></font>&nbsp&nbsp&nbsp";
                              // TODO: subject label update
                              LabelService.updateLabel( label, {
                                 callback : function() {
                                    ref.getView().refresh();
//                                  commented out for performance, do this once the window is closed
//                                    if ( ref.up( '#labelControlWindow' ).isSubjectLabel ) {
//                                       ASPIREdb.EVENT_BUS.fireEvent( 'subject_label_changed' );
//                                    } else {
//                                       ASPIREdb.EVENT_BUS.fireEvent( 'variant_label_changed' );
//                                    }
                                 },
                                 errorHandler : function(er, exception) {
                                    Ext.Msg.alert( "Update Label Error", er + "\n" + exception.stack );
                                    console.log( exception.stack );
                                 }
                              } );

                              this.hide();

                           },

                           getLabel : function() {
                              var editedColorPicker = this.down( "#colorPicker" );
                              var editedLabelName = this.down( "#labelName" );

                              // vo will be a ValueObject if it already exists
                              // otherwise, it's just a name of type string
                              var vo = editedLabelName.getValue();
                              if ( vo == null || vo.length == "" ) {
                                 return null;
                              }
                              if ( vo.id == undefined ) {
                                 vo = new LabelValueObject();
                                 vo.name = editedLabelName.getValue();
                                 vo.colour = editedColorPicker.getValue();
                              }
                              vo.isShown = true;
                              return vo;

                           },

                        } );

                        var labelEditWindow = new ASPIREdb.view.CreateLabelWindowEdit();
                        labelEditWindow.show();

                     }
                  }
               },// end of grid
      ]
   } ],

   initComponent : function() {
      this.callParent();

      var me = this;

      me.down( '#labelShowColumn' ).on( 'checkchange', me.onLabelShowCheckChange, this );

      me.down( '#labelActionColumn' ).on( 'itemclick', me.onLabelActionColumnClick, this );

      me.initGridAndShow();

      ASPIREdb.EVENT_BUS.on( 'label_color_chnaged', me.labelColorHandler, this );
   },

   initGridAndShow : function() {
      var me = this;

      /**
       * Created for row editing me.down( '#labelSettingsGrid' ).on( 'edit', function(editor, e) { var record =
       * e.record; var label = me.visibleLabels[record.data.labelId]; label.name = record.data.labelName;
       * label.htmlLabel ="<font color=black><span
       * style='background-color:"+label.colour+"'>&nbsp&nbsp"+label.name+"&nbsp</span></font>&nbsp&nbsp&nbsp";
       * 
       * LabelService.updateLabel( label, { callback : function() { me.down( '#labelSettingsGrid' ).getView().refresh();
       * if ( me.isSubjectLabel ) { ASPIREdb.EVENT_BUS.fireEvent( 'subject_label_changed' ); } else {
       * ASPIREdb.EVENT_BUS.fireEvent( 'variant_label_changed' ); } },errorHandler : function(er, exception) {
       * Ext.Msg.alert( "Update Label Error", er + "\n" + exception.stack ); console.log( exception.stack ); } } ); } );
       * 
       * me.down( '#labelSettingsGrid' ).on( 'validateedit', function(editor, e) { var record = e.record; var
       * labelcolour = record.data.labelColour; // TODO: check for valid color or find a better way to edit colours
       * using color picker // if (labelcolour) } );
       */
      if ( me.isSubjectLabel ) {
         me.service = SubjectService;
      } else {
         me.service = VariantService;
      }

      var loadData = [];
      for (labelId in me.visibleLabels) {
         var label = me.visibleLabels[labelId];
         loadData.push( [ label.id, label.name, label.colour, label.isShown ] );
      }
      me.down( '#labelSettingsGrid' ).store.loadData( loadData );

   },

   labelColorHandler : function(selColor) {

   },

   xyPositionFoundHandler : function(e) {
      console.log( 'get X and Y :' + e );
      this.X = e.getPageX();
      this.Y = e.getPageY();
      ASPIREdb.EVENT_BUS.fireEvent( 'position_found', this.X, this.Y );
   },

   removeSubjectLabels : function(labels, rowIndex) {
      var me = this;
      if ( me.selectedOwnerIds.length == 0 ) {
         Ext.MessageBox.confirm( 'Delete', 'Remove label ' + 'for all subjects?', function(btn) {
            if ( btn === 'yes' ) {
               LabelService.deleteSubjectLabels( labels, {
                  callback : function() {
                     ASPIREdb.EVENT_BUS.fireEvent( 'subject_label_removed', me.selectedOwnerIds, labels );
                     me.down( '#labelSettingsGrid' ).store.removeAt( rowIndex );
                  }
               } );
            }
         } );
      } else {
         Ext.MessageBox.confirm( 'Delete', 'Remove ' + labels.length + ' label(s) ' + 'for selected subject(s)?',
            function(btn) {
               if ( btn === 'yes' ) {
                  LabelService.removeLabelsFromSubjects( labels, me.selectedOwnerIds, {
                     callback : function() {
                        ASPIREdb.EVENT_BUS.fireEvent( 'subject_label_removed', me.selectedOwnerIds, labels );
                     }
                  } );
               }
            } );
      }
   },

   /**
    * Returns true if label was removed
    */
   removeVariantLabels : function(labels, rowIndex) {
      var me = this;
      if ( me.selectedOwnerIds.length == 0 ) {
         Ext.MessageBox.confirm( 'Delete', 'Remove label ' + 'for all variants?', function(btn) {
            if ( btn === 'yes' ) {
               LabelService.deleteVariantLabels( labels, {
                  callback : function() {
                     ASPIREdb.EVENT_BUS.fireEvent( 'variant_label_removed', me.selectedOwnerIds, labels );
                     me.down( '#labelSettingsGrid' ).store.removeAt( rowIndex );
                  }
               } );
            }
         } );
      } else {
         Ext.MessageBox.confirm( 'Delete', 'Remove ' + labels.length + ' label(s) ' + 'for selected variant(s)?',
            function(btn) {
               if ( btn === 'yes' ) {
                  LabelService.removeLabelsFromVariants( labels, me.selectedOwnerIds, {
                     callback : function() {
                        ASPIREdb.EVENT_BUS.fireEvent( 'variant_label_removed', me.selectedOwnerIds, labels );
                     }
                  } );
               }
            } );
      }
   },

   onLabelActionColumnClick : function(column, action, view, rowIndex, colIndex, item, e) {

      var me = this;
      var rec = view.store.getAt( rowIndex );
      var label = rec.data;
      me.removeLabels( [ me.visibleLabels[label.labelId] ], rowIndex );
      /*
       * ASPIREdb.EVENT_BUS.on('subject_label_changed', function(subjectIds) {
       * me.down('#labelSettingsGrid').store.removeAt(rowIndex); }, this);
       * 
       * ASPIREdb.EVENT_BUS.on('variant_label_changed', function(subjectIds) {
       * me.down('#labelSettingsGrid').store.removeAt(rowIndex); }, this);
       */
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
      var labelId = this.down( '#labelSettingsGrid' ).store.data.items[rowIndex].data.labelId;
      var label = this.visibleLabels[labelId];
      label.isShown = checked;
      LabelService.updateLabel( label, {
         callback : function() {
//            commented out for performance, do this once the window is closed
//            if ( me.isSubjectLabel ) {
//               ASPIREdb.EVENT_BUS.fireEvent( 'subject_label_changed' );
//            } else {
//               ASPIREdb.EVENT_BUS.fireEvent( 'variant_label_changed' );
//            }
         }
      } );
   }

} );
