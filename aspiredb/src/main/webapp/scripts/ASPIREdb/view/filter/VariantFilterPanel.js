Ext.require([ 'Ext.layout.container.*', 'ASPIREdb.view.filter.AndFilterContainer', 'ASPIREdb.view.filter.OrFilterContainer', 'ASPIREdb.view.filter.FilterPanel' ]);

Ext.define('ASPIREdb.view.filter.VariantFilterPanel', {
	extend : 'ASPIREdb.view.filter.FilterPanel',
	alias : 'widget.filter_variant',
	title : 'Variant Filter',
	bodyStyle : 'background: #FFFFD0;',
	items : [ {
		xtype : 'filter_and',
		title : 'Variant Location:',
		itemId : 'locationFilterContainer',
		filterItemType : 'ASPIREdb.view.filter.OrFilterContainer',
		suggestValuesRemoteFunction : VariantService.suggestValues,
		propertyStore : {
			// autoLoad: true,
			proxy : {
				type : 'dwr',
				dwrFunction : VariantService.suggestVariantLocationProperties,
				model : 'ASPIREdb.model.Property',
				reader : {
					type : 'json',
					root : 'data',
					totalProperty : 'count'
				}
			}
		}
	}, {
		xtype : 'label',
		text : 'Variant characteristics:'
	}, {
		xtype : 'panel',
		itemId : 'cnvFilterPanel',
		bodyStyle : 'background: #FFFFD0;',
		title : 'CNV:',
		collapsible : true,
		collapsed : true,
		animCollapse : false,
		getRestrictionExpression : function() {
			var filterContainer = this.getComponent('cnvCharacteristicFilterContainer');
			return filterContainer.getRestrictionExpression();
		},
		setRestrictionExpression : function(restriction) {
			// and filter container
			var filterContainer = this.getComponent('cnvCharacteristicFilterContainer');
			filterContainer.setRestrictionExpression(restriction);
		},
		items : {
			xtype : 'filter_and',
			itemId : 'cnvCharacteristicFilterContainer',
			suggestValuesRemoteFunction : VariantService.suggestValues,
			propertyStore : {
				autoLoad : true,
				proxy : {
					type : 'dwr',
					dwrFunction : VariantService.suggestPropertiesForVariantType,
					dwrParams : [ 'CNV' ],
					model : 'ASPIREdb.model.Property',
					reader : {
						type : 'json',
						root : 'data',
						totalProperty : 'count'
					}
				}
			},
			filterItemType : 'ASPIREdb.view.filter.PropertyFilter'
		}
	}, {
		xtype : 'panel',
		itemId : 'indelFilterPanel',
		bodyStyle : 'background: #FFFFD0;',
		title : 'Indel:',
		collapsible : true,
		collapsed : true,
		animCollapse : false,
		getRestrictionExpression : function() {
			var filterContainer = this.getComponent('indelCharacteristicFilterContainer');
			return filterContainer.getRestrictionExpression();
		},
		setRestrictionExpression : function(restriction) {
			// and filter container
			var filterContainer = this.getComponent('indelCharacteristicFilterContainer');
			filterContainer.setRestrictionExpression(restriction);
		},
		items : {
			xtype : 'filter_and',
			itemId : 'indelCharacteristicFilterContainer',
			suggestValuesRemoteFunction : VariantService.suggestValues,
			propertyStore : {
				autoLoad : true,
				proxy : {
					type : 'dwr',
					dwrFunction : VariantService.suggestPropertiesForVariantType,
					dwrParams : [ 'INDEL' ],
					model : 'ASPIREdb.model.Property',
					reader : {
						type : 'json',
						root : 'data',
						totalProperty : 'count'
					}
				}
			},
			filterItemType : 'ASPIREdb.view.filter.PropertyFilter'
		}
	} ],

	getFilterConfig : function() {
		var cnvFilterPanel = this.getComponent('cnvFilterPanel');
		var indelFilterPanel = this.getComponent('indelFilterPanel');
		var config = new VariantFilterConfig();
		var conjunction = new Conjunction();
		conjunction.restrictions = [];

		var locationConjunction = new Conjunction();
		locationConjunction.restrictions = [];

		var locationFilterContainer = this.getComponent('locationFilterContainer');
		conjunction.restrictions.push(locationFilterContainer.getRestrictionExpression());

		var disjunction = new Disjunction();
		disjunction.restrictions = [];
		if (!cnvFilterPanel.getCollapsed()) {
			disjunction.restrictions.push(cnvFilterPanel.getRestrictionExpression());
		}
		if (!indelFilterPanel.getCollapsed()) {
			disjunction.restrictions.push(indelFilterPanel.getRestrictionExpression());
		}

		if (disjunction.restrictions.length > 0) {
			conjunction.restrictions.push(disjunction);
		}

		config.restriction = conjunction;
		return config;
	},

	setFilterConfig : function(config) {

		var cnvFilterPanel = this.getComponent('cnvFilterPanel');
		var indelFilterPanel = this.getComponent('indelFilterPanel');
		var locationFilterContainer = this.getComponent('locationFilterContainer');
		// var config = new VariantFilterConfig();
		// var conjunction = new Conjunction();
		// conjunction.restrictions = [];

		locationFilterContainer.setRestrictionExpression(config.restriction);

		// cnvFilterPanel.setRestrictionExpression(config.restriction);

		// indelFilterPanel.setRestrictionExpression(config.restriction);

	},

	initComponent : function() {
		this.callParent();
	}

});
