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
				
				if (itemRestriction instanceof PhenotypeRestriction && itemRestriction.name && itemRestriction.value){
					
					conjunction.restrictions.push(itemRestriction);
					
				}else if (itemRestriction instanceof SetRestriction && itemRestriction.operator && itemRestriction.property && itemRestriction.values && itemRestriction.values.length>0){
				
					conjunction.restrictions.push(itemRestriction);
				
				}
				else if (itemRestriction instanceof SimpleRestriction){
					//not sure where simplerestriction comes into play yet
					alert("TODO  simplerestriction");
				}
				else {
					conjunction.restrictions.push(itemRestriction);
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
		}else if (filterItemType == 'ASPIREdb.view.filter.OrFilterContainer' || filterItemType == 'ASPIREdb.view.filter.PropertyFilter') {
			filterContainer.removeAll();
			
			if (restriction.restrictions) {
				
				FilterUtil.traverseRidiculousObjectQueryGraphAndDoSomething(restriction, addMultiItemToContainer, getNewItem);

			}else {
				
				var item = this.getNewItem();				

				item.setSimpleRestrictionExpression(restriction);

				filterContainer.add(item);				
				
			}

		}

	},
	
	getAddMultiItemToContainerFunction : function(filterContainer){	
		
		//outerRestriction is unused in this function and refers to the outermost Conjunction/Disjunction
		var addMultiItemToContainer = function(restriction, outerRestriction,getNewItem){
			
			var item = getNewItem();				

			item.setRestrictionExpression(restriction);

			filterContainer.add(item);
			
		};
		
		return addMultiItemToContainer;		
		
		
	},

	getNewItemFunction : function() {
		
		var filterTypeItem = this.getFilterItemType();
		var propertyStore = this.getPropertyStore();
		var suggestValuesRemoteFunction = this.getSuggestValuesRemoteFunction();
		
		var getNewItem = function(){
		
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
			filterContainer.add(me.getNewItem());
			filterContainer.doLayout();
		});
	}

});
