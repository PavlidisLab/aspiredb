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
      labelName : '',
      labelColour : '',
      selectedIds : [],
      service : null,
   },

   constructor : function(cfg) {
      this.initConfig( cfg );
      this.callParent( arguments );
   },

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
         value : me.labelName,
         fieldLabel : 'Name',
         margin : 5,
      } );

      // TODO
      var descriptionField = Ext.create( 'Ext.form.TextArea', {
         itemId : 'descriptionField',
         renderTo : Ext.getBody(),
         value : me.labelDescription,
         fieldLabel : 'Description',
         margin : 5,
      } );

      var defaultColour = "000000";

      labelCombo.on( 'select', function(combo, records, eOpts) {
         // Bug 3917 fixed
         var vo = records[0].data.value;
         if ( vo != null && vo.colour != null ) {
            if ( me.down( '#colorPicker' ).colors.indexOf( vo.colour ) == -1 ) {
               vo.colour = defaultColour;
               console.log( 'Warning: Label colour ' + vo.colour + ' is not available, setting to ' + defaultColour );
            }
            me.down( '#colorPicker' ).select( vo.colour );
            
            me.down('#descriptionField').setValue( vo.description );
         }
      } );

      me.add( [
               {
                  xtype : 'container',
                  layout : {
                     type : 'vbox'
                  },
                  items : [ labelCombo, descriptionField ]
               },
               {
                  xtype : 'colorpicker',
                  itemId : 'colorPicker',
                  value : defaultColour, // default
                  flex : 1,
                  width : 90,
                  height : 40,
                  colors : [ "000000", "993300", "AC051B", "003300", "636E01", "000080", "333399", "036F0A", "800000",
                            "7C0474" ]
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
               }, ] );

      // if color is found in the color pickers, then assign it, otherwise use the default
      if ( me.down( '#colorPicker' ).colors.indexOf( me.labelColour ) != -1 ) {
         me.down( '#colorPicker' ).select( me.labelColour );
      } else {
         console.log( 'Warning: Label colour ' + me.labelColour + ' is not available, setting to ' + defaultColour );
         me.down( '#colorPicker' ).select( defaultColour );
      }

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
      var labelIndex = labelCombo.getStore().findExact( 'display', vo.name );
      if ( labelIndex != -1 ) {
         // activate confirmation window
         Ext.MessageBox.confirm( 'Label already exist', 'Label already exist. Add into it ?', function(btn) {
            if ( btn === 'yes' ) {
               me.addLabelHandler( vo, me.selectedIds );
               this.hide();
            }

         }, this );

      } else {
         me.addLabelHandler( vo, me.selectedIds );
         this.hide();
      }
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
            LabelService.updateLabel( addedLabel );
            /*
             * var existingLab = me.visibleLabels[addedLabel.id]; if ( existingLab == undefined ) {
             * me.visibleLabels[addedLabel.id] = addedLabel; } else { existingLab.isShown = true; }
             * 
             * for (var i = 0; i < me.selSubjects.length; i++) { me.selSubjects[i].labels.push( addedLabel ); }
             */

            // refresh the grid
            if ( me.isSubjectLabel ) {
               ASPIREdb.EVENT_BUS.fireEvent( 'subject_label_updated', selectedIds, addedLabel );
            } else {
               ASPIREdb.EVENT_BUS.fireEvent( 'variant_label_updated', selectedIds, addedLabel );
            }

         }
      } );

   },

   getLabel : function() {
      var colorPicker = this.down( "#colorPicker" );
      var labelCombo = this.down( "#labelCombo" );
      var description = this.down( '#descriptionField' );

      // vo will be a ValueObject if it already exists
      // otherwise, it's just a name of type string
      var vo = labelCombo.getValue();
      if ( vo == null || vo.length == "" ) {
         return null;
      }
      if ( vo.id == undefined ) {
         vo = new LabelValueObject();
      }
      vo.name = labelCombo.getValue();
      vo.colour = colorPicker.getValue();
      vo.htmlLabel = ASPIREdb.view.LabelControlWindow.getHtmlLabel( vo );
      vo.isShown = true;
      vo.description = description.getValue();
      
      return vo;
   },

} );