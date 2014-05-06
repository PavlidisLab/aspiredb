Ext.require( [ 'Ext.window.*', 'Ext.layout.container.Border', 'ASPIREdb.view.phenotypeSubjectLabelGrid' ] );

Ext.define( 'ASPIREdb.view.PhenotypeSubjectLabelWindow', {
   extend : 'Ext.Window',
   alias : 'widget.phenotypesubjectlabelwindow',
   singleton : true,
   title : 'Subject Labels',
   closable : true,
   closeAction : 'hide',
   width : 900,
   height : 450,
   layout : 'fit',
   bodyStyle : 'padding: 5px;',

   /**
    * items : [ { itemId : 'phenotypeSubjectLabelGrid', xtype : 'phenotypeSubjectLabelGrid', } ],
    */

   initComponent : function() {

      this.callParent();

   },

   /**
    * initGridAndShow : function(psvos, selPhenotypes) {
    * 
    * var ref = this;
    * 
    * var grid = ref.getComponent( 'phenotypeSubjectLabelGrid' ); grid.populateGrid( psvos, selPhenotypes ); ref.show(); },
    */

   initGridAndShow : function(psvos, selPhenotypes) {
      if ( selPhenotypes.length > 3 ) {
         Ext.Msg.alert( 'User is allowed to select maximum 3 phenotypes to view thw table. Reselect' );
      } else {
         // if only one phenotype is selected
         if ( selPhenotypes.length == 1 ) {
            var data = [];

            var phenSummary = selPhenotypes[0].data.selectedPhenotype;
            var phenotypeName = phenSummary.name;
            var columnNames = [ phenotypeName, 'Label' ];

            for ( var labelName in phenSummary.phenoSummaryMap) {
               var subjects = [];
               if ( labelName != "Unknown" ) {
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
            }
            this.createGridPanel( data, columnNames, phenotypeName );

         } else if ( selPhenotypes.length == 2 ) {
            for (var i = 0; i < selPhenotypes.length; i++) {
               var rowData = [];
               var phenSummary1 = selPhenotypes[i].data.selectedPhenotype;

               for ( var rowlabelName in phenSummary1.phenoSummaryMap) {
                  var subjects = [];
                  if ( rowlabelName != "Unknown" ) {
                     if ( rowlabelName == "Present" || rowlabelName == "Y" )
                        subjects = phenSummary1.subjects[1];
                     else if ( rowlabelName == "Absent" || rowlabelName == "N" )
                        subjects = phenSummary1.subjects[0];
                     else
                        subjects = phenSummary1.subjects[rowlabelName];
                     rowData.push( [ rowlabelName, subjects ] );
                  }

               }
               for (var j = i + 1; j < selPhenotypes.length; j++) {

                  var phenSummary2 = selPhenotypes[j].data.selectedPhenotype;
                  var phenotypeName = phenSummary1.name + " vs " + phenSummary2.name;
                  var columnNames = [ phenSummary1.name, phenSummary2.name, 'Label' ];
                  
                  var colData = [];
                  var rowNames = [];
                  var data = [];

                  for ( var columnlabelName in phenSummary2.phenoSummaryMap) {
                     var subjects = [];
                     if ( columnlabelName != "Unknown" ) {
                        if ( columnlabelName == "Present" || columnlabelName == "Y" )
                           subjects = phenSummary2.subjects[1];
                        else if ( columnlabelName == "Absent" || columnlabelName == "N" )
                           subjects = phenSummary2.subjects[0];
                        else
                           subjects = phenSummary2.subjects[columnlabelName];
                        colData.push( [ columnlabelName, subjects ] );

                     }
                  }

                  // find the resultant row
                  for ( var rowName in rowData) {
                     if ( rowName != 'transpose' ) {

                        var rowSubjects = rowData[rowName];
                        if ( rowNames.indexOf( rowSubjects[0] == -1 ) ) {
                           console.log( 'row data :  ' + rowName + ' value is  : ' + rowSubjects[0] );

                           rowNames.push( rowSubjects[0] );

                           for ( var colName in colData) {
                              if ( colName != 'transpose' ) {
                                 var row = [];
                                 var resultSubjects = [];
                                 console.log( 'column names : ' + colName );
                                 var colSubjects = colData[colName];
                                 row.push( rowSubjects[0] );
                                 row.push( colSubjects[0] );
                                 for (var k = 0; k < rowSubjects[1].length; k++) {
                                    if ( colSubjects[1].indexOf( rowSubjects[1][k] ) != -1 ) {
                                       resultSubjects.push( rowSubjects[1][k] );
                                    }
                                 }
                                 row.push( resultSubjects );
                                 data.push( row );
                              }
                           }
                        }
                       
                     }
                  }
                  this.createGridPanel( data, columnNames, phenotypeName );
               }
            }

         } else if ( selPhenotypes.length == 3 ) {

            for (var i = 0; i < selPhenotypes.length; i++) {
               var data = [];
               var rowData = [];
               var phenotypeName ='';
               var columnNames =[];
               
               var phenSummary1 = selPhenotypes[i].data.selectedPhenotype;

               for ( var rowlabelName in phenSummary1.phenoSummaryMap) {
                  var subjects = [];
                  if ( rowlabelName != "Unknown" ) {
                     if ( rowlabelName == "Present" || rowlabelName == "Y" )
                        subjects = phenSummary1.subjects[1];
                     else if ( rowlabelName == "Absent" || rowlabelName == "N" )
                        subjects = phenSummary1.subjects[0];
                     else
                        subjects = phenSummary1.subjects[rowlabelName];
                     rowData.push( [ rowlabelName, subjects ] );
                  }

               }
               
               for (var j = i + 1; j < selPhenotypes.length; j++) {

                  var phenSummary2 = selPhenotypes[j].data.selectedPhenotype;
                  
                  
                  if (data.length>0){
                     phenotypeName = phenotypeName+" vs "+phenSummary2.name;
                     columnNames.push(phenSummary2.name);
                     
                     var colData = [];
                   //  var rowNames = [];
                     var newData =[];

                     for ( var columnlabelName in phenSummary2.phenoSummaryMap) {
                        var subjects = [];
                        if ( columnlabelName != "Unknown" ) {
                           if ( columnlabelName == "Present" || columnlabelName == "Y" )
                              subjects = phenSummary2.subjects[1];
                           else if ( columnlabelName == "Absent" || columnlabelName == "N" )
                              subjects = phenSummary2.subjects[0];
                           else
                              subjects = phenSummary2.subjects[columnlabelName];
                           colData.push( [ columnlabelName, subjects ] );

                        }
                     }

                     // find the resultant row
                     for ( var dataRow in data) {
                           var rowData =data[dataRow];
                           var subjectindex=rowData.length -1;
                           var rowSubjects = rowData[subjectindex];
                           if (rowSubjects!=null){
                           // if ( rowNames.indexOf( rowSubjects[0] == -1 ) ) {
                              //   console.log( 'row data :  ' + dataRow + ' value is  : ' + rowSubjects[0] );

                               //  rowNames.push( rowSubjects[0] );

                                 for ( var colName in colData) {
                                    if ( colName != 'transpose' ) {
                                       var row = [];
                                       var resultSubjects = [];
                                       console.log( 'column names : ' + colName );
                                       var colSubjects = colData[colName];
                                       for (var l=0;l< rowData.length-1;l++){
                                          row.push( rowData[l] );
                                       }
                                       row.push( colSubjects[0] );
                                       for (var k = 0; k < rowSubjects.length; k++) {
                                          if ( colSubjects[1].indexOf( rowSubjects[k] ) != -1 ) {
                                             resultSubjects.push( rowSubjects[k] );
                                          }
                                       }
                                       row.push( resultSubjects );
                                       newData.push( row );
                                       
                                    }
                                 }
                                // data =newData; 
                              //}
                           }
                           else{
                              //empty subjects
                              for ( var colName in colData) {
                                 if ( colName != 'transpose' ) {
                                    var row = [];
                                    var resultSubjects = [];
                                    console.log( 'column names : ' + colName );
                                    var colSubjects = colData[colName];
                                    for (var l=0;l< rowData.length-1;l++){
                                       row.push( rowData[l] );
                                    }
                                    row.push( colSubjects[0] );
                                    
                                    row.push( resultSubjects );
                                    newData.push( row );
                                    
                                 }
                              }
                           }
                           
                          

                     }
                     data =newData;
                      
                  }
                  else{
                     phenotypeName = phenSummary1.name + " vs " + phenSummary2.name;
                     columnNames = [ phenSummary1.name, phenSummary2.name ];
                     
                     var colData = [];
                     var rowNames = [];

                     for ( var columnlabelName in phenSummary2.phenoSummaryMap) {
                        var subjects = [];
                        if ( columnlabelName != "Unknown" ) {
                           if ( columnlabelName == "Present" || columnlabelName == "Y" )
                              subjects = phenSummary2.subjects[1];
                           else if ( columnlabelName == "Absent" || columnlabelName == "N" )
                              subjects = phenSummary2.subjects[0];
                           else
                              subjects = phenSummary2.subjects[columnlabelName];
                           colData.push( [ columnlabelName, subjects ] );

                        }
                     }

                     // find the resultant row
                     for ( var rowName in rowData) {
                        if ( rowName != 'transpose' ) {

                           var rowSubjects = rowData[rowName];
                           if ( rowNames.indexOf( rowSubjects[0] == -1 ) ) {
                              console.log( 'row data :  ' + rowName + ' value is  : ' + rowSubjects[0] );

                              rowNames.push( rowSubjects[0] );

                              for ( var colName in colData) {
                                 if ( colName != 'transpose' ) {
                                    var row = [];
                                    var resultSubjects = [];
                                    console.log( 'column names : ' + colName );
                                    var colSubjects = colData[colName];
                                    row.push( rowSubjects[0] );
                                    row.push( colSubjects[0] );
                                    for (var k = 0; k < rowSubjects[1].length; k++) {
                                       if ( colSubjects[1].indexOf( rowSubjects[1][k] ) != -1 ) {
                                          resultSubjects.push( rowSubjects[1][k] );
                                       }
                                    }
                                    row.push( resultSubjects );
                                    data.push( row );
                                 }
                              }
                           }
                           
                        }
                     }
                  }
                  
                  
               }
               columnNames.push('Label');
               this.createGridPanel( data, columnNames, phenotypeName );
            }

            /**
             * // multiple cases var phenSummaries = []; var phenotypeName = ''; var columnNames = []; for (var i = 0; i <
             * selPhenotypes.length; i++) { var phenSummary = selPhenotypes[i].data.selectedPhenotype;
             * 
             * phenotypeName = phenotypeName+ phenSummary.name;
             * 
             * console.log( 'grid panel names: ' + phenotypeName ); var pheneData = [];
             * 
             * columnNames.push( phenSummary.name );
             * 
             * for ( var rowlabelName in phenSummary.phenoSummaryMap) { var subjects = []; if ( rowlabelName !=
             * "Unknown" ) { if ( rowlabelName == "Present" || rowlabelName == "Y" ) subjects = phenSummary.subjects[1];
             * else if ( rowlabelName == "Absent" || rowlabelName == "N" ) subjects = phenSummary.subjects[0]; else
             * subjects = phenSummary.subjects[rowlabelName]; pheneData.push( [ rowlabelName, subjects ] ); }
             *  } phenSummaries.push(pheneData); }
             * 
             * for (var i = 0; i < phenSummaries.length; i++) { for (var k = i+1; k < phenSummaries.length; k++) { var
             * rowNames=[]; var data=[]; // find the resultant row for ( var rowName in phenSummaries[i]) { if ( rowName !=
             * 'transpose' ) {
             * 
             * var rowSubjects = phenSummaries[i][rowName]; if ( rowNames.indexOf( rowSubjects[0] == -1 ) ) {
             * console.log( 'row data : ' + rowName + ' value is : ' + rowSubjects[0] );
             * 
             * rowNames.push( rowSubjects[0] );
             * 
             * for ( var colName in phenSummaries[k]) { if ( colName != 'transpose' ) { var row = []; var resultSubjects =
             * []; console.log( 'column names : ' + colName ); var colSubjects = phenSummaries[k][colName]; //row.push(
             * rowSubjects[0] ); //row.push( colSubjects[0] ); for (var k = 0; k < rowSubjects[1].length; k++) { if (
             * colSubjects[1].indexOf( rowSubjects[1][k] ) != -1 ) { resultSubjects.push( rowSubjects[1][k] ); } }
             * row.push( resultSubjects ); data.push( row ); } } }
             *  } }
             *  }
             *  } this.createGridPanel( data, columnNames, phenotypeName );
             */

         }

         this.show();
      }

   },

   createGridPanel : function(data, columnNames, phenotypeName) {

      var ref = this;
      var fields = [];

      if ( columnNames.length > 0 ) {
         for (var i = 0; i < columnNames.length; i++) {
            fields.push( columnNames[i] );
         }
      }
      // create store
      var suggestContigencyTableStore = Ext.create( 'Ext.data.ArrayStore', {
         fields : fields,
         data : data,
         autoLoad : true,
         autoSync : true,
         storeId : phenotypeName,

      } );

      // columns
      var columns = [];

      if ( columnNames.length > 1 ) {
         for (var i = 0; i < columnNames.length; i++) {
            if ( columnNames[i] == 'Label' ) {
               columns.push( {
                  header : columnNames[i],
                  dataIndex : columnNames[i],
                  flex : 1,
                  renderer : function(value) {
                     /**
                      * var labels =[]; var projectIds= ASPIREdb.ActiveProjectSettings.getActiveProjectIds();
                      * SubjectService.getSubjects(projectIds[0],value, { callback :
                      * function(selectedSubjectValueObjects) { for (var k=0;k<selectedSubjectValueObjects.length;k++){
                      * if (selectedSubjectValueObjects[k].labels[0].isShown){
                      * labels.push(selectedSubjectValueObjects[k].labels[0]); } } } }); console.log('label colour
                      * :'+labels[0].colour); if (labels.length >0){ var fontcolor = (parseInt( labels[0].colour, 16 ) >
                      * 0xffffff / 2) ? 'black' : 'white'; ret += "<font color=" + fontcolor + "><span
                      * style='background-color: " + labels[0].colour + "'>&nbsp&nbsp" + value.length + "&nbsp</span></font>&nbsp&nbsp&nbsp";
                      * console.log('font color :'+fontcolor); } else ret =value.length;
                      * 
                      * return ret; //value.length;
                      */
                     return value.length;
                  }
               } );
            } else {
               columns.push( {
                  header : columnNames[i],
                  dataIndex : columnNames[i],
                  flex : 1,

               } );
            }

         }
      }
      // create grid
      var grid = Ext.create( 'Ext.grid.Panel', {
         // title : phenotypeName,
         id : phenotypeName,
         closable : false,
         border : true,
         store : suggestContigencyTableStore,
         columns : columns,
         autoRender : true,
         width : 850,
         // columnLines : true,
         listeners : {
            cellclick : function(view, td, cellIndex, record, tr, rowIndex, e, eOpts) {
               ref.makeLabelHandler( record.raw[cellIndex] );

            }
         },
         selModel : Ext.create( 'Ext.selection.RowModel', {
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

      ref.add( grid );
      ref.show();
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