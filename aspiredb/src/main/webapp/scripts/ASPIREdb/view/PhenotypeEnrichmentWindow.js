Ext.require([
    'Ext.Window'   
]);

Ext.define('ASPIREdb.view.PhenotypeEnrichmentWindow', {
        extend: 'Ext.Window',
        alias: 'widget.phenotypeenrichmentwindow',
        singleton: true,
        title: 'Phenotype Enrichment',
        closable: true,        
        width: 700,
        height: 350,
        layout: 'border',
        bodyStyle: 'padding: 5px;',

        initComponent: function () {
            var ref = this;
            //this.items = [
                
            //];

            this.callParent();
            
        }

});  