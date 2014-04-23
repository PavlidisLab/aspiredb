Ext.require([
    'Ext.grid.Panel',
]);

/**var store = Ext.create('Ext.data.Store', {
    storeId:'employeeStore',
    fields:['name', 'seniority', 'department'],
    groupField: 'department',
    data: {'employees':[
        { "name": "Michael Scott",  "seniority": 7, "department": "Management" },
        { "name": "Dwight Schrute", "seniority": 2, "department": "Sales" },
        { "name": "Jim Halpert",    "seniority": 3, "department": "Sales" },
        { "name": "Kevin Malone",   "seniority": 4, "department": "Accounting" },
        { "name": "Angela Martin",  "seniority": 5, "department": "Accounting" }
    ]},
    proxy: {
        type: 'memory',
        reader: {
            type: 'json',
            root: 'employees'
        }
    }
});*/

Ext.define('ASPIREdb.view.phenotypeSubjectLabelGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.phenotypeSubjectLabelGrid',
   // title : 'Subject Labels',
    //closable: true,
    border :false,
    //header: false,
    store : Ext.create('ASPIREdb.store.PhenotypeSubjectStore'),
    columns : [ {
		header : 'characteristics Name',
		dataIndex : 'name',
		flex : 1
	}, {
		header : '#subjects',
		dataIndex : 'NoOfsubjects',
		flex : 1
	},{
		header : 'SubjectIds',
		dataIndex : 'subjectIds',
		flex : 1
		
	}],
	width: 300,
    height : 150,
    features: [{ftype:'grouping'}],
	
   
    initComponent: function () {
        this.callParent();
                 
    },
    
    populateGrid : function(phenotypeSummaryValueObjects, selPhenotypes){
    	var data = [];
		
		for (var j=0; j<selPhenotypes.length;j++){
			var phenSummary=selPhenotypes[j].data.selectedPhenotype;
				for (var key in phenSummary.phenoSummaryMap){
					var subjects=[];
					if (key=="Present" || key=="Y")
						 subjects = phenSummary.subjects[1];
					else if (key=="Absent" || key=="N")
						subjects = phenSummary.subjects[0];
					else subjects = phenSummary.subjects[key];
					var row = [ key, phenSummary.phenoSummaryMap[key], subjects.toString(),phenSummary.name ];
					data.push(row);
				}				
		}
		this.store.loadData(data);
		this.getView().refresh(true);
		
    },

});
