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

				var itemRestriction = item.getRestrictionExpression();

				if (FilterUtil.isSimpleRestriction(itemRestriction)) {

					if (FilterUtil.validateSimpleRestriction(itemRestriction)) {

						conjunction.restrictions.push(itemRestriction);
					}

				} else if (itemRestriction instanceof Disjunction) {

					var nonEmptyDisjunction = new Disjunction();
					
					var nonEmptyRestrictionsArray = [];

					if (itemRestriction.restrictions) {

						for ( var i = 0; i < itemRestriction.restrictions.length; i++) {
							
							var disjunctedRestriction = itemRestriction.restrictions[i];
							
							if (FilterUtil.isSimpleRestriction(disjunctedRestriction)) {

								if (FilterUtil.validateSimpleRestriction(disjunctedRestriction)) {

									nonEmptyRestrictionsArray.push(disjunctedRestriction);
								}

							}							

						}

					}

					else {
						//to help flush out any bugs
						alert("multi nested disjunction andfilterconatiner");

					}
					
					if (nonEmptyRestrictionsArray.length>0){
						
						nonEmptyDisjunction.restrictions = nonEmptyRestrictionsArray;
						
						conjunction.restrictions.push(nonEmptyDisjunction);
						
					}

				}
				else{
					//to help flush out any bugs
					alert("Unsupported Restriction andfiltercontainer");
					
				}
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

		var addMultiItemToContainer = this.getAddMultiItemToContainerFunction(filterContainer);

		var getNewItem = this.getNewItemFunction();

		var filterItemType = this.getFilterItemType();

		if (filterItemType == 'ASPIREdb.view.filter.PhenotypeFilter') {

			filterContainer.removeAll();
			for ( var i = 0; i < restriction.restrictions.length; i++) {

				addMultiItemToContainer(restriction.restrictions[i], null, getNewItem);
				

			}
		} else if (filterItemType == 'ASPIREdb.view.filter.OrFilterContainer' || filterItemType == 'ASPIREdb.view.filter.OrPhenotypeFilterContainer' || filterItemType == 'ASPIREdb.view.filter.PropertyFilter') {
			filterContainer.removeAll();

			if (restriction.restrictions) {

				FilterUtil.traverseRidiculousObjectQueryGraphAndDoSomething(restriction.restrictions, restriction, addMultiItemToContainer, getNewItem);

			} else {

				var item = this.getNewItem();

				item.setSimpleRestrictionExpression(restriction);

				filterContainer.add(item);

			}

		}

	},

	getAddMultiItemToContainerFunction : function(filterContainer) {

				
		var addMultiItemToContainer = function(restriction, outerRestriction, getNewItem) {
			
			
			if (!(restriction instanceof VariantTypeRestriction)){
				
				if (outerRestriction instanceof Disjunction){
					
					var item = getNewItem();
					
					item.setRestrictionExpression(outerRestriction);

					filterContainer.add(item);
					
					
				}else{
				
					var item = getNewItem();
				
					item.setRestrictionExpression(restriction);

					filterContainer.add(item);
				
				}
				
			}

		};

		return addMultiItemToContainer;

	},

	getNewItemFunction : function() {

		var filterTypeItem = this.getFilterItemType();
		var propertyStore = this.getPropertyStore();
		var suggestValuesRemoteFunction = this.getSuggestValuesRemoteFunction();

		var getNewItem = function() {

			return Ext.create(filterTypeItem, {
				propertyStore : propertyStore,
				suggestValuesRemoteFunction : suggestValuesRemoteFunction
			});

		};

		return getNewItem;
	},

	initComponent : function() {
		this.callParent();

		var me = this;
		var filterContainer = this.getComponent("filterContainer");

		var getNewItem = this.getNewItemFunction();

		var item = getNewItem();
		// Add first item.
		filterContainer.insert(0, item);

		// Attach button listener
		me.getComponent("addButton").on('click', function(button, event) {
			filterContainer.add(getNewItem());
			filterContainer.doLayout();
		});
	}

});
