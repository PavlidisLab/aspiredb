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

Ext.require( [ 'ASPIREdb.store.PhenotypeStore', 'ASPIREdb.ActiveProjectSettings',
              'Ext.grid.column.Column',
              'ASPIREdb.view.NeurocartaGeneWindow', 'ASPIREdb.view.SubjectPhenotypeHeatmapWindow',
              'ASPIREdb.view.PhenotypesContigencyTableWindow' ] );

// TODO js documentation
Ext.define( 'ASPIREdb.view.PhenotypeGrid', {
   extend : 'Ext.grid.Panel',
   alias : 'widget.phenotypeGrid',
   title : 'Phenotype',
   id : 'phenotypeGrid',
   multiSelect : true,
   // disableSelection:true,

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
      selPhenotypes : [],

   },
   constructor : function(cfg) {
      this.initConfig( cfg );
      this.callParent( arguments );
   },

   dockedItems : [ {
      xtype : 'toolbar',
      itemId : 'phenotypeGridToolbar',
      dock : 'top',
      items : [ {
         xtype : 'button',
         text : 'Heatmap',
         disabled : 'true',
         itemId : 'heatmapButton',
         tooltip : 'View subject-phenotype heatmap',
         tooltipType : 'title',

      }, {
         xtype : 'button',
         text : 'Contingency table',
         disabled : 'true',
         itemId : 'contingencyTableButton',
         tooltip : 'View subject-phenotype labels',
         tooltipType : 'title',
      }, {
         xtype : 'tbfill',
      }, {
         itemId : 'saveButton',
         xtype : 'button',
         text : '',
         tooltip : 'Download table contents as text',
         tooltipType : 'title',
         icon : 'scripts/ASPIREdb/resources/images/icons/disk.png'

      } ]

   } ],

   bbar : [ {
      xtype : 'label',
      itemId : 'statusbar',
      html : ''
   } ], // bbar

   columns : [
              {
                 text : 'Name',
                 dataIndex : 'name',

                 renderer : function(value) {

                    var image = "";
                    if ( value.neurocartaPhenotype ) {
                       var src = 'scripts/ASPIREdb/resources/images/icons/neurocarta.png';
                       var tooltip = "View genes associated in Neurocarta";

                       var ahrefurl = '<a onclick="return Ext.getCmp(\'phenotypeGrid\').viewNeurocartaGenes(\''
                          + value.uri + '\',\'' + value.name + '\')" href=#>';

                       image = Ext.String.format( ahrefurl + "<img src='{0}' alt='{1}' > </a>", src, tooltip );
                    }
                    var ret = value.name + " " + image;
                    return ret;
                 },
                 // width : 350,
                 flex : 1
              },

              {
                 // populated dynamically when a Subject is selected
                 text : '',
                 dataIndex : 'selectedPhenotype',
                 hidden : true,
                 // width : 80,
                 renderer : function(value) {

                    var phenSummary = value.selectedPhenotype;
                    if ( phenSummary == null )
                       return ret;
                    var style = this.STYLE_DEFAULT;
                    var displayVal = phenSummary.dbValue;
                    /**
                      * Used the Color Brewer 2.0 system for coloring the chart Thanks for Cynthia Brewer, Mark Harrower
                      * and The Pennsylvania State University
                      */
                    var colors = [ "#b35806", "#31a354", "#636363", "#d8b365", "#2c7fb8", "#addd8e", "#7570b3",
                                  "#a6bddb" ];
                    var colorIdx = 5;
                    // TODO : This is not a good workaround to display the color, have to figure out a way to do
                    // this
                    // better
                    if ( phenSummary.valueType == "HPONTOLOGY" ) {
                       if ( phenSummary.dbValue == this.DB_VAL_HPO_ABSENT ) {
                          style = colors[1];
                          displayVal = "Absent";
                       } else if ( phenSummary.dbValue == this.DB_VAL_HPO_PRESENT ) {
                          style = colors[0];
                          displayVal = "Present";
                       } else if ( phenSummary.dbValue == "Unknown" ) {
                          style = colors[2];
                          displayVal = "Unknown";
                       }
                    } else if ( phenSummary.dbValue == "Unknown" ) {
                       style = colors[2];
                       displayVal = "Unknown";
                    } else if ( phenSummary.dbValue == "N" ) {
                       style = colors[3];
                       displayVal = "N";
                    } else if ( phenSummary.dbValue == "Y" ) {
                       style = colors[4];
                       displayVal = "Y";
                    } else if ( phenSummary.dbValue == "F" ) {
                       style = colors[3];
                       displayVal = "F";
                    } else if ( phenSummary.dbValue == "M" ) {
                       style = colors[4];
                       displayVal = "M";
                    } else {
                       style = colors[colorIdx];
                       displayVal = phenSummary.dbValue;
                    }

                    var ret = "<span  style='color:" + style + "'>" + displayVal + "</span>";
                    return ret;
                 },
              },

              {
                 text : 'Select Subject values',
                 hidden : true,
                 dataIndex : 'phenoSummaryMap',
                 width : 60,
                 renderer : function(value, metadata, record) {

                    var phenSummary = value.phenoSummaryMapSelectedSubjects;
                    var displaySummary = value.displaySummarySelectedSubjects
                    if ( phenSummary != null ) {
                       var ret = "<canvas width='50' height='20' id=multi" + value.name.replace( / /g, '' ) + ">"
                          + "</canvas>";
                       metadata.tdAttr = 'data-qtip="' + displaySummary + ret + '"';
                       return ret;
                    } else
                       return "";
                 },

              },

              {
                 text : 'Value',
                 tooltip : 'Number of subjects for each phenotype value',
                 tooltipType : 'title',
                 dataIndex : 'allPhenoSummaryMap',
                 renderer : function(value, metadata, record) {
                    var phenSummary = value.phenoSummaryMap;

                    if ( phenSummary != null ) {
                       var ret = "<canvas width='50' height='20' id=all" + value.name.replace( / /g, '' ) + ">"
                          + "</canvas>";
                       metadata.tdAttr = 'data-qtip="' + value.displaySummary + ret + '"';
                       return ret;
                    } else
                       return "";
                 },
                 width : 60,
              // flex : 1
              } ],
   listeners : {
      sortchange : function(phenotypeGrid, sortinfo) {

         ASPIREdb.EVENT_BUS.fireEvent( 'allPhenoSummary_sorted' );
         
//         if ( sortinfo.dataIndex == 'allPhenoSummaryMap' )
//            ASPIREdb.EVENT_BUS.fireEvent( 'allPhenoSummary_sorted' );
//         if ( sortinfo.dataIndex == 'phenoSummaryMap' )
//            ASPIREdb.EVENT_BUS.fireEvent( 'selectedPhenoSummary_sorted' );
      }
   },

   store : Ext.create( 'ASPIREdb.store.PhenotypeStore' ),

   initComponent : function() {
      this.callParent();

      this.getDockedComponent( 'phenotypeGridToolbar' ).getComponent( 'heatmapButton' ).on( 'click', this.viewHeatmap,
         this );

      this.getDockedComponent( 'phenotypeGridToolbar' ).getComponent( 'contingencyTableButton' ).on( 'click',
         this.viewSubjectLabel, this );

      var ref = this;

      ASPIREdb.EVENT_BUS.on( 'filter_submit', function() {

         ref.setLoading( true );
         ref.getStore().removeAll();

      } );

      // when phenotypes selected
      this.on( 'selectionchange', this.selectionChangeHandler, this );

      ASPIREdb.EVENT_BUS.on( 'subjects_loaded', function(subjectIds) {

         ref.currentSubjectIds = subjectIds;

         SubjectService.getPhenotypeSummaries( subjectIds, ASPIREdb.ActiveProjectSettings.getActiveProjectIds(), {
            callback : function(vos) {// vos is a list of phenotypeSummaryValueobjects (converted to a javascript
               // Array)

               var data = [];
               ref.phenotypeSummaryValueObjects = [];

               if ( vos.length > 0 ) {
                  ref.getDockedComponent( 'phenotypeGridToolbar' ).getComponent( 'heatmapButton' ).enable();
               } else {
                  ref.getDockedComponent( 'phenotypeGridToolbar' ).getComponent( 'heatmapButton' ).disable();
               }

               for (var i = 0; i < vos.length; i++) {
                  var phenSummary = vos[i];

                  ref.phenotypeSummaryValueObjects[i] = phenSummary;

                  // [ phenSummary.name, phenSummary.selectedPhenotype, subjectVal]
                  // TODO find a more elegant way of doing this ...
                  var row = [ phenSummary, phenSummary, phenSummary, phenSummary ];
                  data.push( row );
               }

               ref.down( '#statusbar' ).update( vos.length + " phenotypes loaded" );

               ref.store.loadData( data );
               ref.updatePhenotypeSummaryCanvasesAllSubjects();
               ref.setLoading( false );

            }
         } );

      } );

      var saveButton = ref.getDockedComponent( 'phenotypeGridToolbar' ).getComponent( 'saveButton' );

      saveButton.on( 'click', function() {
         ASPIREdb.TextDataDownloadWindow.show();
         ASPIREdb.TextDataDownloadWindow.setLoading( true );

         SubjectService.getPhenotypeTextDownloadBySubjectIds( ref.currentSubjectIds, ref.saveButtonHandler );
      } );

      ASPIREdb.EVENT_BUS.on( 'select_subject_from_variant_grid', this.subjectSelectHandler, this );
      ASPIREdb.EVENT_BUS.on( 'subject_selected', this.subjectSelectHandler, this );
      ASPIREdb.EVENT_BUS.on( 'allPhenoSummary_sorted', this.sortAllPhenoSummary, this );
      ASPIREdb.EVENT_BUS.on( 'selectedPhenoSummary_sorted', this.sortSelectedPhenoSummary, this );

   },

   /**
    * when all phenotype summary canvas sorted
    */
   sortAllPhenoSummary : function() {

      this.updatePhenotypeSummaryCanvasesAllSubjects();
      this.updatePhenotypeSummaryCanvasesSelectedSubjects();
      this.setLoading( false );

   },

   /**
    * This method called when phenotypes are selected in the phenotype grid
    */
   selectionChangeHandler : function() {
      this.selPhenotypes = this.getSelectionModel().getSelection();

      if ( this.selPhenotypes.length > 0 && this.selPhenotypes.length < 4 ) {
         var names = [];

         for (var i = 0; i < this.selPhenotypes.length; i++) {
            names.push( this.selPhenotypes[i].data.name );
         }
         ASPIREdb.EVENT_BUS.fireEvent( 'phenotype_selected', names );
         this.getDockedComponent( 'phenotypeGridToolbar' ).getComponent( 'contingencyTableButton' ).enable();

      } else {
         ASPIREdb.EVENT_BUS.fireEvent( 'phenotype_selected', null );
         this.getDockedComponent( 'phenotypeGridToolbar' ).getComponent( 'contingencyTableButton' ).disable();
      }

   },
   /**
    * when all phenotype summary canvas sorted
    */
   sortSelectedPhenoSummary : function() {
      this.updatePhenotypeSummaryCanvasesAllSubjects();
      this.updatePhenotypeSummaryCanvasesSelectedSubjects();
      this.setLoading( false );

   },
   /**
    * Loads selected Subject's phenotypes
    * 
    * @param subjectId
    */
   subjectSelectHandler : function(subjectIds) {// todo

      if ( !subjectIds || subjectIds.length == 0 ) {
         var col = this.columns[this.SELECTED_VALUES_COL_IDX];

         col.setText( "" );
         col.setVisible( false );

         return;
      }

      var activeProjectId = ASPIREdb.ActiveProjectSettings.getActiveProjectIds()[0];

      var ref = this;
      console.log( "on subject select handler in phenotype grid ..........." );

      if ( subjectIds.length == 1 ) {
         SubjectService.getSubject( activeProjectId, subjectIds[0], {
            callback : function(svo) {
               if ( svo != null ) {
                  ref.setLoading( true );

                  PhenotypeService.getPhenotypes( svo.id, {
                     callback : function(voMap) {// these vos a

                        ref.columns[2].setVisible( false );

                        var col = ref.columns[ref.SELECTED_VALUES_COL_IDX];
                        col.setText( svo.patientId );
                        col.setVisible( true );

                        for (var i = 0; i < ref.phenotypeSummaryValueObjects.length; i++) {
                           var phenSummary = ref.phenotypeSummaryValueObjects[i];
                           var subjectPhenotype = voMap[phenSummary.name];
                           phenSummary.selectedPhenotype = subjectPhenotype;
                        }

                        ref.getView().refresh( true );
                        ref.updatePhenotypeSummaryCanvasesAllSubjects();
                        ref.setLoading( false );
                     }
                  } );
               }
            }
         } );
      } else {

         SubjectService.getPhenotypeSummaryValueObjects( subjectIds, ASPIREdb.ActiveProjectSettings
            .getActiveProjectIds(), {
            callback : function(voMap) {// voMap is a <String, PhenotypeSummaryValueObject>Map

               if ( voMap != null ) {
                  ref.setLoading( true );

                  // validate that single case column is hidden
                  ref.columns[1].setVisible( false );

                  var col = ref.columns[2];
                  col.setVisible( true );

                  var data = [];

                  for (var i = 0; i < ref.phenotypeSummaryValueObjects.length; i++) {
                     var phenSummary = ref.phenotypeSummaryValueObjects[i];
                     var phenoSummaryValueObject = voMap[phenSummary.name];
                     // we are attaching a new property to phenSummary here, calling it
                     // phenoSummaryMapSelectedSubjects

                     if ( phenoSummaryValueObject ) {
                        phenSummary.phenoSummaryMapSelectedSubjects = phenoSummaryValueObject.phenoSummaryMap;
                        phenSummary.displaySummarySelectedSubjects = phenoSummaryValueObject.displaySummary;
                     } else {// if phenoSummaryValueObject is null or undefined
                        console.log( "null or undefined phenoSummaryValueObject: " + phenSummary.name );
                     }

                  }
                  ref.getView().refresh( true );
                  ref.updatePhenotypeSummaryCanvasesAllSubjects();
                  ref.updatePhenotypeSummaryCanvasesSelectedSubjects();
                  ref.setLoading( false );
               }

            }
         } );

      }

   },

   updatePhenotypeSummaryCanvasesSelectedSubjects : function() {

      for (var i = 0; i < this.phenotypeSummaryValueObjects.length; i++) {
         var phenSummary = this.phenotypeSummaryValueObjects[i];

         // phenSummary.phenoSummaryMapSelectedSubjects is the new parameter we added in the javascript
         var phenMap = phenSummary.phenoSummaryMapSelectedSubjects;

         var canvas = document.getElementById( "multi" + phenSummary.name.replace( / /g, '' ) );

         this.drawCanvas( canvas, phenSummary, phenMap );

      }

   },

   updatePhenotypeSummaryCanvasesAllSubjects : function() {

      for (var i = 0; i < this.phenotypeSummaryValueObjects.length; i++) {
         var phenSummary = this.phenotypeSummaryValueObjects[i];

         // phenSummary.phenoSummaryMapSelectedSubjects is the new parameter we added in the javascript
         var phenMap = phenSummary.phenoSummaryMap;

         var canvas = document.getElementById( "all" + phenSummary.name.replace( / /g, '' ) );

         this.drawCanvas( canvas, phenSummary, phenMap );

      }

   },

   drawCanvas : function(canvas, phenSummary, phenMap) {

      if ( phenMap === undefined ) {
         // console.log( "phenSummary name " + phenSummary.name + " phenMap is undefined" );
         return;
      }

      var keyArray = phenSummary.phenoSet;

      var total = 0;
      for (var j = 0; j < keyArray.length; j++) {
         total = total + phenMap[keyArray[j]];
      }

      var ctx = canvas.getContext( "2d" );
      ctx.font = "bold 8px sans-serif";
      ctx.textAlign = "center";

      var width = 50;
      var height = 20;
      var yValue = 0;
      var xValue = 0;
      var colorIndex = 3;
      /**
       * Used the Color Brewer 2.0 system for coloring the chart Thanks for Cynthia Brewer, Mark Harrower and The
       * Pennsylvania State University
       */
      var colors = [ "#b35806", "#31a354", "#636363", "#d8b365", "#2c7fb8", "#addd8e", "#7570b3", "#a6bddb" ];
      var displayVal = '';

      // draw Y axis
      /**
       * ctx.beginPath(); ctx.moveTo(0,50); ctx.lineTo(0,0); ctx.closePath(); ctx.stroke();
       * 
       * //draw Y axis ctx.beginPath(); ctx.moveTo(0,50); ctx.lineTo(50,50); ctx.closePath(); ctx.stroke();
       */

      for (var k = 0; k < keyArray.length; k++) {
         if ( keyArray[k] != "Unknown" ) {

            if ( phenSummary.valueType == "HPONTOLOGY" ) {
               // horizontal = -(phenMap["Present"]*width)/total,5)
               if ( keyArray[k] == "Present" ) {

                  ctx.fillStyle = colors[0];
                  ctx.fillRect( xValue, yValue, (phenMap["Present"] * width) / total, 5 );
                  yValue = yValue + 5;
                  displayVal = displayVal + "Present(" + phenMap["Present"] + ")";

               } else if ( keyArray[k] == "Absent" ) {

                  ctx.fillStyle = colors[1];
                  ctx.fillRect( xValue, yValue, (phenMap["Absent"] * width) / total, 5 );
                  yValue = yValue + 5;
                  displayVal = displayVal + "Absent(" + phenMap["Absent"] + ")";

               } else {

                  ctx.fillStyle = colors[colorIndex];
                  ctx.fillRect( xValue, yValue, (phenMap[keyArray[k]] * width) / total, 5 );
                  colorIndex++;
                  yValue = yValue + 5;
                  displayVal = displayVal + keyArray[k] + "(" + phenMap[keyArray[k]] + ")";

               }
            } else {

               ctx.fillStyle = colors[colorIndex];
               ctx.fillRect( xValue, yValue, (phenMap[keyArray[k]] * width) / total, 5 );
               colorIndex++;
               yValue = yValue + 5;
               displayVal = displayVal + keyArray[k] + "(" + phenMap[keyArray[k]] + ")";

            }
            ;
         } else {
            var unknown = phenMap["Unknown"];
         }

      }
      if ( unknown != null ) {
         ctx.fillStyle = colors[2];
         ctx.fillRect( xValue, yValue, (phenMap["Unknown"] * width) / total, 5 );
         yValue = yValue + 5;
         displayVal = displayVal + "Unknown(" + phenMap["Unknown"] + ")";

      }

   },

   saveButtonHandler : function(text) {
      ASPIREdb.TextDataDownloadWindow.setLoading( false );
      ASPIREdb.TextDataDownloadWindow.showPhenotypesDownload( text );
   },

   getSubjectValue : function(phenSummary) {

      var valueToSubjectSet = phenSummary.dbValueToSubjectSet;

      var subjectValue = '';

      var keyArray = [];

      // if this is a special(large) project, this will not be populated
      if ( !valueToSubjectSet.Unknown ) {
         return subjectValue;
      }

      for ( var key in valueToSubjectSet) {
         if ( key !== "Unknown" ) {
            keyArray.push( key );
         }

      }

      keyArray.sort();
      // ensure "Unknown is at the end
      keyArray.push( "Unknown" );

      for (var i = 0; i < keyArray.length; i++) {

         var key = keyArray[i];

         if ( phenSummary.valueType == "HPONTOLOGY" ) {
            if ( key == this.DB_VAL_HPO_PRESENT ) {
               subjectValue = subjectValue + ' Present(' + valueToSubjectSet[key].length + ')';
               subjectValue = "<span " + this.STYLE_HPO_PRESENT + ">" + subjectValue + "</span>";
            } else if ( key == this.DB_VAL_HPO_ABSENT ) {
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

   viewHeatmap : function() {
      var ref = this;
      var removeEmpty = true;

      SubjectService.getPhenotypeBySubjectIds( ref.currentSubjectIds, removeEmpty, {
         callback : function(matrix) {
            ASPIREdb.view.SubjectPhenotypeHeatmapWindow.show();
            ASPIREdb.view.SubjectPhenotypeHeatmapWindow.draw( matrix );
         }
      } );
   },

   viewSubjectLabel : function() {
      console.log( "view subject labels" );
      ASPIREdb.view.PhenotypesContigencyTableWindow.initGridAndShow( this.phenotypeSummaryValueObjects,
         this.selPhenotypes );

   },

   viewNeurocartaGenes : function(value, name) {
      ASPIREdb.view.NeurocartaGeneWindow.initGridAndShow( value, name );
   }

} );
