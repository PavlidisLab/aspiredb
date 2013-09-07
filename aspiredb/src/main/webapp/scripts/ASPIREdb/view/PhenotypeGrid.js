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

Ext.require([
    'ASPIREdb.store.PhenotypeStore',
    'ASPIREdb.ActiveProjectSettings',
    'ASPIREdb.view.PhenotypeEnrichmentWindow'
]);

//TODO js documentation
Ext.define('ASPIREdb.view.PhenotypeGrid', {
	extend : 'Ext.grid.Panel',
	alias : 'widget.phenotypeGrid',
	title : 'Phenotype',

	tbar : [ {
		xtype : 'button',
		text : 'Analyze',
		handler : function(){
			ASPIREdb.view.PhenotypeEnrichmentWindow.show();
		}
	}, {
		xtype : 'button',
		text : 'Save'
	} ],

	columns : [ {
		header : 'Name',
		dataIndex : 'name',
		flex : 1
	}, {
		header : 'Value (subject count)',
		dataIndex : 'value',
		flex : 1
	} ],

	store : Ext.create('ASPIREdb.store.PhenotypeStore'),

	initComponent : function() {
		this.callParent();

		var ref = this;

		ASPIREdb.EVENT_BUS.on('subjects_loaded', function(subjectIds){
            SubjectService.getPhenotypeSummaries(subjectIds, ASPIREdb.ActiveProjectSettings.getActiveProjectIds() , {
                callback : function(vos) {

                    var data = [];
                    for ( var key in vos) {
                        var phenSummary = vos[key];

                        var row = [ phenSummary.name,
                            ref.getSubjectValue(phenSummary) ];
                        data.push(row);
                    }

                    ref.store.loadData(data);
                }
            });
        });

	},

	// TODO this will have to change when we get around to prettying up the grid
	getSubjectValue : function(phenSummary) {

		var valueToSubjectSet = phenSummary.dbValueToSubjectSet;

		var subjectValue = '';

		for ( var key in valueToSubjectSet) {

			if (phenSummary.valueType == "HPONTOLOGY") {
				if (key == '1') {
					subjectValue = subjectValue + ' Present('
							+ valueToSubjectSet[key].length + ')';
				}
				if (key == '0') {
					subjectValue = subjectValue + ' Absent('
							+ valueToSubjectSet[key].length + ')';
				}
			} else {
				subjectValue = subjectValue + ' ' + key + ' ('
						+ valueToSubjectSet[key].length + ')';
			}
		}

		return subjectValue;

	}
});
