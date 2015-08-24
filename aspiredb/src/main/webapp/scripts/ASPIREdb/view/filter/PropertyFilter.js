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
Ext.require( [ 'Ext.layout.container.*', 'ASPIREdb.view.filter.multicombo.MultiValueCombobox',
              'ASPIREdb.model.Operator', 'ASPIREdb.view.filter.TextImportWindow', 'ASPIREdb.ActiveProjectSettings' ] );

/**
 * Includes a property combo, operator combo and value text field.
 */
Ext.define( 'ASPIREdb.view.filter.PropertyFilter', {
   extend : 'Ext.Container',
   alias : 'widget.filter_property',
   width : 875,
   layout : {
      type : 'hbox',

      defaultMargins : {
         right : 5,
      }
   },
   config : {
      propertyStore : null, /* property suggestions */
      suggestValuesRemoteFunction : null,
   },
   constructor : function(cfg) {
      this.initConfig( cfg );
      this.callParent( arguments );
   },

   isMultiValue : true,
   selectedProperty : null,

   getRestrictionExpression : function() {

      var operatorComboBox = this.getComponent( "operatorComboBox" );
      var multicombo_container = this.getComponent( "multicombo_container" );
      var multicombo = multicombo_container.getComponent( "multicombo" );
      var singleValueField = multicombo_container.getComponent( "singleValueField" );

      if ( this.isMultiValue ) {
         var setRestriction = new SetRestriction();
         setRestriction.property = this.selectedProperty;
         setRestriction.operator = operatorComboBox.getValue();
         setRestriction.values = multicombo.getValues();
         return setRestriction;
      } else {
         var simpleRestriction = new SimpleRestriction();
         simpleRestriction.property = this.selectedProperty;
         simpleRestriction.operator = operatorComboBox.getValue();
         var value = new NumericValue();
         value.value = singleValueField.getValue();
         simpleRestriction.value = value;
         return simpleRestriction;
      }
   },

   // pass restriction to populate the gene list in variant filter
   setRestrictionExpression : function(restriction) {

      if ( restriction instanceof Conjunction || restriction instanceof Disjunction ) {

         for (var i = 0; i < restriction.restrictions.length; i++) {

            var rest1 = restriction.restrictions[i];

            this.populateMultiComboItem( rest1 );

         }

      } else if ( restriction instanceof VariantTypeRestriction ) {
         // VariantType is implied by the container

      } else if ( restriction instanceof SimpleRestriction ) {
         this.setSimpleRestrictionExpression( restriction );
      } else {
         this.populateMultiComboItem( restriction );
      }

   },

   setSimpleRestrictionExpression : function(restriction) {

      var operatorComboBox = this.getComponent( "operatorComboBox" );
      var multicombo_container = this.getComponent( "multicombo_container" );
      var multicombo = multicombo_container.getComponent( "multicombo" );
      var singleValueField = multicombo_container.getComponent( "singleValueField" );

      singleValueField.on( 'change', function() {

         ASPIREdb.EVENT_BUS.fireEvent( 'query_update' );

      } );

      var propertyComboBox = me.getComponent( "propertyComboBox" );

      this.selectedProperty = restriction.property;
      propertyComboBox.setValue( restriction.property.displayName );
      operatorComboBox.setValue( restriction.operator );

      singleValueField.setValue( restriction.value.value );

      this.isMultiValue = false;
      multicombo.hide();
      singleValueField.show();

   },

   // populate multi combo based on property combo selection
   populateMultiComboItem : function(restriction) {

      var r = restriction;

      var propertyComboBox = this.getComponent( "propertyComboBox" );
      var operatorComboBox = this.getComponent( "operatorComboBox" );
      var multicombo_container = this.getComponent( "multicombo_container" );
      var multicombo = multicombo_container.getComponent( "multicombo" );

      this.selectedProperty = r.property;
      propertyComboBox.setValue( r.property.displayName );
      operatorComboBox.setValue( r.operator );

      // set property in combo store for dwr calls
      multicombo.setProperty( r.property );

      for (var j = 0; j < r.values.length; j++) {

         var itemElement = Ext.create( 'ASPIREdb.view.filter.multicombo.Item', {
            text : r.values[j].label,
            value : r.values[j]
         } );

         multicombo.addMultiComboItem( itemElement );
         multicombo_container.add( multicombo );
      }

      this.enableEnterList( r.property );

   },
   // populate gene list from the text box in variant filter
   populateMultiComboItemFromImportList : function(vos) {

      var multicombo_container = this.getComponent( "multicombo_container" );
      var multicombo = multicombo_container.getComponent( "multicombo" );

      multicombo.reset();

      // set property in combo store for dwr calls
      // multicombo.setProperty(r.property);

      for (var j = 0; j < vos.length; j++) {

         var itemElement = Ext.create( 'ASPIREdb.view.filter.multicombo.Item', {
            text : vos[j].label,
            value : vos[j]
         } );

         multicombo.addMultiComboItem( itemElement );
         multicombo_container.add( multicombo );
      }

      this.enableEnterList( this.selectedProperty );

   },

   updateOperators : function(operators) {
      // update operators
      var operatorComboBox = this.getComponent( "operatorComboBox" );
      var operatorModels = Ext.Array.map( operators, function(x) {
         return {
            displayLabel : x,
            operator : x
         };
      } );
      var store = operatorComboBox.getStore();
      store.removeAll();
      store.add( operatorModels );
      operatorComboBox.select( store.getAt( 0 ) );
   },

   initComponent : function() {
      var me = this;

      this.items = [ {
         xtype : 'combo',
         itemId : 'propertyComboBox',
         emptyText : 'name',
         store : me.getPropertyStore(),
         displayField : 'displayName',
         listeners : {
            select : {
               fn : function(obj, records) {
                  // ASPIREdb.EVENT_BUS.fireEvent('query_update');
                  if ( records[0].name == "CNV Characteristics" )
                     this.setVisible( false );
               },
               scope : this,
            }
         },
      }, {
         xtype : 'combo',
         itemId : 'subPropertyComboBox',
         emptyText : 'Type characteristics',
         suggestValuesRemoteFunction : VariantService.suggestValues,
         store : {
            extend : Ext.data.Store,
            sorters : [ {
               property : 'displayName',
               direction : 'ASC'
            } ],
            proxy : {
               type : 'dwr',
               dwrFunction : VariantService.suggestPropertiesForVariantTypeInProject,
               dwrParams : [ 'CNV', ASPIREdb.ActiveProjectSettings.getActiveProjectIds()[0] ],
               model : 'ASPIREdb.model.Property',
               reader : {
                  type : 'json',
                  root : 'data',
                  totalProperty : 'count'
               }
            }
         },
         displayField : 'displayName',
         filterItemType : 'ASPIREdb.view.filter.PropertyFilter',
         hidden : true
      },

      {
         xtype : 'combo',
         itemId : 'operatorComboBox',
         emptyText : 'operator',
         displayField : 'displayLabel',
         queryMode : 'local',
         width : 100,
         store : {
            proxy : {
               type : 'memory'
            },
            model : 'ASPIREdb.model.Operator'
         }
      }, {
         /* multi value vs single value */
         xtype : 'container',
         itemId : 'multicombo_container',
         items : [ {
            xtype : 'multivalue_combo',
            itemId : 'multicombo',
            width : 400, // 450,
            enableKeyEvents : false,
            suggestValuesRemoteFunction : me.getSuggestValuesRemoteFunction(),
         }, {
            xtype : 'numberfield',
            itemId : 'singleValueField',
            width : 300,
            enableKeyEvents : true,
            hidden : true
         }, {
            xtype : 'label',
            itemId : 'example',
            text : '',
            style : {
               'top' : '25px',
               'font-size' : '10px',
               'color' : 'gray'
            }
         } ]
      }, {
         xtype : 'button',
         itemId : 'removeButton',
         text : 'X'
      }, {
         itemId : 'enterListButton',
         xtype : 'button',
         text : '',
         tooltip : 'Enter list...',
         icon : 'scripts/ASPIREdb/resources/images/icons/page_upload.png',
         disabled : true,
         hidden : true
      }, {
         xtype : 'label',
         itemId : 'operationLabel',
         text : ''
      }, ];

      this.callParent();
      var multicombo_container = me.getComponent( "multicombo_container" );
      var operatorComboBox = me.getComponent( "operatorComboBox" );
      var multicombo = multicombo_container.getComponent( "multicombo" );
      var singleValueField = multicombo_container.getComponent( "singleValueField" );
      var example = multicombo_container.getComponent( "example" );
      var propertyComboBox = me.getComponent( "propertyComboBox" );
      var subPropertyComboBox = me.getComponent( "subPropertyComboBox" );
      var andLabel = me.getComponent( "andLabel" );
      var orLabel = me.getComponent( "orLabel" );

      var firstTime = true;

      subPropertyComboBox.on( 'select', function(obj, records) {
         var record = records[0];

         var property = record.raw;
         if ( property.dataType instanceof NumericalDataType ) {
            me.isMultiValue = false;
            multicombo.hide();
            singleValueField.reset();
            singleValueField.show();
         } else {
            me.isMultiValue = true;
            // update multicombobox
            multicombo.setProperty( property );
            multicombo.reset();
            multicombo.show();
            singleValueField.hide();
         }

         me.selectedProperty = property;
         me.enableEnterList( property );

         me.updateOperators( record.data.operators );
      } );

      propertyComboBox.on( 'select', function(obj, records) {
         var record = records[0];
         var value = record.data.displayName;

         if ( value == "CNV Characteristics" ) {
            var subPropertyComboBox = me.getComponent( "subPropertyComboBox" );
            subPropertyComboBox.setVisible( true );
            subPropertyComboBox.store.proxy.dwrParams[0] = "CNV";
            subPropertyComboBox.emptyText = 'Type characteristics';

            var storeInstance = Ext.create( 'Ext.data.Store', {
               proxy : {
                  type : 'dwr',
                  dwrFunction : VariantService.suggestPropertiesForVariantTypeInProject,
                  dwrParams : [ 'CNV', ASPIREdb.ActiveProjectSettings.getActiveProjectIds()[0] ],
                  model : 'ASPIREdb.model.Property',
                  reader : {
                     type : 'json',
                     root : 'data',
                     totalProperty : 'count'
                  }
               },
               sortOnLoad : true,
               autoLoad : true
            } );

            subPropertyComboBox.store.reload( storeInstance );

            // context menu
            /**
             * VariantService.suggestPropertiesForVariantType( 'CNV', { callback : function(Properties) {
             * 
             * 
             * var menuItems = []; // TODO: context menu handlers for (var i = 0; i < Properties.length; i++) {
             * menuItems.push( { text : Properties[i].displayName, scope : me, listeners : { itemclick : function(item) {
             * alert( "selected item " + item.displayName ); me.selectedProperty = item; } }, handler :
             * me.contextMenuSelectionHandler, } ); }
             * 
             * var contextMenu = new Ext.menu.Menu( { items : menuItems, } );
             * 
             * contextMenu.showAt( 650, 410 ); } } );
             */

         } else if ( value == "SNV Characteristics" ) {

            var subPropertyComboBox = me.getComponent( "subPropertyComboBox" );
            subPropertyComboBox.setVisible( true );
            subPropertyComboBox.store.proxy.dwrParams[0] = "SNV";
            subPropertyComboBox.emptyText = 'Type characteristics';

            var storeInstance = Ext.create( 'Ext.data.Store', {
               proxy : {
                  type : 'dwr',
                  dwrFunction : VariantService.suggestPropertiesForVariantTypeInProject,
                  dwrParams : [ 'SNV', ASPIREdb.ActiveProjectSettings.getActiveProjectIds()[0] ],
                  model : 'ASPIREdb.model.Property',
                  reader : {
                     type : 'json',
                     root : 'data',
                     totalProperty : 'count'
                  }
               },
               sortOnLoad : true,
               autoLoad : true
            } );

            subPropertyComboBox.store.reload( storeInstance );

         } else if ( value == "Indel Characteristics" ) {

            var subPropertyComboBox = me.getComponent( "subPropertyComboBox" );
            subPropertyComboBox.setVisible( true );
            subPropertyComboBox.store.proxy.dwrParams[0] = "INDEL";
            subPropertyComboBox.emptyText = 'Type characteristics';

            var storeInstance = Ext.create( 'Ext.data.Store', {
               proxy : {
                  type : 'dwr',
                  dwrFunction : VariantService.suggestPropertiesForVariantTypeInProject,
                  dwrParams : [ 'INDEL', ASPIREdb.ActiveProjectSettings.getActiveProjectIds()[0] ],
                  model : 'ASPIREdb.model.Property',
                  reader : {
                     type : 'json',
                     root : 'data',
                     totalProperty : 'count'
                  }
               },
               sortOnLoad : true,
               autoLoad : true
            } );

            subPropertyComboBox.store.reload( storeInstance );

         } else {
            var subPropertyComboBox = me.getComponent( "subPropertyComboBox" );
            subPropertyComboBox.setVisible( false );
         }

         // update examples
         var queryExample = record.data.exampleValues;
         example.setText( queryExample, false );

         if ( !subPropertyComboBox.isVisible() ) {
            me.updateOperators( record.data.operators );
         } else {
            // clear operator combobox
            var operatorComboBox = me.getComponent( "operatorComboBox" );
            var store = operatorComboBox.getStore();
            store.removeAll();
            operatorComboBox.select( null );
         }

         var property = record.raw;
         if ( property.dataType instanceof NumericalDataType ) {
            me.isMultiValue = false;
            multicombo.hide();
            singleValueField.reset();
            singleValueField.show();
         } else {
            me.isMultiValue = true;
            // update multicombobox
            multicombo.setProperty( property );
            multicombo.reset();
            multicombo.show();
            singleValueField.hide();
         }

         me.selectedProperty = property;

         me.enableEnterList( property );

      } );

      me.getComponent( "removeButton" ).on( 'click', function(button, event) {

         var item = button.ownerCt;
         var filterContainer = item.ownerCt;
         filterContainer.remove( item );
         filterContainer.doLayout();

         if ( filterContainer.ownerCt.closeEmptyFilter )
            filterContainer.ownerCt.closeEmptyFilter();
      } );

      me.getComponent( "enterListButton" ).on( 'click', function(button, event) {
         example.destroy();
         ASPIREdb.view.filter.TextImportWindow.setPropertyFilterAndShow( me );
      } );

      propertyComboBox.getStore().on( 'load', function(store, records, successful) {

         var properties = [];
         for (var i = 0; i < records.length; i++) {
            properties.push( records[i].data.displayName );
         }
         // add Gene Set to the property list
         properties.push( 'GeneSet' );
         var geneSetProperty = new Property();
         geneSetProperty.displayName = 'GeneSet';
         geneSetProperty.name = 'GeneSet';
         store.data.add( geneSetProperty );

         propertyComboBox.select( store.getAt( 0 ) );
         propertyComboBox.fireEvent( 'select', propertyComboBox, [ store.getAt( 0 ) ] );
      } );

      propertyComboBox.getStore().on( 'datachanged', function(store, e0pts) {
         console.log( 'datachanged fired' );

      } );
   },

   contextMenuSelectionHandler : function(selProperty) {

      Ext.Msg.alert( 'Selected Sub-Property', selProperty.displayName + '  selected' );
      var propertyComboBox = this.getComponent( "propertyComboBox" );
      var existingStore = propertyComboBox.getStore();

      var newStore = Ext.create( 'Ext.data.Store', {
         model : 'ASPIREdb.model.Property',
         autoLoad : true,
         displayField : 'displayName',
         filterItemType : 'ASPIREdb.view.filter.PropertyFilter',
      } );

      existingStore.add( me.selectedProperty );
      propertyComboBox.select( store.getAt( 0 ) );
   },

   enableEnterList : function(property) {

      if ( property.displayName == 'Location' || property.displayName == 'Gene'
         || property.displayName == 'Neurocarta Phenotype' ) {

         this.getComponent( "enterListButton" ).enable();
         this.getComponent( "enterListButton" ).show();

      } else {
         this.getComponent( "enterListButton" ).disable();
         this.getComponent( "enterListButton" ).hide();
      }

   },

   setOperationLabel : function(operation) {
      this.down( '#operationLabel' ).setText( operation );
   }

} );
