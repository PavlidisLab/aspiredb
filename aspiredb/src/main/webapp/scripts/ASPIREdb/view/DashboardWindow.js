Ext.require([
    'Ext.Window'
]);

Ext.define('ASPIREdb.view.DashboardWindow', {
        extend: 'Ext.Window',
        alias: 'widget.dashboardWindow',
        singleton: true,
        title: 'Dashboard',
        closable: true,
        closeAction: 'hide',
        width: 500,
        height: 400,
        layout: {
            type: 'vbox',
            align: 'left'
        },
        bodyStyle: 'padding: 5px;',
      
        initComponent: function () {
        	
        	this.callParent();
        	
        	var ref = this;
        	var projectStore = Ext.create('Ext.data.Store',{            	            	
                proxy: {
                    type: 'dwr',
                    dwrFunction : ProjectService.getProjects,
                    model: 'ASPIREdb.model.Project',
                    reader : {
                        type: 'json',
                        root: 'name'
                                           }
                }
            });

           var projectComboBox = Ext.create( 'Ext.form.ComboBox',{
              id:'projectField',
              name: 'unit',
              fieldLabel: 'Project',
              store: projectStore,
              editable: false,
              displayField: 'name',
              allowBlank: false,
              valueField: 'id'
               });
           
           	this.add(projectComboBox);
           	
           	var okButton = Ext.create('Ext.Button', {
           	    text: 'ok',           	    
           	    handler: function() {           	    	
           	    	ASPIREdb.ActiveProjectSettings.setActiveProject([{id: projectComboBox.getValue(), name:projectComboBox.getName(), description:''}]);
           	    	
           	    	var filterConfigs = [];
                    var activeProjectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds();
                    var projectFilter = new ProjectFilterConfig;
                    projectFilter.projectIds = activeProjectIds;
                    filterConfigs.push(projectFilter);
                    ASPIREdb.EVENT_BUS.fireEvent('filter_submit', filterConfigs);                    
                    
                    ref.close();
                    
           	    }
           	});
           	
           	this.add(okButton);
           	
           	
           	
            //this.on('show',function(event) {});
            
        }

});  