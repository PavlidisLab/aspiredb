Ext.require([ 'Ext.layout.container.*', 'ASPIREdb.view.filter.multicombo.MultiValueCombobox', 'ASPIREdb.model.Operator','ASPIREdb.view.filter.TextImportWindow' ]);

Ext.define('ASPIREdb.view.filter.PropertyFilter', {
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
			//VariantType is implied by the container
			
		} else if (restriction instanceof SimpleRestriction){
			this.setSimpleRestrictionExpression(restriction);
		} 
		else {
			this.populateMultiComboItem(restriction);
		}

	},

		
	setSimpleRestrictionExpression : function(restriction) {
		
		var operatorComboBox = this.getComponent("operatorComboBox");	
		
		var multicombo_container = this.getComponent("multicombo_container");
		var multicombo = multicombo_container.getComponent("multicombo");
		
		var singleValueField = multicombo_container.getComponent("singleValueField");
		
		singleValueField.on('change', function(){
			
			ASPIREdb.EVENT_BUS.fireEvent('query_update');
			
		});
		
		var propertyComboBox = me.getComponent("propertyComboBox");

		this.selectedProperty = restriction.property;
		propertyComboBox.setValue(restriction.property.displayName);
		operatorComboBox.setValue(restriction.operator);

		singleValueField.setValue(restriction.value.value);
		
		this.isMultiValue = false;
		multicombo.hide();		
		singleValueField.show();

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
		
		//set property in combo store for dwr calls
		multicombo.setProperty(r.property);

		for ( var j = 0; j < r.values.length; j++) {

			var itemElement = Ext.create('ASPIREdb.view.filter.multicombo.Item', {
				text : r.values[j].label,
				value : r.values[j]
			});

			multicombo.addMultiComboItem(itemElement);
			multicombo_container.add(multicombo);
		}
		
		this.enableEnterList(r.property);
		

	},
	
	populateMultiComboItemFromImportList : function(vos) {

		
		var multicombo_container = this.getComponent("multicombo_container");
		var multicombo = multicombo_container.getComponent("multicombo");
		
		multicombo.reset();

				
		//set property in combo store for dwr calls
		//multicombo.setProperty(r.property);

		for ( var j = 0; j < vos.length; j++) {

			var itemElement = Ext.create('ASPIREdb.view.filter.multicombo.Item', {
				text : vos[j].label,
				value : vos[j]
			});

			multicombo.addMultiComboItem(itemElement);
			multicombo_container.add(multicombo);
		}
		
		this.enableEnterList(this.selectedProperty);

	},

	initComponent : function() {
		var me = this;
		this.items = [ {
			xtype : 'combo',
			itemId : 'propertyComboBox',
			emptyText: 'name',
			store : me.getPropertyStore(),
			displayField : 'displayName'
		}, {
			xtype : 'combo',
			itemId : 'operatorComboBox',
			emptyText: 'operator',
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
				width : 450,
				height : 20,
				suggestValuesRemoteFunction : me.getSuggestValuesRemoteFunction()
			}, {
				xtype : 'numberfield',
				itemId : 'singleValueField',
				width : 450,
				height : 20,
				enableKeyEvents : true,
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
		},
		
		{			
			itemId : 'enterListButton',
			xtype : 'button',
			text : '',
			tooltip : 'Enter list...',
			icon : 'scripts/ASPIREdb/resources/images/icons/page_upload.png',
			disabled: true,
			hidden: true			
		}];

		this.callParent();
		var multicombo_container = me.getComponent("multicombo_container");
		var operatorComboBox = me.getComponent("operatorComboBox");
		var multicombo = multicombo_container.getComponent("multicombo");
		var singleValueField = multicombo_container.getComponent("singleValueField");
		var example = multicombo_container.getComponent("example");
		var propertyComboBox = me.getComponent("propertyComboBox");
		
		var firstTime= true;

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
			
			me.enableEnterList(property);
						
		});

		
		
		
		me.getComponent("removeButton").on('click', function(button, event) {
			
			var item = button.ownerCt;
			var filterContainer = item.ownerCt;
			filterContainer.remove(item);
			filterContainer.doLayout();
				
			if(filterContainer.ownerCt.closeEmptyFilter) filterContainer.ownerCt.closeEmptyFilter();
		});
		
		me.getComponent("enterListButton").on('click', function(button, event) {
			example.destroy();
			ASPIREdb.view.filter.TextImportWindow.setPropertyFilterAndShow(me);
		});

		propertyComboBox.getStore().on('load', function(store, records, successful) {
			propertyComboBox.select(store.getAt(0));
			propertyComboBox.fireEvent('select', propertyComboBox, [ store.getAt(0) ]);
		});
	},
	
	enableEnterList: function(property){
		
		if (property.displayName == 'Location' || property.displayName == 'Gene' || property.displayName== 'Neurocarta Phenotype'){
			
			this.getComponent("enterListButton").enable();			
			this.getComponent("enterListButton").show();
			
		}else{
			this.getComponent("enterListButton").disable();			
			this.getComponent("enterListButton").hide();
		}
		
	}
});
