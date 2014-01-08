Ext.require([ 'Ext.layout.container.*', 'ASPIREdb.view.filter.OrFilterContainer' ]);

Ext.define('ASPIREdb.view.filter.ProjectOverlapFilterContainer', {
	extend : 'Ext.Container',
	alias : 'widget.filter_projectoverlap',
	layout : {
		type : 'vbox'
	},
	config : {
		propertyStore : {
			
			proxy : {
				type : 'dwr',
				dwrFunction : VariantService.suggestPropertiesForProjectOverlap,				
				model : 'ASPIREdb.model.Property',
				reader : {
					type : 'json',
					root : 'data',
					totalProperty : 'count'
				}
			}
		},
		propertyStore2 : {
			
			proxy : {
				type : 'dwr',
				dwrFunction : VariantService.suggestPropertiesForNumberOfVariantsInProjectOverlap,				
				model : 'ASPIREdb.model.Property',
				reader : {
					type : 'json',
					root : 'data',
					totalProperty : 'count'
				}
			}
		},
		propertyStore3 : {
			
			proxy : {
				type : 'dwr',
				dwrFunction : VariantService.suggestPropertiesForSupportOfVariantsInProjectOverlap,				
				model : 'ASPIREdb.model.Property',
				reader : {
					type : 'json',
					root : 'data',
					totalProperty : 'count'
				}
			}
		},
		suggestValuesRemoteFunction : null,
		filterItemType : 'ASPIREdb.view.filter.PropertyFilter'
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
		}
	} ],

	getRestrictionExpression : function() {
		var filterContainer = this.getComponent('filterContainer');
		
		var projectOverlapConfig = new ProjectOverlapFilterConfig();
        
		projectOverlapConfig.projectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds();
		
		
		var specialProjectComboBox = filterContainer.getComponent('specialProjectComboBox');
		var overlapProjectIds = [];
		overlapProjectIds.push(specialProjectComboBox.getValue()); 
		
		projectOverlapConfig.overlapProjectIds = overlapProjectIds;
		
		var overlapRestriction = filterContainer.getComponent('overlapItem').getRestrictionExpression();
		
		projectOverlapConfig.restriction1 = this.validateOverlapRestriction(overlapRestriction);
		
		var numVariantsOverlapRestriction = filterContainer.getComponent('numVariantsOverlapItem').getRestrictionExpression();
		
		projectOverlapConfig.restriction2 = this.validateOverlapRestriction(numVariantsOverlapRestriction);
		
		var supportOfVariantsOverlapRestriction = filterContainer.getComponent('supportOfVariantsOverlapItem').getRestrictionExpression();
		
		projectOverlapConfig.restriction3 = this.validateOverlapRestriction(supportOfVariantsOverlapRestriction);
				
		projectOverlapConfig.phenotypeRestriction = filterContainer.getComponent('phenRestriction').getRestrictionExpression();
		
		return projectOverlapConfig;
	},

	setRestrictionExpression : function(restriction) {
		var filterContainer = this.getComponent('filterContainer');

		var getNewOverlapItem = this.getNewOverlapItemFunction();

		//TODO this will get called when a saved query is reconstructed, implement later after requirements are more clear
	},
	
	validateOverlapRestriction : function(restriction){
		
		
		//the widget defaults to a SetRestriction if nothing is set, so return SimpleRestriction(what dwr expects) if it is not set
		if (restriction.operator == null || restriction.property == null){
			
			var simpleRestriction = new SimpleRestriction();
			simpleRestriction.property = null;
			simpleRestriction.operator = null;
			var value = new NumericValue();
			value.value = null;
			simpleRestriction.value = value;
			return simpleRestriction;
			
		}
		
			
		return restriction;
	},
	
	
	getNewOverlapItemFunction : function(remoteFunction, storeRef, id) {

		var filterTypeItem = this.getFilterItemType();
				
		var getNewOverlapItem = function() {

			return Ext.create(filterTypeItem, {
				propertyStore : storeRef,
				suggestValuesRemoteFunction : remoteFunction,
				itemId : id
			});

		};

		return getNewOverlapItem;
	},

	initComponent : function() {
		this.callParent();
		
		var filterContainer = this.getComponent("filterContainer");

		var getNewOverlapItem = this.getNewOverlapItemFunction(VariantService.suggestPropertiesForProjectOverlap, this.getPropertyStore(),'overlapItem');

		var overlapItem = getNewOverlapItem();
		
		var getNewNumVariantsOverlapItem = this.getNewOverlapItemFunction(VariantService.suggestPropertiesForNumberOfVariantsInProjectOverlap, this.getPropertyStore2(), 'numVariantsOverlapItem');

		var numVariantsOverlapItem = getNewNumVariantsOverlapItem();
		
		var getNewVariantSupportOverlapItem = this.getNewOverlapItemFunction(VariantService.suggestPropertiesForSupportOfVariantsInProjectOverlap, this.getPropertyStore3(), 'supportOfVariantsOverlapItem');

		var supportOfVariantsOverlapItem = getNewVariantSupportOverlapItem();
		
		filterContainer.insert(0, {xtype: 'filter_phenotype_property', itemId : 'phenRestriction' });
		filterContainer.insert(0, {
			xtype : 'label',
			text : 'Phenotype Association of target project variants restriction: '
		});
		
		filterContainer.insert(0, supportOfVariantsOverlapItem);
		filterContainer.insert(0, {
			xtype : 'label',
			text : 'Number of Different Support Evidence restriction: '
		});
		
		filterContainer.insert(0, numVariantsOverlapItem);
		filterContainer.insert(0, {
			xtype : 'label',
			text : 'Number of Variants Overlapped restriction: '
		});
		
		filterContainer.insert(0, overlapItem);
		filterContainer.insert(0, {
			xtype : 'label',
			text : 'Length or %Length of Overlap restriction: '
		});
		filterContainer.insert(0,{
			xtype : 'combo',
			itemId : 'specialProjectComboBox',
			editable : false,
			forceSelection : true,
			value : 'FILTER_PLACEHOLDER',
			store : [ [ 'QUERY_NAME_PLACEHOLDER', '<Query name>' ] ]
		});
		
		filterContainer.insert(0, {
			xtype : 'label',
			text : 'Project Name to search for overlap: '
		});
		

		this.updateSpecialProjectCombo();
		
		
		this.down('#specialProjectComboBox').on('select', this.specialProjectComboBoxSelectHandler, this);
		
		/*
		me.getComponent("addButton").on('click', function(button, event) {
			
			filterContainer.add( {
				xtype : 'label',
				text : 'Project Name: '
			});
			
			filterContainer.add({
				xtype : 'combo',
				itemId : 'specialProjectComboBox',
				editable : false,
				forceSelection : true,
				value : 'FILTER_PLACEHOLDER',
				store : [ [ 'QUERY_NAME_PLACEHOLDER', '<Query name>' ] ]
			});
			
			filterContainer.add(getNewOverlapItem());
			filterContainer.add({xtype: 'filter_phenotype_property' });
			filterContainer.doLayout();
		});
		*/
	},
	
	updateSpecialProjectCombo : function() {

		var specialProjectComboBox = this.down('#specialProjectComboBox');

		//this may run into security problems for regular users
		ProjectService.getOverlapProjects({
			//replace this with name/id
			callback : function(pvos) {

				var storedata = [ [ 'Value', '<Project name>' ] ];

				for ( var i = 0; i < pvos.length; i++) {

					storedata.push([ pvos[i].id, pvos[i].name ]);

				}

				specialProjectComboBox.getStore().loadData(storedata);

			}
		});

	},

});
