Ext.require([ 'Ext.layout.container.*', 'ASPIREdb.view.filter.PropertyFilter' ]);

Ext.define('ASPIREdb.view.filter.OrFilterContainer', {
	extend : 'ASPIREdb.view.filter.FilterPanel',
	alias : 'widget.filter_or',
	closable : true,
	title : 'OR Filter',
	width: 900,
	layout : {
		type : 'vbox'
	},
	config : {
		propertyStore : null,
		suggestValuesRemoteFunction : null
	},
	/*
	 * border: 1, style: { border: "1px solid lightgray" },
	 */
	getRestrictionExpression : function() {
		var filterContainer = this.getComponent('filterContainer');
		return filterContainer.getRestrictionExpression();
	},

	setRestrictionExpression : function(restriction) {
		var filterContainer = this.getComponent('filterContainer');

		filterContainer.removeAll();

		filterContainer.setRestrictionExpression(restriction);

	},

	initComponent : function() {
		var me = this;
		this.items = [ {
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
				var disjunction = new Disjunction();
				disjunction.restrictions = [];
				this.items.each(function(item, index, length) {
					disjunction.restrictions.push(item.getRestrictionExpression());
				});
				return disjunction;
			},

			setRestrictionExpression : function(restriction) {

				var filterContainer = me.getComponent("filterContainer");
				
				if (restriction instanceof Disjunction){
					
					for (var i = 0 ; i < restriction.restrictions.length; i++){
						
						var filter = Ext.create('ASPIREdb.view.filter.PropertyFilter', {
							propertyStore : me.getPropertyStore(),
							suggestValuesRemoteFunction : me.getSuggestValuesRemoteFunction()
						});

						filter.setRestrictionExpression(restriction.restrictions[i]);

						filterContainer.add(filter);
						
					}
					
				}else{
					//this else block probably never gets called
					var filter = Ext.create('ASPIREdb.view.filter.PropertyFilter', {
						propertyStore : me.getPropertyStore(),
						suggestValuesRemoteFunction : me.getSuggestValuesRemoteFunction()
					});

					filter.setRestrictionExpression(restriction);

					filterContainer.add(filter);					
					
				}
				
				filterContainer.doLayout();

			},

			items : [ {
				xtype : 'filter_property',
				itemId : 'filterProperty',
				propertyStore : me.getPropertyStore(),
				suggestValuesRemoteFunction : me.getSuggestValuesRemoteFunction()
			} ]
		}, {
			xtype : 'button',
			itemId : 'addButton',
			padding: '5 5 5 5',
			text : 'OR'
		} ];

		this.callParent();

		me.getComponent("addButton").on('click', function(button, event) {
			var filterContainer = me.getComponent("filterContainer");
			filterContainer.add(Ext.create('ASPIREdb.view.filter.PropertyFilter', {
				propertyStore : me.getPropertyStore(),
				suggestValuesRemoteFunction : me.getSuggestValuesRemoteFunction()
			}));
			filterContainer.doLayout();
		});
	}
});
