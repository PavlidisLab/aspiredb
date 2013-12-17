Ext.require([ 'Ext.layout.container.*', 'ASPIREdb.view.filter.OrFilterContainer' ]);

Ext.define('ASPIREdb.view.filter.ProjectOverlapFilterContainer', {
	extend : 'Ext.Container',
	alias : 'widget.filter_projectoverlap',
	layout : {
		type : 'vbox'
	},
	config : {
		propertyStore : {
			//autoLoad : true,
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
		
		var overlapItem = filterContainer.getComponent('overlapItem').getRestrictionExpression();
		
		var operator = null;
		
		if (overlapItem.operator == 'NUMERIC_EQUAL'){
			operator = 0;
		}
		if (overlapItem.operator == 'NUMERIC_GREATER'){
			operator = 1;
		}
		if (overlapItem.operator == 'NUMERIC_LESS'){
			operator = -1;
		}
		
		//hacky if else block because of poorly implemented 'X subjects and X variants will be returned' functionality that updates way too often
		if (overlapItem.value.value){
			projectOverlapConfig.overlap = overlapItem.value.value;
		}
		else{
			projectOverlapConfig.overlap= '999999999999999999';
		}
		projectOverlapConfig.operator= operator;
				
		projectOverlapConfig.phenotypeRestriction = filterContainer.getComponent('phenRestriction').getRestrictionExpression();
		
		return projectOverlapConfig;
	},

	setRestrictionExpression : function(restriction) {
		var filterContainer = this.getComponent('filterContainer');

		

		var getNewOverlapItem = this.getNewOverlapItemFunction();

		//TODO

	},

	getNewOverlapItemFunction : function() {

		var filterTypeItem = this.getFilterItemType();
		var propertyStore = this.getPropertyStore();
		var suggestValuesRemoteFunction = this.getSuggestValuesRemoteFunction();

		var getNewOverlapItem = function() {

			return Ext.create(filterTypeItem, {
				propertyStore : propertyStore,
				suggestValuesRemoteFunction : suggestValuesRemoteFunction,
				itemId : 'overlapItem'
			});

		};

		return getNewOverlapItem;
	},

	initComponent : function() {
		this.callParent();
		
		var filterContainer = this.getComponent("filterContainer");

		var getNewOverlapItem = this.getNewOverlapItemFunction();

		var overlapItem = getNewOverlapItem();
		
		filterContainer.insert(0, {xtype: 'filter_phenotype_property', itemId : 'phenRestriction' });
		filterContainer.insert(0, overlapItem);
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
