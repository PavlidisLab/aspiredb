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

Ext.require( [ 'Ext.window.*', 'Ext.layout.container.Border', 'ASPIREdb.view.CreateLabelWindow' ] );

Ext.define( 'ASPIREdb.view.PhenotypeContigencyTableWindow', {
   extend : 'Ext.Window',
   alias : 'widget.phenotypeContigencyTableWindow',
   singleton : true,
   title : 'Subject Labels',
   closable : true,
   closeAction : 'hide',
   width : 900,
   height : 450,
   layout : 'fit',
   bodyStyle : 'padding: 5px;',

   config : {
      visibleLabels : {},
      selSubjects : []
   },
   items : [ {
      region : 'center',
      xtype : 'container',
      itemId : 'tableContainer',
      overflowY : 'auto',
      layout : {
         type : 'vbox'
      },
      items : [

      ],

   } ],

   initComponent : function() {

      this.callParent();
      // load existing subject labels
      this.visibleLabels = this.createVisibleLabels();

   },

   initGridAndShow : function(psvos, selPhenotypes) {

      var tableContainer = this.down( '#tableContainer' );
      tableContainer.removeAll( true );

      // if only one phenotype is selected
      if ( selPhenotypes.length == 1 ) {
         var data = [];

         var columnNames = [];
         var phenSummary = selPhenotypes[0].data.selectedPhenotype;
         var gridPanelTitle = phenSummary.name;
         for ( var labelName in phenSummary.phenoSummaryMap) {
            var subjects = [];
            if ( labelName == "Present" || labelName == "Y" )
               subjects = phenSummary.subjects[1];
            else if ( labelName == "Absent" || labelName == "N" )
               subjects = phenSummary.subjects[0];
            else
               subjects = phenSummary.subjects[labelName];
            // rowName, cell Value
            var row = [ labelName, subjects ];
            data.push( row );
         }
         this.createGridPanel( data, columnNames, gridPanelTitle );

      } else {
         for (var i = 0; i < selPhenotypes.length; i++) {
            for (var j = i + 1; j < selPhenotypes.length; j++) {
               var phenSummary1 = selPhenotypes[i].data.selectedPhenotype;
               var phenSummary2 = selPhenotypes[j].data.selectedPhenotype;
               var gridPanelTitle = phenSummary1.name + " vs " + phenSummary2.name;
               console.log( 'grid panel names: ' + gridPanelTitle );
               var rowData = [];
               var colData = [];
               var columnNames = [];
               var rowNames = [];
               var data = [];

               for ( var rowlabelName in phenSummary1.phenoSummaryMap) {
                  var subjects = [];
                  if ( rowlabelName == "Present" || rowlabelName == "Y" )
                     subjects = phenSummary1.subjects[1];
                  else if ( rowlabelName == "Absent" || rowlabelName == "N" )
                     subjects = phenSummary1.subjects[0];
                  else
                     subjects = phenSummary1.subjects[rowlabelName];
                  rowData.push( [ rowlabelName, subjects ] );
               }

               for ( var columnlabelName in phenSummary2.phenoSummaryMap) {
                  var subjects = [];
                  if ( columnlabelName == "Present" || columnlabelName == "Y" )
                     subjects = phenSummary2.subjects[1];
                  else if ( columnlabelName == "Absent" || columnlabelName == "N" )
                     subjects = phenSummary2.subjects[0];
                  else
                     subjects = phenSummary2.subjects[columnlabelName];
                  colData.push( [ columnlabelName, subjects ] );
                  columnNames.push( columnlabelName );
               }

               // find the resultant row
               for ( var rowName in rowData) {
                  if ( rowName != 'transpose' ) {

                     var rowSubjects = rowData[rowName];
                     if ( rowNames.indexOf( rowSubjects[0] == -1 ) ) {
                        console.log( 'row data :  ' + rowName + ' value is  : ' + rowSubjects[0] );
                        var row = [];
                        row.push( rowSubjects[0] );
                        rowNames.push( rowSubjects[0] );

                        for ( var colName in colData) {
                           if ( colName != 'transpose' ) {
                              var resultSubjects = [];
                              console.log( 'column names : ' + colName );
                              var colSubjects = colData[colName];
                              for (var k = 0; k < rowSubjects[1].length; k++) {
                                 if ( colSubjects[1].indexOf( rowSubjects[1][k] ) != -1 ) {
                                    resultSubjects.push( rowSubjects[1][k] );
                                 }
                              }
                              row.push( resultSubjects );
                           }
                        }
                     }
                     data.push( row );
                  }
               }
               this.createGridPanel( data, columnNames, gridPanelTitle );
            }
         }
      }
      this.show();

   },

   createGridPanel : function(data, columnNames, gridPanelTitle) {

      var ref = this;
      var fields = [];
      fields.push( 'rowName' );

      if ( columnNames.length > 0 ) {
         for (var i = 0; i < columnNames.length; i++) {
            fields.push( columnNames[i] );
         }
      } else
         fields.push( 'subjects' );
      // create store
      var suggestContigencyTableStore = Ext.create( 'Ext.data.ArrayStore', {
         fields : fields,
         data : data,
         autoLoad : true,
         autoSync : true,
         storeId : gridPanelTitle,

      } );

      // columns
      var columns = [];
      columns.push( {
         header : '',
         dataIndex : 'rowName',
         flex : 1
      } );

      if ( columnNames.length > 1 ) {
         for (var i = 0; i < columnNames.length; i++) {
            columns.push( {
               header : columnNames[i],
               dataIndex : columnNames[i],
               flex : 1,
               renderer : function(value) {
                  return value.length;
               }
            } );
         }
      } else {
         columns.push( {
            header : 'Subjects',
            dataIndex : 'subjects',
            flex : 1,
            renderer : function(value) {
               return value.length;
            }
         } );
      }
      // create grid
      var grid = Ext.create( 'Ext.grid.Panel', {
         title : gridPanelTitle,
         id : gridPanelTitle,
         closable : true,
         border : true,
         store : suggestContigencyTableStore,
         columns : columns,
         autoRender : true,
         width : 850,
         columnLines : true,
         listeners : {
            cellclick : function(view, td, cellIndex, record, tr, rowIndex, e, eOpts) {
               ref.makeLabelHandler( record.raw[cellIndex] );
            }
         },
         selModel : Ext.create( 'Ext.selection.CellModel', {
            mode : 'MULTI',
            listeners : {
               click : {
                  element : 'el', // bind to the underlying el property on the panel
                  fn : function() {
                     console.log( 'click el' );

                  }
               },
            }
         } ),

      } );

      var tableContainer = this.down( '#tableContainer' );

      tableContainer.add( grid );
      tableContainer.doLayout();

   },

   /**
    * Reusing the code in subject grid Assigns a Label
    * 
    * @param :
    *           event
    */
   makeLabelHandler : function(subjectIds) {

      var me = this;
      var projectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds();
      SubjectService.getSubjects( projectIds[0], subjectIds, {
         callback : function(selectedSubjectValueObjects) {
            me.selSubjects = selectedSubjectValueObjects;
         }
      } );

      Ext.define( 'ASPIREdb.view.CreateLabelWindowSubject', {
         isSubjectLabel : true,
         extend : 'ASPIREdb.view.CreateLabelWindow',

         // override
         onOkButtonClick : function() {

            var labelCombo = this.down( "#labelCombo" );
            var vo = this.getLabel();
            if ( vo == null ) {
               return;
            }
            var labelIndex = labelCombo.getStore().findExact( 'display', vo.name );
            if ( labelIndex != -1 ) {
               // activate confirmation window
               Ext.MessageBox.confirm( 'Label already exist', 'Label already exist. Add into it ?', function(btn) {
                  if ( btn === 'yes' ) {
                     me.addLabelHandler( vo, subjectIds );
                     this.hide();
                     // ASPIREdb.EVENT_BUS.fireEvent('subject_label_created');
                  }

               }, this );

            } else {
               me.addLabelHandler( vo, subjectIds );
               this.hide();
               // ASPIREdb.EVENT_BUS.fireEvent('subject_label_created');
            }

         }
      } );

      var labelWindow = new ASPIREdb.view.CreateLabelWindowSubject();
      labelWindow.show();

   },

   /**
    * Reusing the code in subject grid Add the label to the store
    * 
    * @param: label value object, selected subject Ids
    */
   addLabelHandler : function(vo, selSubjectIds) {

      var me = this;

      // store in database
      SubjectService.addLabel( selSubjectIds, vo, {
         callback : function(addedLabel) {

            addedLabel.isShown = true;
            LabelService.updateLabel( addedLabel );

            var existingLab = me.visibleLabels[addedLabel.id];
            if ( existingLab == undefined ) {
               me.visibleLabels[addedLabel.id] = addedLabel;
            } else {
               existingLab.isShown = true;
            }
            for (var i = 0; i < me.selSubjects.length; i++) {
               me.selSubjects[i].labels.push( addedLabel );
            }

            ASPIREdb.EVENT_BUS.fireEvent( 'subject_label_changed' );

         }
      } );

   },

   /**
    * Reusing the code in subject grid Load subject labels created by the user
    * 
    * @return visibleLabels
    */
   createVisibleLabels : function() {
      var visibleLabels = [];
      var suggestionContext = new SuggestionContext();
      suggestionContext.activeProjectIds = ASPIREdb.ActiveProjectSettings.getActiveProjectIds();

      // load all labels created by this user
      SubjectService.suggestLabels( suggestionContext, {
         callback : function(labels) {
            for ( var idx in labels) {
               var label = labels[idx];
               visibleLabels[label.id] = label;
            }
         }
      } );

      return visibleLabels;
   },

} );