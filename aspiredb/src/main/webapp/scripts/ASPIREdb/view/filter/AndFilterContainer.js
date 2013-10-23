Ext.require([ 'Ext.layout.container.*', 'ASPIREdb.view.filter.OrFilterContainer' ]);

Ext.define('ASPIREdb.view.filter.AndFilterContainer', {
	extend : 'Ext.Container',
	alias : 'widget.filter_and',
	layout : {
		type : 'vbox'
	},
	config : {
		propertyStore : null,
		suggestValuesRemoteFunction : null,
		filterItemType : null
	},
	items : [ {
		xtype : 'container',
		itemId : 'filterContainer',
		layout : {
			type : 'vbox',
			defaultMargins : {
				top : 5,
				right : 5,
				left : 5,
				bottom : 5
			}
		},
		getRestrictionExpression : function() {
			var conjunction = new Conjunction();
			conjunction.restrictions = [];
			this.items.each(function(item, index, length) {
				conjunction.restrictions.push(item.getRestrictionExpression());
			});
			return conjunction;
		}
	}, {
		xtype : 'button',
		itemId : 'addButton',
		text : 'AND'
	} ],

	getRestrictionExpression : function() {
		var filterContainer = this.getComponent('filterContainer');
		return filterContainer.getRestrictionExpression();
	},

	setRestrictionExpression : function(restriction) {
		var filterContainer = this.getComponent('filterContainer');

		var filterItemType = this.getFilterItemType();

		if (filterItemType == 'ASPIREdb.view.filter.PhenotypeFilter') {

			filterContainer.removeAll();
			for ( var i = 0; i < restriction.restrictions.length; i++) {

				var item = this.getNewItem();

				item.setRestrictionExpression(restriction.restrictions[i]);

				filterContainer.add(item);

			}
		}// else if (filterItemType == 'ASPIREdb.view.filter.PropertyFilter') {

		//	filterContainer.removeAll();

		//	var item = this.getNewItem();

		//	item.setRestrictionExpression(restriction);

		//	filterContainer.add(item);

		//} 
	else if (filterItemType == 'ASPIREdb.view.filter.OrFilterContainer' || filterItemType == 'ASPIREdb.view.filter.PropertyFilter') {
			filterContainer.removeAll();

			//this next bit of code is ugly and ridiculous, and can probably be trimmed down a bit
			if (restriction.restrictions) {

				for ( var i = 0; i < restriction.restrictions.length; i++) {

					var rest1 = restriction.restrictions[i];

					if (rest1.restrictions) {

						var rest1Array = rest1.restrictions;

						for ( var j = 0; j < rest1Array.length; j++) {

							rest2 = rest1Array[j];

							if (rest2.restrictions) {

								var rest2Array = rest2.restrictions;

								for ( var k = 0; k < rest2Array.length; k++) {
									var rest3 = rest2Array[k];									
									
									var item = this.getNewItem();				

									item.setRestrictionExpression(rest3);

									filterContainer.add(item);
								}

							} else {
								
								var item = this.getNewItem();				

								item.setRestrictionExpression(rest2);

								filterContainer.add(item);
								
							}

						}

					} else{
						var item = this.getNewItem();				

						item.setRestrictionExpression(rest1);

						filterContainer.add(item);
					}

				}

			}else {
				
				var item = this.getNewItem();				

				item.setSimpleRestrictionExpression(restriction);

				filterContainer.add(item);
				
				
			}

		}

	},

	getNewItem : function() {
		return Ext.create(this.getFilterItemType(), {
			propertyStore : this.getPropertyStore(),
			suggestValuesRemoteFunction : this.getSuggestValuesRemoteFunction()
		});
	},

	initComponent : function() {
		this.callParent();

		var me = this;
		var filterContainer = this.getComponent("filterContainer");

		var item = this.getNewItem();
		// Add first item.
		filterContainer.insert(0, item);

		// Attach button listener
		me.getComponent("addButton").on('click', function(button, event) {
			filterContainer.add(me.getNewItem());
			filterContainer.doLayout();
		});
	}

});
