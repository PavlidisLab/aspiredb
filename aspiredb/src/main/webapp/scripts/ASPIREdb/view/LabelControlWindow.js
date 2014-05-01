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
/**
 * Ext.define('Ext.ux.ColorPickerCombo', { extend: 'Ext.form.field.Trigger', alias: 'widget.colorcbo', triggerTip:
 * 'Please select a color.', onTriggerClick: function() { var me = this; picker = Ext.create('Ext.picker.Color', {
 * pickerField: this, ownerCt: this, renderTo: document.body, floating: true, hidden: true, focusOnShow: true, style: {
 * backgroundColor: "#fff" } , listeners: { scope:this, select: function(field, value, opts){ me.setValue('#' + value);
 * me.inputEl.setStyle({backgroundColor:value}); picker.hide(); }, show: function(field,opts){
 * field.getEl().monitorMouseLeave(500, field.hide, field); } } }); picker.alignTo(me.inputEl, 'tl-bl?');
 * picker.show(me.inputEl); } });
 */

/**
 * var colorPicker= Ext.create('Ext.picker.Color', { itemId : 'colorPicker', id :'colorPicker', displayField:
 * 'labelColour', queryMode: 'local', resizable: false, draggable: true, closeAction: 'hide', width: 150, height: 135,
 * border: true, hidden: false, //layout:'fit', fixed :true, frame:true, formBind:true, listeners: { select:
 * function(picker, selColor) { alert(selColor); //picker.setValue(selColor); this.displayField.value=selColor; } } });
 */

/**
 * For removing and showing labels
 */
Ext.define( 'ASPIREdb.view.LabelControlWindow', {
   extend : 'Ext.Window',
   alias : 'widget.labelControlWindow',
   title : 'Label settings',
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
      visibleLabels : [],
      isSubjectLabel : false,
      selectedIds : [],
      XValue : 0,
      YValue : 0,
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
                  // width : 550,
                  // height : 100,
                  itemId : 'labelSettingsGrid',
                  store : Ext.create( 'ASPIREdb.store.LabelStore' ),
                  columns : [
                             {
                                header : 'Label',
                                dataIndex : 'labelId',
                                // width : 100,
                                flex : 1,
                                renderer : function(labelId) {

                                   var label = this.up( '#labelControlWindow' ).visibleLabels[labelId];
                                   var ret = "";
                                   var fontcolor = (parseInt( label.colour, 16 ) > 0xffffff / 2) ? 'black' : 'white';
                                   ret += "<font color=" + fontcolor + "><span style='background-color: "
                                      + label.colour + "'>&nbsp&nbsp" + label.name
                                      + "&nbsp</span></font>&nbsp&nbsp&nbsp";

                                   return ret;
                                },
                                editor : {
                                   xtype : 'numberfield',
                                   allowBlank : false,

                                }
                             }, {
                                header : 'Name',
                                dataIndex : 'labelName',
                                width : 100,
                                editor : {
                                   xtype : 'textfield',
                                   allowBlank : false,

                                }
                             }, {
                                header : 'Color',
                                dataIndex : 'labelColour',
                                width : 100,
                                editor : {
                                   xtype : 'textfield',
                                   allowBlank : false,

                                },
                             /**
                               * editor ://colorPicker, { xtype:colorPicker, allowBlank: false, displayField
                               * :'labelColour', }, width : 100, listeners :{ click : function(e){ console.log('color
                               * picker X '+e.getX()+'and Y '+e.getY()); this.XValue = e.getX(); this.YValue =e.getY(); } }
                               */
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
                  plugins : [ rowEditing ],
                  /**
                   * [Ext.create('Ext.grid.plugin.CellEditing', { clicksToEdit: 2, ptype:'cellEditing', autoCancel:
                   * false,
                   * 
                   * })],
                   */
                  listeners : {
                     cellclick : function(e) {
                        console.log( 'hi. I am in grid listener' + e.getX() );
                        // console.log('testing ***************'+e.getEditor());
                        // colorPicker.setPosition(e.getX(), e.getY());
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
   },

   initGridAndShow : function() {
      var me = this;

      me.down( '#labelSettingsGrid' ).on( 'edit', function(editor, e) {
         var record = e.record;
         var label = me.visibleLabels[record.data.labelId];
         label.name = record.data.labelName;
         label.colour = record.data.labelColour;
         LabelService.updateLabel( label, {
            callback : function() {
               me.down( '#labelSettingsGrid' ).getView().refresh();
               if ( me.isSubjectLabel ) {
                  ASPIREdb.EVENT_BUS.fireEvent( 'subject_label_changed' );
               } else {
                  ASPIREdb.EVENT_BUS.fireEvent( 'variant_label_changed' );
               }
            }
         } );
      } );

      me.down( '#labelSettingsGrid' ).on( 'validateedit', function(editor, e) {
         var record = e.record;
         var labelcolour = record.data.labelColour;
         // TODO: check for valid color or find a better way to edit colours using color picker
         // if (labelcolour)
      } );

      // init visibleLabels
      me.visibleLabels = [];
      var loadData = [];

      if ( me.isSubjectLabel ) {
         me.service = SubjectService;
      } else {
         me.service = VariantService;
      }

      me.service.suggestLabels( null, {
         callback : function(vos) {
            for (var i = 0; i < vos.length; i++) {
               var label = vos[i];
               me.visibleLabels[label.id] = label;
               loadData.push( [ label.id, label.name, label.colour, label.isShown ] );
            }
            me.down( '#labelSettingsGrid' ).store.loadData( loadData );
         }
      } );
   },

   xyPositionFoundHandler : function(e) {
      console.log( 'get X and Y :' + e );
      this.X = e.getPageX();
      this.Y = e.getPageY();
      ASPIREdb.EVENT_BUS.fireEvent( 'position_found', this.X, this.Y );
   },

   removeSubjectLabels : function(labels, rowIndex) {
      var me = this;
      if ( me.selectedIds.length == 0 ) {
         Ext.MessageBox.confirm( 'Delete', 'Remove label ' + 'for all subjects?', function(btn) {
            if ( btn === 'yes' ) {
               LabelService.deleteSubjectLabels( labels, {
                  callback : function() {
                     ASPIREdb.EVENT_BUS.fireEvent( 'subject_label_changed', me.selectedIds );
                     me.down( '#labelSettingsGrid' ).store.removeAt( rowIndex );
                  }
               } );
            }
         } );
      } else {
         Ext.MessageBox.confirm( 'Delete', 'Remove ' + labels.length + ' label(s) ' + 'for ' + me.selectedIds.length
            + ' subject(s)?', function(btn) {
            if ( btn === 'yes' ) {
               LabelService.removeLabelsFromSubjects( labels, me.selectedIds, {
                  callback : function() {
                     ASPIREdb.EVENT_BUS.fireEvent( 'subject_label_changed', me.selectedIds );
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
      if ( me.selectedIds.length == 0 ) {
         Ext.MessageBox.confirm( 'Delete', 'Remove label ' + 'for all variants?', function(btn) {
            if ( btn === 'yes' ) {
               LabelService.deleteVariantLabels( labels, {
                  callback : function() {
                     ASPIREdb.EVENT_BUS.fireEvent( 'variant_label_changed', me.selectedIds );
                     me.down( '#labelSettingsGrid' ).store.removeAt( rowIndex );
                  }
               } );
            }
         } );
      } else {
         Ext.MessageBox.confirm( 'Delete', 'Remove ' + labels.length + ' label(s) ' + 'for ' + me.selectedIds.length
            + ' variant(s)?', function(btn) {
            if ( btn === 'yes' ) {
               LabelService.removeLabelsFromVariants( labels, me.selectedIds, {
                  callback : function() {
                     ASPIREdb.EVENT_BUS.fireEvent( 'variant_label_changed', me.selectedIds );
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
            if ( me.isSubjectLabel ) {
               ASPIREdb.EVENT_BUS.fireEvent( 'subject_label_changed' );
            } else {
               ASPIREdb.EVENT_BUS.fireEvent( 'variant_label_changed' );
            }
         }
      } );
   }

} );