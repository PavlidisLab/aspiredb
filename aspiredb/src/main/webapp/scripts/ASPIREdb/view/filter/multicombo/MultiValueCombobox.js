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
Ext.require( [ 'Ext.Component', 'Ext.form.field.Text', 'ASPIREdb.view.filter.multicombo.Item',
              'ASPIREdb.model.Property', 'ASPIREdb.model.PropertyValue', 'ASPIREdb.ValueSuggestionStore' ] );
/**
 * Define multi combo values
 */
Ext.define( 'ASPIREdb.view.filter.multicombo.MultiValueCombobox', {
   extend : 'Ext.Container',
   alias : 'widget.multivalue_combo',
   layout : {
      type : 'column',
      autoSize : true,
   },
   // minHeight : '50',
   autoEl : {
      tag : 'ul'
   },
   cls : 'multiValueSuggestBox-list',
   // width : 'auto',
   config : {
      suggestValuesRemoteFunction : null
   },

   setProperty : function(propertyObj) {
      var comboBox = this.getComponent( 'invisibleCombo' );

      var store = comboBox.getStore();
      store.setProperty( propertyObj );
   },

   /**
    * @returns {Array}
    */
   getValues : function() {
      var values = [];
      this.items.each( function(item) {
         if ( item instanceof ASPIREdb.view.filter.multicombo.Item ) {
            values.push( item.getValue() );
         }
      } );
      return values;
   },

   reset : function() {

      this.items = this.items.filterBy( function(item) {
         if ( item instanceof ASPIREdb.view.filter.multicombo.Item ) {
            item.destroy();
            return false;
         }
         return true;
      } );

      this.doLayout();

      var comboBox = this.getComponent( 'invisibleCombo' );
      comboBox.lastQuery = null;
   },

   /**
    * @private
    * @param item
    */
   addItem : function(item) {

      if ( !item )
         return;

      var itemElement;

      // if this is gene don't display full description
      if ( item.data && item.data.value && item.data.value.ensemblId ) {
         itemElement = Ext.create( 'ASPIREdb.view.filter.multicombo.Item', {
            text : item.data.value.label,
            value : item.raw.value
         } );

      } else {
         itemElement = Ext.create( 'ASPIREdb.view.filter.multicombo.Item', {
            text : item.data.displayValue,
            value : item.raw.value
         } );

      }

      itemElement.on( 'remove', function(itemToRemove) {
         this.items.remove( itemToRemove );
         itemToRemove.destroy();
      }, this );

      var comboBox = this.getComponent( 'invisibleCombo' );
      var items = this.items;
      items.insert( items.getCount() - 1, itemElement );
      comboBox.clearValue();
      this.doLayout();
      ASPIREdb.EVENT_BUS.fireEvent( 'query_update' );
   },

   addMultiComboItem : function(item) {
      var itemElement = item;
      itemElement.on( 'remove', function(itemToRemove) {
         this.items.remove( itemToRemove );
         itemToRemove.destroy();
      }, this );

      var comboBox = this.getComponent( 'invisibleCombo' );
      var items = this.items;
      items.insert( items.getCount() - 1, itemElement );
      comboBox.clearValue();
      this.doLayout();
      ASPIREdb.EVENT_BUS.fireEvent( 'query_update' );

   },

   /**
    * @private
    */
   removeItem : function() {
      if ( this.items.getCount() > 1 ) {
         // second before last
         var item = this.items.removeAt( this.items.getCount() - 2 );
         item.destroy();
         this.doLayout();
         ASPIREdb.EVENT_BUS.fireEvent( 'query_update' );
      }
   },

   initComponent : function() {
      this.items = [ {
         xtype : 'combo',
         itemId : 'invisibleCombo',
         // width : 150,
         minChars : 0,
         matchFieldWidth : false,
         hideTrigger : true,
         cls : 'multiValueSuggestBox-list-input',
         triggerAction : 'query',
         autoSelect : false,

         enableKeyEvents : false,
         displayField : 'displayValue',
         store : Ext.create( 'ASPIREdb.ValueSuggestionStore', {
            remoteFunction : this.getSuggestValuesRemoteFunction()
         } ),
         listConfig : {
            loadingText : 'Searching...',
            emptyText : 'No results found.',
            listeners : {
               itemclick : function(list, record) {
                  // this fires a 'select' event
                  comboBox.clearValue();
               }
            }
         },
         listeners : {
            change : function(field, newValue) {

               field.setValue( newValue );

            }
         }
      } ];

      this.callParent();

      var multiCombo = this;
      var comboBox = this.getComponent( 'invisibleCombo' );

      var addItemHack = function() {
         var currentValue = comboBox.getValue();
         var record = comboBox.findRecordByValue( currentValue );

         var records = [];
         records.push( record );

         comboBox.fireEvent( 'select', comboBox, records );
      };

      comboBox.on( 'keydown', function(obj, event) {
         if ( event.getKey() === event.BACKSPACE ) {
            if ( comboBox.getRawValue() === "" ) {
               multiCombo.removeItem();
               comboBox.collapse();
            }
         }

         if ( event.getKey() === event.ENTER || event.getKey() === event.TAB ) {

            addItemHack();

         }
      } );

      comboBox.on( 'select', function(obj, records) {
         multiCombo.addItem( records[0] );
      } );

      // ExtJs wasn't firing the select event when we selected the
      // currently highlighted item in the combolist, this is
      // a workaround
      comboBox.on( 'blur', function(obj, records) {
         addItemHack();
      } );

      this.on( 'afterrender', function() {
         this.getEl().on( 'click', function() {
            comboBox.focus();
         } );
      }, this );
   }
} );
