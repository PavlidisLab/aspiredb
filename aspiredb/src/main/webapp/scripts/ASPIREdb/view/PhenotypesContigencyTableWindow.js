Ext.require( [ 'Ext.window.*', 'Ext.layout.container.Border' ] );

Ext.define( 'ASPIREdb.view.PhenotypesContigencyTableWindow', {
   extend : 'Ext.Window',
   alias : 'widget.phenotypescontigencytablewindow',
   singleton : true,
   title : 'Contingency Table',
   closable : true,
   closeAction : 'hide',
   width : 900,
   height : 450,
   layout : 'fit',
   bodyStyle : 'padding: 5px;',
   config : {
      selectedSubjectIds : [],
      selSubjects : [],
      visibleLabels : [],
      gridPanelName : '',

   },

   initComponent : function() {

      this.callParent();

   },

   initGridAndShow : function(psvos, selPhenotypes) {
      if ( selPhenotypes.length > 3 ) {
         Ext.Msg.alert( 'Warning', 'User is allowed to select maximum 3 phenotypes to view thw table. Reselect' );
      } else {
         // if only one phenotype is selected
         if ( selPhenotypes.length == 1 ) {
            var data = [];

            var phenSummary = selPhenotypes[0].data.selectedPhenotype;
            var phenotypeName = phenSummary.name;
            var columnNames = [ phenotypeName, 'Subject Count' ];

            for ( var labelName in phenSummary.phenoSummaryMap) {
               var subjects = [];
               if ( labelName != "Unknown" ) {
                  if ( labelName == "Present" )
                     subjects = phenSummary.subjects[1];
                  else if ( labelName == "Absent" )
                     subjects = phenSummary.subjects[0];
                  else if ( labelName == "Y" )
                     subjects = phenSummary.subjects['Y'];
                  else if ( labelName == "N" )
                     subjects = phenSummary.subjects['N'];
                  else
                     subjects = phenSummary.subjects[labelName];
                  // rowName, cell Value
                  var row = [ labelName, subjects ];
                  data.push( row );
               }
            }
            this.createGridPanel( data, columnNames, phenotypeName );

         } else {
            // multiple cases
            var selectedPhenotypeData = [];
            var phenotypeName = '';
            var columnNames = [];
            for (var i = 0; i < selPhenotypes.length; i++) {
               var phenSummary = selPhenotypes[i].data.selectedPhenotype;

               phenotypeName = phenotypeName + phenSummary.name;

               console.log( 'grid panel names: ' + phenotypeName );
               var pheneData = [];

               columnNames.push( phenSummary.name );

               for ( var rowlabelName in phenSummary.phenoSummaryMap) {
                  var subjects = [];
                  if ( rowlabelName != "Unknown" ) {
                     if ( rowlabelName == "Present" )
                        subjects = phenSummary.subjects[1];
                     else if ( rowlabelName == "Absent" )
                        subjects = phenSummary.subjects[0];
                     else if ( rowlabelName == "Y" )
                        subjects = phenSummary.subjects['Y'];
                     else if ( rowlabelName == "N" )
                        subjects = phenSummary.subjects['N'];
                     else
                        subjects = phenSummary.subjects[rowlabelName];
                     pheneData.push( [ rowlabelName, subjects ] );
                  }
               }
               selectedPhenotypeData.push( pheneData );
            }

            var resultantData = selectedPhenotypeData[0];
            for (var i = 1; i < selectedPhenotypeData.length; i++) {
               var newResultantRow = [];
               for (var j = 0; j < resultantData.length; j++) {
                  for (var m = 0; m < selectedPhenotypeData[i].length; m++) {
                     var columnValues = resultantData[j];
                     var newResultantColumn = [];
                     // push the phenotype values for the grid column
                     for (var k = 0; k < columnValues.length - 1; k++) {
                        if ( columnValues[k] != 'transpose' ) {
                           newResultantColumn.push( columnValues[k] );
                        }
                     }
                     // newResultantColumn.push( selectedPhenotypeData[i][0][m] );
                     var secondColumnValues = selectedPhenotypeData[i][m];
                     for (var n = 0; n < secondColumnValues.length - 1; n++) {
                        if ( secondColumnValues[k] != 'transpose' ) {
                           newResultantColumn.push( secondColumnValues[n] );
                        }
                     }

                     // finding the common subject ids for selected 2 phenotypes
                     var firstColumnSubjects = columnValues[columnValues.length - 1];
                     var secondColumnSubjects = secondColumnValues[secondColumnValues.length - 1];
                     var resultSubjects = [];
                     for (var k = 0; k < firstColumnSubjects.length; k++) {
                        if ( secondColumnSubjects.indexOf( firstColumnSubjects[k] ) != -1 ) {
                           resultSubjects.push( firstColumnSubjects[k] );
                        }
                     }

                     newResultantColumn.push( resultSubjects );
                     newResultantRow.push( newResultantColumn );

                  }

               }
               // recursive resultant data
               resultantData = newResultantRow;

            }
            columnNames.push( 'Subject Count' );
            // columnNames.push( '' );
            this.createGridPanel( resultantData, columnNames, phenotypeName );

         }

         this.show();
      }

   },

   createGridPanel : function(data, columnNames, phenotypeName) {

      var ref = this;
      ref.removeAll();
      ref.gridPanelName = phenotypeName;
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
            if ( columnNames[i] == 'Subject Count' ) {
               columns.push( {
                  header : columnNames[i],
                  dataIndex : columnNames[i],
                  width : 100,
                  tooltip: 'Subject counts for multiple phenotypes',
                  tooltipType : 'title',
                  renderer : function(value, metadata, record) {
                     var src = 'scripts/ASPIREdb/resources/images/icons/tag.png';
                     var tooltip = "subject counts for multiple phenotype ";

                     image = Ext.String.format(  "<i class='fa fa-tags'></i> ", src, tooltip );

                     var ret = value.length + '&nbsp&nbsp' + image;
                     metadata.tdAttr = 'data-qtip="Click to assign a subject label" data-qwidth="200"';
                     if ( value.length == 0 )
                        ret = value.length + '&nbsp&nbsp';
                     return ret;
                     // return value.length
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
         id : phenotypeName,
         closable : false,
         border : true,
         store : suggestContigencyTableStore,
         columns : columns,
         autoRender : true,
         // multiSelect : false,
         width : 850,
         listeners : {
            cellclick : function(view, td, cellIndex, record, tr, rowIndex, e, eOpts) {
               var subjectIdLength = record.raw.length;
               ref.selectedSubjectIds = record.raw[subjectIdLength - 1];
               if ( ref.selectedSubjectIds.length != 0 )
                  ref.makeLabelHandler();
            }

         // }
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
   makeLabelHandler : function() {

      var me = this;

      var subjectIds = me.selectedSubjectIds;
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

                  }

               }, this );

            } else {
               me.addLabelHandler( vo, subjectIds );
               this.hide();

            }

         }
      } );

      var labelWindow = new ASPIREdb.view.CreateLabelWindowSubject();
      labelWindow.show();
      ASPIREdb.EVENT_BUS.fireEvent( 'phenotype_label_created' );

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

            // refresh the grid
            ASPIREdb.EVENT_BUS.fireEvent( 'subject_label_updated', selSubjectIds, addedLabel );

         }
      } );

   },

   /**
    * Reusing the code in subject grid to Load subject labels created by the user
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