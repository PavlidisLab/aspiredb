/*
 * The aspiredb project
 * 
 * Copyright (c) 2013 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

Ext.require([ 'ASPIREdb.store.PhenotypeStore', 'ASPIREdb.ActiveProjectSettings', 'ASPIREdb.view.PhenotypeEnrichmentWindow', 'Ext.grid.column.Column', 'ASPIREdb.view.NeurocartaGeneWindow']);

// TODO js documentation
Ext.define('ASPIREdb.view.PhenotypeGrid', {
	extend : 'Ext.grid.Panel',
	alias : 'widget.phenotypeGrid',
	title : 'Phenotype',
	id : 'phenotypeGrid',
	disableSelection:true,
	

	config : {

		// member variables

		// column id of selectedValuesColumn
		SELECTED_VALUES_COL_IDX : 1,
		
		// PhenotypeSummary styles
		STYLE_DEFAULT : "style='color: black'",
		STYLE_HPO_PRESENT : "style='color: red'",
		STYLE_HPO_ABSENT : "style='color: green'",
		DB_VAL_HPO_PRESENT : 1,
		DB_VAL_HPO_ABSENT : 0,

		// collection of all the PhenotypeSummaryValueObject loaded
		phenotypeSummaryValueObjects : [],
		
	},
	constructor : function(cfg) {
		this.initConfig(cfg);
		this.callParent(arguments);
	},
	
	dockedItems : [ {
		xtype : 'toolbar',
		itemId : 'phenotypeGridToolbar',
		dock : 'top',
		items : [ {
			xtype : 'button',
			text : 'Analyze',
			disabled : 'true',
			itemId : 'analyzeButton'

		},{			
			itemId : 'saveButton',
			xtype : 'button',
			text : '',
			tooltip : 'Download table contents as text',
			icon : 'scripts/ASPIREdb/resources/images/icons/disk.png'
						
		}]

	} ],

	columns : [ {
		text : 'Name',
		dataIndex : 'name',
		
		renderer : function(value) {
			
			var image = "";
			if (value.neurocartaPhenotype) {
				var src = 'scripts/ASPIREdb/resources/images/icons/neurocarta.png';
				var tooltip = "View genes associated in Neurocarta";
								
				var ahrefurl = '<a onclick="return Ext.getCmp(\'phenotypeGrid\').viewNeurocartaGenes(\''+value.uri+'\',\''+value.name+'\')" href=#>';
				 
				image = Ext.String.format(ahrefurl + "<img src='{0}' alt='{1}' > </a>", src, tooltip);
			}
			var ret = value.name + " " + image;
			return ret;
		},
		width : 350,
	}, 
	
	{ 
		// populated dynamically when a Subject is selected
		text : '',
		dataIndex : 'selectedPhenotype',
		hidden : true,
		width : 80,
		renderer : function(value) {
			
			var phenSummary = value.selectedPhenotype;
			if (phenSummary == null) return ret;
			var style = this.STYLE_DEFAULT;
			var displayVal = phenSummary.dbValue;
			
			if (phenSummary.valueType == "HPONTOLOGY") {
				if (phenSummary.dbValue == this.DB_VAL_HPO_ABSENT) {
					style = this.STYLE_HPO_ABSENT;
					displayVal = "Absent";
				} else if (phenSummary.dbValue == this.DB_VAL_HPO_PRESENT) {
					style = this.STYLE_HPO_PRESENT;
					displayVal = "Present";	
				}
			}
			
			var ret = "<span " + style + ">" + displayVal + "</span>";
			return ret;
		},
	} ,
	
	{
		text : 'Select Subject values',
		hidden : true,
		dataIndex : 'phenoSummaryMap',
			renderer : function(value, metadata, record) {
			
			var phenSummary = value.phenoSummaryMapSelectedSubjects;
			var displaySummary = value.displaySummarySelectedSubjects
			if (phenSummary!= null){
				var ret = "<canvas width='50' height='80' id=multi"+ value.name.replace(/ /g,'') + ">"+"</canvas>";
				metadata.tdAttr = 'data-qtip="'+displaySummary + ret + '"';
				return ret;
			} else return "";
			},
	},
	{
		text : 'Value (subject count)',
		dataIndex : 'allPhenoSummaryMap',
		renderer : function(value, metadata, record) {
		
		var phenSummary = value.phenoSummaryMap;
		
		if (phenSummary!= null){
			var ret = "<canvas width='50' height='50' id=all"+ value.name.replace(/ /g,'') + ">"+"</canvas>";
			metadata.tdAttr = 'data-qtip="'+value.displaySummary + ret + '"';
			return ret;
		} else return "";
		},
		flex : 1
	} ],

	store : Ext.create('ASPIREdb.store.PhenotypeStore'),

	initComponent : function() {
		this.callParent();

		this.getDockedComponent('phenotypeGridToolbar').getComponent('analyzeButton').on('click', this.getPhenotypeEnrichment, this);
		
		var ref = this;
		
		ASPIREdb.EVENT_BUS.on('filter_submit', function(){
			
			ref.setLoading(true);
			ref.getStore().removeAll();
			
		});

		ASPIREdb.EVENT_BUS.on('subjects_loaded', function(subjectIds) {

			ref.currentSubjectIds = subjectIds;

			ProjectService.numSubjects(ASPIREdb.ActiveProjectSettings.getActiveProjectIds(), {
				callback : function(numSubjects) {

					if (subjectIds.length > 0) {

						if (subjectIds.length < numSubjects - 1) {
							ref.getDockedComponent('phenotypeGridToolbar').getComponent('analyzeButton').enable();
						} else {
							ref.getDockedComponent('phenotypeGridToolbar').getComponent('analyzeButton').disable();
						}

					}

				}
			});

			SubjectService.getPhenotypeSummaries(subjectIds, ASPIREdb.ActiveProjectSettings.getActiveProjectIds(), {
				callback : function(vos) {//vos is a list of phenotypeSummaryValueobjects (converted to a javascript Array)

					var data = [];
					ref.phenotypeSummaryValueObjects=[];
					for ( var i = 0; i < vos.length ; i++) {
						var phenSummary = vos[i];
						
						ref.phenotypeSummaryValueObjects[i] = phenSummary;
						
						// [ phenSummary.name, phenSummary.selectedPhenotype, subjectVal]
						// TODO find a more elegant way of doing this ...
						var row = [ phenSummary, phenSummary, phenSummary,phenSummary ];
						data.push(row);
					}
					
					ref.store.loadData(data);
					ref.updatePhenotypeSummaryCanvasesAllSubjects();
					ref.setLoading(false);
					
				}
			});
		

		});		
		
		var saveButton = ref.getDockedComponent('phenotypeGridToolbar').getComponent('saveButton');
		
		saveButton.on('click', function(){
			ASPIREdb.TextDataDownloadWindow.show();
			ASPIREdb.TextDataDownloadWindow.setLoading(true);
			
			SubjectService.getPhenotypeTextDownloadBySubjectIds(ref.currentSubjectIds,ref.saveButtonHandler);
		});
		
		ASPIREdb.EVENT_BUS.on('subject_selected', this.subjectSelectHandler, this);
		
		

	},
	
	
	/**
	 * Loads selected Subject's phenotypes
	 * 
	 * @param subjectId
	 */
	subjectSelectHandler : function(subjectIds) {//todo
		
		if (!subjectIds){
			var col = this.columns[this.SELECTED_VALUES_COL_IDX];
			
			col.setText("");
			col.setVisible(false);
			
			
			return;
		}
		
		var activeProjectId = ASPIREdb.ActiveProjectSettings.getActiveProjectIds()[0];
		
		var ref = this;
		console.log("on subject select handler");
		
	
		if (subjectIds.length ==1){
		  SubjectService.getSubject(activeProjectId, subjectIds[0], { 
			callback : function(svo) {
				if ( svo != null ) {
					ref.setLoading(true);
					
					PhenotypeService.getPhenotypes( svo.id, {
						callback : function(voMap) {//these vos a
						
							ref.columns[2].setVisible(false);
							
							var col = ref.columns[ref.SELECTED_VALUES_COL_IDX];
							col.setText(svo.patientId);
							col.setVisible(true);
							
							for ( var i = 0 ; i < ref.phenotypeSummaryValueObjects.length; i++ ) {
								var phenSummary = ref.phenotypeSummaryValueObjects[i];
								var subjectPhenotype = voMap[phenSummary.name];
								phenSummary.selectedPhenotype= subjectPhenotype;
							}
							
							ref.getView().refresh(true);
							ref.updatePhenotypeSummaryCanvasesAllSubjects();
							ref.setLoading(false);
						}
					});
				}
			}
		  });
		}
		else{
			
			SubjectService.getPhenotypeSummaryValueObjects(subjectIds, ASPIREdb.ActiveProjectSettings.getActiveProjectIds(), {
				callback : function(voMap) {//voMap is a <String, PhenotypeSummaryValueObject>Map
					
					if ( voMap != null ) {
						ref.setLoading(true);
					
						//validate that single case column is hidden
						ref.columns[1].setVisible(false);
					
						var col = ref.columns[2];
						col.setVisible(true);	
					
						var data = [];
						
						for (var i = 0; i < ref.phenotypeSummaryValueObjects.length; i++){
							var phenSummary = ref.phenotypeSummaryValueObjects[i];
							var phenoSummaryValueObject = voMap[phenSummary.name];
							//we are attaching a new property to phenSummary here, calling it phenoSummaryMapSelectedSubjects
							
							if (phenoSummaryValueObject){
								phenSummary.phenoSummaryMapSelectedSubjects= phenoSummaryValueObject.phenoSummaryMap;
								phenSummary.displaySummarySelectedSubjects= phenoSummaryValueObject.displaySummary;
							} else{//if phenoSummaryValueObject is null or undefined
								console.log("null or undefined phenoSummaryValueObject: "+ phenSummary.name);
							}
												
						}
						ref.getView().refresh(true);
						ref.updatePhenotypeSummaryCanvasesAllSubjects();
						ref.updatePhenotypeSummaryCanvasesSelectedSubjects();
						ref.setLoading(false);
					}
					
				}
			});
		
		}
		
	},
	
	updatePhenotypeSummaryCanvasesSelectedSubjects : function (){
		
		for (var i = 0 ; i < this.phenotypeSummaryValueObjects.length; i++){
			var phenSummary = this.phenotypeSummaryValueObjects[i];
			
			//phenSummary.phenoSummaryMapSelectedSubjects is the new parameter we added in the javascript
			var phenMap=phenSummary.phenoSummaryMapSelectedSubjects;
						
			var canvas = document.getElementById("multi"+phenSummary.name.replace(/ /g,''));
			
			this.drawCanvas(canvas, phenSummary, phenMap);
			
								
		}
		
		
		
	},
	
	
	updatePhenotypeSummaryCanvasesAllSubjects : function (){
				
		for (var i = 0 ; i < this.phenotypeSummaryValueObjects.length; i++){
			var phenSummary = this.phenotypeSummaryValueObjects[i];
			
			//phenSummary.phenoSummaryMapSelectedSubjects is the new parameter we added in the javascript
			var phenMap=phenSummary.phenoSummaryMap;
						
			var canvas = document.getElementById("all"+phenSummary.name.replace(/ /g,''));
			
			this.drawCanvas(canvas, phenSummary, phenMap);
			
								
		}
		
		
		
		
	},
	
	
	drawCanvas : function(canvas,  phenSummary, phenMap){
		var keyArray =phenSummary.phenoSet;
		
		var total=0;
		for (var j=0; j<keyArray.length;j++){
			total = total+phenMap[keyArray[j]];
		}
		
		var ctx = canvas.getContext("2d");
		ctx.font = "bold 8px sans-serif";
		ctx.textAlign = "center";
		
		
		var xValue=0;
		var yValue= 0;
		var colorIndex=2;
		var width=50;
		var height=50;
		var colors = ["red", "green", "black", "purple","blue", "yellow","orange", "grey"];
		var displayVal = '';
		
								
		for (var k=0;k<keyArray.length;k++){
			var fillTextWidth=k * width / keyArray.length + (width / keyArray.length) / 2;
			var fillTextHeight=height +5;
							
			if (phenSummary.valueType == "HPONTOLOGY") {
				
				if (keyArray[k] =="Present"){
					ctx.fillStyle = colors[0];
					ctx.fillRect(xValue,yValue,10,(phenMap["Present"]*height)/total);
					xValue=xValue+10;
					displayVal =displayVal+"Present("+phenMap["Present"]+")";				
																
				}
				else if (keyArray[k]=="Absent"){
					
					ctx.fillStyle =colors[1];
					ctx.fillRect(xValue,yValue,10,(phenMap["Absent"]*height)/total);
					xValue=xValue+10;
					displayVal =displayVal+"Absent("+phenMap["Absent"]+")";
									
				}
				else {
								
					ctx.fillStyle =colors[colorIndex];
					ctx.fillRect(xValue,yValue,10,(phenMap[keyArray[k]]*height)/total);
					colorIndex++;
					xValue=xValue+10;
					displayVal =displayVal+keyArray[k]+"("+phenMap[keyArray[k]]+")";
									
					}
			}
			
			else {
				
				ctx.fillStyle =colors[colorIndex];
				ctx.fillRect(xValue,yValue,10,(phenMap[keyArray[k]]*height)/total);
				colorIndex++;
				xValue=xValue+10;
				displayVal =displayVal+keyArray[k]+"("+phenMap[keyArray[k]]+")";
				
			};
		}
		
	
		
		
		
	},
	
	
	saveButtonHandler : function(text) {
		ASPIREdb.TextDataDownloadWindow.setLoading(false);
		ASPIREdb.TextDataDownloadWindow.showPhenotypesDownload(text);		
	},
	
	getSubjectValue : function(phenSummary) {

		var valueToSubjectSet = phenSummary.dbValueToSubjectSet;

		var subjectValue = '';

		var keyArray = [];
		
		//if this is a special(large) project, this will not be populated
		if (!valueToSubjectSet.Unknown){
			return subjectValue;
		}
		
		for ( var key in valueToSubjectSet) {
			if(key !== "Unknown"){
				keyArray.push(key);				
			}
						
		}
		
		keyArray.sort();
		//ensure "Unknown is at the end
		keyArray.push("Unknown");
		
		for ( var i = 0; i < keyArray.length ; i ++) {
			
			var key = keyArray[i];

			if (phenSummary.valueType == "HPONTOLOGY") {
				if (key == this.DB_VAL_HPO_PRESENT) {
					subjectValue = subjectValue + ' Present(' + valueToSubjectSet[key].length + ')';
					subjectValue = "<span " + this.STYLE_HPO_PRESENT + ">" + subjectValue + "</span>";
				}else if (key == this.DB_VAL_HPO_ABSENT) {
					subjectValue = subjectValue + ' Absent(' + valueToSubjectSet[key].length + ')';
					subjectValue = "<span " + this.STYLE_HPO_ABSENT + ">" + subjectValue + "</span>";
				} else {
					subjectValue = subjectValue + ' ' + key + ' (' + valueToSubjectSet[key].length + ')';
					subjectValue = "<span " + this.STYLE_DEFAULT + ">" + subjectValue + "</span>";
				}
			} else {
				subjectValue = subjectValue + ' ' + key + ' (' + valueToSubjectSet[key].length + ')';
				subjectValue = "<span " + this.STYLE_DEFAULT + ">" + subjectValue + "</span>";
			}
		}

		return subjectValue;

	},

	getPhenotypeEnrichment : function() {
		
		ASPIREdb.view.PhenotypeEnrichmentWindow.clearGrid();		
		ASPIREdb.view.PhenotypeEnrichmentWindow.show();
		ASPIREdb.view.PhenotypeEnrichmentWindow.setLoading(true);

		PhenotypeService.getPhenotypeEnrichmentValueObjects(ASPIREdb.ActiveProjectSettings.getActiveProjectIds(), this.currentSubjectIds, {
			callback : function(vos) {
				ASPIREdb.view.PhenotypeEnrichmentWindow.populateGrid(vos);
				ASPIREdb.view.PhenotypeEnrichmentWindow.setLoading(false);
			}
		});

	},
	
	viewNeurocartaGenes : function(value, name){
		ASPIREdb.view.NeurocartaGeneWindow.initGridAndShow(value,name);
	}

});
