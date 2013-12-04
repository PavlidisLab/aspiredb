Ext.require([ 'Ext.Window' ]);

Ext.define('ASPIREdb.view.DashboardWindow', {
	extend : 'Ext.Window',
	alias : 'widget.dashboardWindow',
	singleton : true,
	title : 'Dashboard',
	closable : true,
	closeAction : 'hide',
	width : 400,
	height : 250,
	layout : {
		type : 'vbox',
		align : 'left'
	},
	bodyStyle : 'padding: 5px;',
	border: false,

	initComponent : function() {

		this.callParent();

		var ref = this;
		var projectStore = Ext.create('Ext.data.Store', {
			proxy : {
				type : 'dwr',
				dwrFunction : ProjectService.getProjects,
				model : 'ASPIREdb.model.Project',
				reader : {
					type : 'json',
					root : 'name'
				}
			}
		});

		var projectComboBox = Ext.create('Ext.form.ComboBox', {
			id : 'projectField',
			name : 'unit',
			fieldLabel : 'Project',
			store : projectStore,
			editable : false,
			displayField : 'name',
			allowBlank : false,
			valueField : 'id',
			forceSelection : true,
			emptyText : "Choose project...",
			msgTarget : 'qtip'
		});

		this.add(projectComboBox);

		projectComboBox.on('select', function() {

			ProjectService.numSubjects([ projectComboBox.getValue() ], {
				callback : function(numSubjects) {

					ref.getComponent('numSubjects').setText('Number of Subjects: ' + numSubjects);

				}

			});

			ProjectService.numVariants([ projectComboBox.getValue() ], {
				callback : function(numVariants) {

					ref.getComponent('numVariants').setText('Number of Variants:  ' + numVariants);

				}

			});

		});

		var okButton = Ext.create('Ext.Button', {
			text : 'ok',
			handler : function() {

				if (!projectComboBox.getValue()) {

					projectComboBox.setActiveError('Please select project');
					return;
				}

				ASPIREdb.ActiveProjectSettings.setActiveProject([ {
					id : projectComboBox.getValue(),
					name : projectComboBox.getName(),
					description : ''
				} ]);

				var filterConfigs = [];
				var activeProjectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds();
				var projectFilter = new ProjectFilterConfig;
				projectFilter.projectIds = activeProjectIds;
				filterConfigs.push(projectFilter);
				console.log("filter_submit event from DashBoard window");
				ASPIREdb.EVENT_BUS.fireEvent('filter_submit', filterConfigs);
				
				console.log("query_update event from DashboardWindow");
				ASPIREdb.EVENT_BUS.fireEvent('query_update');
				ref.close();

			}
		});

		this.add({
			xtype : 'label',
			itemId : 'numSubjects',
			text : 'Number of Subjects:',
			margin : '20 20 5 20'
		}, {
			xtype : 'label',
			itemId : 'numVariants',
			text : 'Number of Variants:',
			margin : '5 20 20 20'
		});

		this.add(okButton);

	}

});