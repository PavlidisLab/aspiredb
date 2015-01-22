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
Ext.require( [] );

/**
 * Create Variant Report Chart Panel
 */
Ext.define( 'ASPIREdb.view.report.PhenotypePerSubjectLabel', {
   extend : 'ASPIREdb.view.report.VariantReportPanel',
   alias : 'widget.phenotypePerSubjectLabel',
   itemId : 'phenotypePerSubjectLabel',
   xtype : 'clustered-column',
   layout : 'fit',

   config : {},

   initComponent : function() {
      this.callParent();
   },

   createReport : function(store, columnName) {

      var me = this;
      var reportWindow = me.up( '#variantReportWindow' );

      reportWindow.setLoading( true );

      // get a list of variants grouped by Subject labels
      // var variantIds = this.getColumnDataFromStore( store, 'id' );
      var variantIds = ASPIREdb.view.report.VariantReportWindow.getColumnDataFromStore( store, 'id' );

      var columnName = 'phenotype';
      var labelNames = [ 'label_A', 'non_label_A' ];

      var mergedFreqData = [ {
         'phenotype' : 'pheno_1',
         'label_A' : 20,
         'non_label_A' : 10
      }, {
         'phenotype' : 'pheno_2',
         'label_A' : 40,
         'non_label_A' : 60
      }, ];

      reportWindow.setLoading( false );
      me.createLabelReport( mergedFreqData, columnName, labelNames );

   }

} );