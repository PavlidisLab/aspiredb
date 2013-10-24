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
			
			var cnvRestrictionExpression = filterContainer.getRestrictionExpression();
			
			var variantRestriction = new VariantTypeRestriction();
			
			variantRestriction.type = "CNV";
			
			cnvRestrictionExpression.restrictions.push(variantRestriction);
			
			return cnvRestrictionExpression;
			
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
				//autoLoad : true,
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
			
			var indelRestrictionExpression = filterContainer.getRestrictionExpression();
			return indelRestrictionExpression;
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
		
		
		var locationRestrictions = new Conjunction();
		var conjunctions = [];
		
		var cnvRestrictions = new Conjunction();
		var indelRestrictions = new Conjunction();
		var variantTypeDisjunctions = [];
		
		if (config.restriction.restrictions){
			
			restrictions = config.restriction.restrictions;
			
			for (var i = 0 ; i<restrictions.length ; i++){
				
				if (restrictions[i] instanceof Conjunction){
					conjunctions.push(restrictions[i]);
				}else if (restrictions[i] instanceof Disjunction){
					variantTypeDisjunctions.push(restrictions[i]);
				}
				
			}			
			
		}
		
		locationRestrictions.restrictions = conjunctions;

		locationFilterContainer.setRestrictionExpression(locationRestrictions);
		
		cnvRestrictions.restrictions = this.separateVariantDisjunctions(variantTypeDisjunctions, "CNV");
		
		cnvFilterPanel.setRestrictionExpression(cnvRestrictions);

		// indelFilterPanel.setRestrictionExpression(config.restriction);

	},
	
	separateVariantDisjunctions : function(disjunctions, variantType){
		
		var restriction = {};
		restriction.restrictions = disjunctions;
		
		var separatedDisjunctions = [];
		
		var addVariantRestrictionToDisjunctions = function(innerRestriction, outerRestriction, somethingElseToDo){
			
			if (innerRestriction.type  && innerRestriction.type == variantType){
				
				separatedDisjunctions.push(outerRestriction);
				
			}
			
		};
		
		var somethingElseToDoFunction = function(){
			
		};
		
		FilterUtil.traverseRidiculousObjectQueryGraphAndDoSomething(restriction, addVariantRestrictionToDisjunctions, somethingElseToDoFunction);
		
		return separatedDisjunctions;
		
	},

	initComponent : function() {
		this.callParent();
	}

});
