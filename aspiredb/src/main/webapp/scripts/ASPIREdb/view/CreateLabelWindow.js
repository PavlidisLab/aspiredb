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
Ext.require( [ 'Ext.Window', 'Ext.picker.Color', 'Ext.data.ArrayStore', 'Ext.form.ComboBox', 'Ext.button.Button' ] );

Ext.define( 'ASPIREdb.view.CreateLabelWindow', {
   /**
    * @memberOf ASPIREdb.view.CreateLabelWindow
    */
   extend : 'Ext.Window',
   alias : 'widget.createLabelWindow',
   title : 'Make label',
   closable : true,
   modal : true,
   closeAction : 'destroy',
   bodyStyle : 'padding: 5px;',
   layout : 'fit',
   width : 610,
   height : 250,
   frame : true,
   layout : {
      type : 'hbox',
      defaultMargins : {
         top : 5,
         right : 5,
         left : 5,
         bottom : 5,
      },
   },

   config : {
      isSubjectLabel : false,
      selectedIds : [],
      service : null,
      selectedLabel : null,
   },

   constructor : function(cfg) {
      this.initConfig( cfg );
      this.callParent( arguments );
   },

   buttons : [ {
      xtype : 'button',
      itemId : 'previewButton',
      text : 'Preview',
      margin : 5,
      handler : function(ref) {
         ref.up( '.window' ).onPreviewButtonClick();
      }
   }, {
      xtype : 'button',
      itemId : 'okButton',
      text : 'OK',
      margin : 5,
      handler : function(ref) {
         ref.up( '.window' ).onOkButtonClick();
      }
   }, {
      xtype : 'button',
      itemId : 'cancelButton',
      text : 'Cancel',
      margin : 5,
      handler : function(ref) {
         ref.up( '.window' ).hide();
      }
   }, ],

   initComponent : function() {
      var me = this;

      if ( me.isSubjectLabel ) {
         me.service = SubjectService;
      } else {
         me.service = VariantService;
      }

      Ext.getCmp( 'aspireDbPanel' ).setLoading( true );
      me.service.suggestLabels( null, {
         callback : function(vos) {
            me.createItems( vos, me );
            Ext.getCmp( 'aspireDbPanel' ).setLoading( false );
         }
      } );

      this.callParent();

   },

   createItems : function(vos, me) {

      var data = [];
      for (var i = 0; i < vos.length; i++) {
         data.push( [ vos[i], vos[i].name ] );
      }

      var suggestLabelStore = Ext.create( 'Ext.data.ArrayStore', {
         fields : [ 'value', 'display' ],
         data : data,
         autoLoad : true,
         autoSync : true,

      } );

      var labelCombo = Ext.create( 'Ext.form.ComboBox', {
         itemId : 'labelCombo',
         store : suggestLabelStore,
         queryMode : 'local',
         displayField : 'display',
         valueField : 'value',
         renderTo : Ext.getBody(),
         fieldLabel : 'Name',
         emptyText : 'Choose existing or enter new label name',
         margin : 5,
         width : 400,
      } );

      var descriptionField = Ext.create( 'Ext.form.TextArea', {
         itemId : 'descriptionField',
         renderTo : Ext.getBody(),
         fieldLabel : 'Description',
         emptyText : 'Enter label description',
         margin : 5,
         width : 400,
         height : 100,
      } );

      var defaultColour = "000000";

      var colorPicker = {
         xtype : 'colorpicker',
         itemId : 'colorPicker',
         value : defaultColour, // default
         width : 145,
         height : 50,
         margins : 5,
         // brewer.pal(12,name="Paired")
         colors : [ "A6CEE3", "1F78B4", "B2DF8A", "33A02C", "FB9A99", "E31A1C", "FDBF6F", "FF7F00", "CAB2D6", "6A3D9A",
                   "FFFF99", "B15928",

                   "000000", "FFFFFF", "AC051B", "003300", "636E01", "000080", "333399", "036F0A", "800000", "7C0474" ]
      };

      var preview = {
         xtype : 'label',
         itemId : 'previewLabel',
         margins : 5,
      };

      me.add( [ {
         xtype : 'container',
         layout : {
            type : 'vbox'
         },
         items : [ labelCombo, descriptionField ]
      }, {
         xtype : 'container',
         layout : {
            type : 'vbox'
         },
         items : [ colorPicker, preview ]
      }, ] );

      labelCombo.on( 'change', me.labelComboSelect, this );

      // select "selectedLabel" in the combobox
      if ( me.selectedLabel != null && labelCombo != null ) {
         labelCombo.store.on( 'load', function(ds, records, o) {
            var rec = labelCombo.findRecordByDisplay( me.selectedLabel.name );
            labelCombo.setValue( rec );
         } );
      }

      labelCombo.store.load();
   },

   /**
    * This method is called when the user selects from the label combo box. Sets the component values based on
    * this.labelColour, this.labelDesc
    */
   labelComboSelect : function(combo, record) {
      var me = this;

      var defaultColour = "000000";

      if ( record == null ) {
         return;
      }

      var vo = record;
      me.selectedLabel = vo;

      // if color is found in the color pickers, then assign it, otherwise use the default
      if ( vo.colour != null && me.down( '#colorPicker' ).colors.indexOf( vo.colour ) != -1 ) {
         me.down( '#colorPicker' ).select( vo.colour );
      } else {
         console.log( 'Warning: Label colour ' + vo.colour + ' is not available, setting to ' + defaultColour );
         me.down( '#colorPicker' ).select( defaultColour );
      }

      if ( vo.description != null ) {
         me.down( '#descriptionField' ).setValue( vo.description );
      }

   },

   /**
    * Display the preview of the label
    */
   onPreviewButtonClick : function() {
      var me = this;
      var preview = me.down( "#previewLabel" );
      var labelCombo = me.down( "#labelCombo" );
      preview.setText( '', false );
      var vo = me.getLabel();
      if ( vo == null ) {
         return;
      }
      preview.setText( vo.htmlLabel, false );
   },

   /**
    * Create or update Label in the back-end
    */
   onOkButtonClick : function() {
      var me = this;
      var labelCombo = this.down( "#labelCombo" );
      var vo = this.getLabel();
      if ( vo == null ) {
         return;
      }
      me.addLabelHandler( vo, me.selectedIds );
      this.hide();
   },

   /**
    * Reusing the code in subject grid Add the label to the store
    * 
    * @param: label value object, selected subject Ids
    */
   addLabelHandler : function(vo, selectedIds) {

      var me = this;

      // store in database
      me.service.addLabel( selectedIds, vo, {
         callback : function(addedLabel) {

            addedLabel.isShown = true;

            // refresh the grid
            if ( me.isSubjectLabel ) {
               ASPIREdb.EVENT_BUS.fireEvent( 'subject_label_updated', selectedIds, addedLabel );
            } else {
               ASPIREdb.EVENT_BUS.fireEvent( 'variant_label_updated', selectedIds, addedLabel );
            }

         }
      } );

   },

   /**
    * Creates a new LabelValueObject if it doesn't exist, otherwise, update the values from UI components.
    * 
    * @param selectedLabel
    *           assign values to selectedLabel if not null
    */
   getLabel : function(selectedLabel) {

      var me = this;

      var colorPicker = me.down( "#colorPicker" );
      var labelCombo = me.down( "#labelCombo" );
      var description = me.down( '#descriptionField' );

      var vo = labelCombo.getValue();

      if ( vo === null ) {
         return;
      }

      if ( selectedLabel != null ) {
         vo = selectedLabel;
      }

      if ( vo.id == null ) {
         // user wants to create a new Label
         vo = new LabelValueObject();
         vo.name = labelCombo.getValue();
      } else {
         // user chooses an existing label
         vo.id = me.selectedLabel.id;
         vo.name = labelCombo.getDisplayValue();
      }

      vo.colour = colorPicker.getValue();
      vo.htmlLabel = ASPIREdb.view.LabelControlWindow.getHtmlLabel( vo );
      vo.isShown = true;
      vo.description = description.getValue();

      return vo;
   },

} );