Ext.require([ 'Ext.layout.container.*', 'ASPIREdb.view.filter.multicombo.MultiValueCombobox', 'ASPIREdb.model.Operator' ]);

Ext.define('ASPIREdb.view.filter.PropertyFilter', {
	extend : 'Ext.Container',
	alias : 'widget.filter_property',
	width : 690,
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
		var propertyComboBox = this.getComponent("propertyComboBox");
		var operatorComboBox = this.getComponent("operatorComboBox");
		var multicombo_container = this.getComponent("multicombo_container");
		var multicombo = multicombo_container.getComponent("multicombo");
		var singleValueField = multicombo_container.getComponent("singleValueField");

		if (this.isMultiValue) {
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

	setRestrictionExpression : function(restriction) {

		if (restriction instanceof Conjunction || restriction instanceof Disjunction) {

			for ( var i = 0; i < restriction.restrictions.length; i++) {

				var rest1 = restriction.restrictions[i];

				this.populateMultiComboItem(rest1);

			}

		} else if (restriction instanceof VariantTypeRestriction){

			
		} else {
			this.populateMultiComboItem(restriction);
		}

	},
	
	setSimpleRestrictionExpression : function(restriction) {

	

			var singleValueField = multicombo_container.getComponent("singleValueField");
			// var simpleRestriction = new SimpleRestriction();
			simpleRestriction.property = this.selectedProperty = restriction.property;
			simpleRestriction.operator = operatorComboBox.getValue();
			var value = new NumericValue();
			value.value = singleValueField.getValue();
			simpleRestriction.value = value;
			return simpleRestriction;
		
	},

	populateMultiComboItem : function(restriction) {

		var r = restriction;

		var propertyComboBox = this.getComponent("propertyComboBox");
		var operatorComboBox = this.getComponent("operatorComboBox");
		var multicombo_container = this.getComponent("multicombo_container");
		var multicombo = multicombo_container.getComponent("multicombo");

		this.selectedProperty = r.property;
		propertyComboBox.setValue(r.property.displayName);
		operatorComboBox.setValue(r.operator);

		for ( var j = 0; j < r.values.length; j++) {

			var itemElement = Ext.create('ASPIREdb.view.filter.multicombo.Item', {
				text : r.values[j].label,
				value : r.values[j]
			});

			multicombo.addMultiComboItem(itemElement);
			multicombo_container.add(multicombo);
		}

	},

	initComponent : function() {
		var me = this;
		this.items = [ {
			xtype : 'combo',
			itemId : 'propertyComboBox',
			store : me.getPropertyStore(),
			displayField : 'displayName'
		}, {
			xtype : 'combo',
			itemId : 'operatorComboBox',
			displayField : 'displayLabel',
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
				xtype : 'multivalue_combo',
				itemId : 'multicombo',
				width : 400,
				height : 20,
				suggestValuesRemoteFunction : me.getSuggestValuesRemoteFunction()
			}, {
				xtype : 'textfield',
				itemId : 'singleValueField',
				width : 400,
				height : 20,
				hidden : true
			}, {
				xtype : 'label',
				itemId : 'example',
				style : {
					'padding-top' : '5px',
					'font-size' : 'smaller',
					'color' : 'gray'
				}
			} ]
		}, {
			xtype : 'button',
			itemId : 'removeButton',
			text : 'X'
		} ];

		this.callParent();
		var multicombo_container = me.getComponent("multicombo_container");
		var operatorComboBox = me.getComponent("operatorComboBox");
		var multicombo = multicombo_container.getComponent("multicombo");
		var singleValueField = multicombo_container.getComponent("singleValueField");
		var example = multicombo_container.getComponent("example");
		var propertyComboBox = me.getComponent("propertyComboBox");

		propertyComboBox.on('select', function(obj, records) {
			var record = records[0];

			// update examples
			var queryExample = record.data.exampleValues;
			example.setText(queryExample, false);

			// update operators
			var operators = record.data.operators;
			var operatorModels = Ext.Array.map(operators, function(x) {
				return {
					displayLabel : x,
					operator : x
				};
			});
			var store = operatorComboBox.getStore();
			store.removeAll();
			store.add(operatorModels);
			operatorComboBox.select(store.getAt(0));

			var property = record.raw;
			if (property.dataType instanceof NumericalDataType) {
				me.isMultiValue = false;
				multicombo.hide();
				singleValueField.reset();
				singleValueField.show();
			} else {
				me.isMultiValue = true;
				// update multicombobox
				multicombo.setProperty(property);
				multicombo.reset();
				multicombo.show();
				singleValueField.hide();
			}

			me.selectedProperty = property;
		});

		me.getComponent("removeButton").on('click', function(button, event) {
			// TODO: fix with custom events
			var item = button.ownerCt;
			var filterContainer = item.ownerCt;
			filterContainer.remove(item);
			filterContainer.doLayout();
		});

		propertyComboBox.getStore().on('load', function(store, records, successful) {
			propertyComboBox.select(store.getAt(0));
			propertyComboBox.fireEvent('select', propertyComboBox, [ store.getAt(0) ]);
		});
	}
});
