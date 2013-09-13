Ext.require([ 'Ext.Window', 'ASPIREdb.view.PhenotypeEnrichmentGrid' ]);

Ext.define('ASPIREdb.view.PhenotypeEnrichmentWindow', {
	extend : 'Ext.Window',
	alias : 'widget.phenotypeEnrichmentWindow',
	singleton : true,
	title : 'Phenotype Enrichment',
	closable : true,
	width : 800,
	height : 500,
	layout : 'fit',
	bodyStyle : 'padding: 5px;',

	items : [ {
		xtype : 'phenotypeEnrichmentGrid',
		itemId : 'phenotypeEnrichmentGrid'
	} ],

	initComponent : function() {
		var ref = this;

		this.callParent();

	},

	populateGrid : function(vos) {

		var grid = ASPIREdb.view.PhenotypeEnrichmentWindow.getComponent('phenotypeEnrichmentGrid');

		var data = [];
		for ( var i = 0; i < vos.length; i++) {
			var vo = vos[i];

			var row = [ vo.name, vo.inGroupTotalString, vo.outGroupTotalString, vo.PValueString, vo.PValueCorrectedString ];
			data.push(row);
		}

		grid.store.loadData(data);

	}

});