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

Ext.define( 'ASPIREdb.GemmaURLUtils', {
   singleton : true,

   GEMMA_URL : "https://gemma.msl.ubc.ca",
   GEMMA_WEB_SERVICE_URL : "https://gemma.msl.ubc.ca/rest/v2",

   HELP_PAGE_URL : "http://aspiredb.sites.olt.ubc.ca/",

   makeViewGeneNetworkInGemmaURL : function(geneSymbols) {

      var url = ASPIREdb.GemmaURLUtils.GEMMA_URL + "/home.html?taxon=1&geneList=";

      for (var i = 0; i < geneSymbols.length; i++) {

         url = url + geneSymbols[i] + ",";

      }

      return url;

   },

   makeGeneUrl : function(geneSymbol) {
      return ASPIREdb.GemmaURLUtils.GEMMA_URL + "/gene/showGene.html?name=" + geneSymbol + "&taxon=human";
   },

   makeNeurocartaPhenotypeUrl : function(phenotypeUri) {
      return ASPIREdb.GemmaURLUtils.GEMMA_URL + "/phenotypes.html?phenotypeUrlId=" + phenotypeUri;
   },

   makeWebServiceUrl : function(path) {
      return ASPIREdb.GemmaURLUtils.GEMMA_WEB_SERVICE_URL + path;
   },

   getHelpPageURL : function() {
      return ASPIREdb.GemmaURLUtils.HELP_PAGE_URL;
   }
} );
