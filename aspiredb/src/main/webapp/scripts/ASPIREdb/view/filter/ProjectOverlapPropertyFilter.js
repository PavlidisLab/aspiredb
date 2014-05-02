Ext.require( [ 'Ext.layout.container.*', 'ASPIREdb.view.filter.multicombo.MultiValueCombobox',
              'ASPIREdb.model.Operator', 'ASPIREdb.view.filter.TextImportWindow' ] );

Ext.define( 'ASPIREdb.view.filter.ProjectOverlapPropertyFilter', {
   extend : 'Ext.Container',
   alias : 'widget.filter_property',
   width : 875,
   layout : {
      type : 'hbox'
   },
   config : {
      propertyStore : null, /* property suggestions */
      suggestValuesRemoteFunction : null
   },

   isMultiValue : true,
   selectedProperty : null,

   getRestrictionExpression : function() {

      var multicombo_container = this.getComponent( "multicombo_container" );

      var operatorComboBox = this.getComponent( "operatorComboBox" );
      var singleValueField = multicombo_container.getComponent( "singleValueField" );
      var simpleRestriction = new SimpleRestriction();
      simpleRestriction.property = this.selectedProperty;
      simpleRestriction.operator = operatorComboBox.getValue();
      var value = new NumericValue();
      value.value = singleValueField.getValue();
      simpleRestriction.value = value;
      return simpleRestriction;

   },

   setRestrictionExpression : function(restriction) {
      this.setSimpleRestrictionExpression( restriction );
   },

   setSimpleRestrictionExpression : function(restriction) {
      var propertyComboBox = this.getComponent( "propertyComboBox" );
      var operatorComboBox = this.getComponent( "operatorComboBox" );

      var multicombo_container = this.getComponent( "multicombo_container" );

      var singleValueField = multicombo_container.getComponent( "singleValueField" );

      this.selectedProperty = restriction.property;

      if ( this.selectedProperty ) {
         propertyComboBox.setValue( restriction.property.displayName );
         operatorComboBox.setValue( restriction.operator );
         singleValueField.setValue( restriction.value.value );
         this.isMultiValue = false;
         singleValueField.show();
      }

   },

   initComponent : function() {
      var me = this;
      this.items = [ {
         xtype : 'combo',
         itemId : 'propertyComboBox',
         emptyText : 'name',
         store : me.getPropertyStore(),
         displayField : 'displayName',
         width : 300
      }, {
         xtype : 'combo',
         itemId : 'operatorComboBox',
         emptyText : 'operator',
         displayField : 'displayLabel',
         width : 300,
         queryMode : 'local',
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
         layout : {
            type : 'vbox'
         },
         items : [ {
            xtype : 'numberfield',
            itemId : 'singleValueField',
            width : 200,
            enableKeyEvents : true,
            hidden : true
         } ]
      } ];

      this.callParent();

      var operatorComboBox = me.getComponent( "operatorComboBox" );
      var multicombo_container = me.getComponent( "multicombo_container" );
      var singleValueField = multicombo_container.getComponent( "singleValueField" );

      singleValueField.on( 'change', function() {

         ASPIREdb.EVENT_BUS.fireEvent( 'query_update' );

      } );

      var propertyComboBox = me.getComponent( "propertyComboBox" );

      propertyComboBox.on( 'select', function(obj, records) {
         var record = records[0];

         // update operators
         var operators = record.data.operators;
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

         var property = record.raw;
         if ( property.dataType instanceof NumericalDataType ) {
            me.isMultiValue = false;

            singleValueField.reset();
            singleValueField.show();
         }

         me.selectedProperty = property;

      } );

      propertyComboBox.getStore().on( 'load', function(store, records, successful) {
         propertyComboBox.select( store.getAt( 0 ) );
         propertyComboBox.fireEvent( 'select', propertyComboBox, [ store.getAt( 0 ) ] );
      } );
   }

} );
