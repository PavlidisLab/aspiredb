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

Ext.define('ASPIREdb.TextDataDownloadWindow', {
	extend : 'Ext.Window',
	singleton : true,
	closable : true,
	closeAction : 'hide',
	width : 800,
	height : 500,
	layout : 'fit',
	title : 'Select all and right click to copy to clipboard',

	initComponent : function() {

		var ref = this;

		Ext.apply(this, {
			tbar : [ {
				itemId : 'selectAllButton',
				xtype : 'button',
				text : 'Select All',
				scope : this,
				handler : function() {
					ref.getComponent('textAreaPanel').selectText();
				}
			} ],
			items : [ {
				xtype : 'textareafield',
				itemId : 'textAreaPanel',

				anchor : '100%'
			} ],

			populateText : function(text) {
				var textArea = ref.getComponent('textAreaPanel');
				textArea.setValue(text);
				ref.show();
			}

		});

		this.callParent();

	},

	showPhenotypeEnrichmentDownload : function(pvoList) {

		var text = '';
		text = text + "Name\t";
		text = text + "uri\t";
		text = text + "In Group\t";
		text = text + "Out Group\t";
		text = text + "pValue\t";
		text = text + "Corrected pValue\t";

		text = text + "\n";
		for ( var i = 0; i < pvoList.length; i++) {

			var pvo = pvoList[i];

			text = text + pvo.name + "\t";
			text = text + pvo.uri + "\t";
			text = text + pvo.inGroupTotalString + "\t";
			text = text + pvo.outGroupTotalString + "\t";
			text = text + pvo.PValueString + "\t";
			text = text + pvo.PValueCorrectedString + "\t";
			text = text + "\n";
		}

		this.populateText(text);

	},

	showSubjectDownload : function(svoList) {

		var text = '';
		text = text + "Subject Id";

		text = text + "\n";
		for ( var i = 0; i < svoList.length; i++) {

			text = text + svoList[i].patientId;
			text = text + "\n";
		}

		this.populateText(text);

	},
	
	showVariantsDownload : function( data, columnHeaders ) {

        var text = "";
		
		for (var i = 0 ; i < columnHeaders.length ; i++){
			
			text = text + columnHeaders[i];
			text = text +  "\t" ;
			
		}		

        text = text +  "\n" ;
        for (var i = 0 ; i< data.length ; i++ ) {
        	
        	vvoArray = data[i].raw;
        	
        	//first column is subjectId so skip
        	for (var j = 1 ; j <vvoArray.length ; j++){
        		
        		if (vvoArray[j]){
        			text = text + vvoArray[j];
        		}
        		text = text +  "\t" ;
        	} 

            text = text +  "\n" ;
        }

        this.populateText(text);

    },
    
    showGenesDownload : function( data, columnHeaders ) {

        var text = "";
		
		for (var i = 0 ; i < columnHeaders.length ; i++){
			
			text = text + columnHeaders[i];
			text = text +  "\t" ;
			
		}		

        text = text +  "\n" ;
        for (var i = 0 ; i< data.length ; i++ ) {
        	
        	geneArray = data[i].raw;
        	
        	//last column is gemma link so skip
        	for (var j = 0 ; j <geneArray.length-1 ; j++){
        		
        		if (geneArray[j]){
        			text = text + geneArray[j];
        		}
        		text = text +  "\t" ;
        	} 

            text = text +  "\n" ;
        }

        this.populateText(text);

    },
    
    showPhenotypesDownload : function(text) {
    	this.populateText(text);
	},
});
