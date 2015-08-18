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

Ext.define( 'ASPIREdb.Utils', {
   singleton : true,
   
   /**
    * Renders the label
    * 
    * @param visibleLabels - array of label value objects
    * @param value - array of label IDs
    * @param metaData - to show tooltips 
    */
   renderLabel : function( visibleLabels, value, metaData ) {
      var ret = "";
      var qtip = "";
      
      for (var i = 0; i < value.length; i++) {

         var label = visibleLabels[value[i]];
         if ( label === undefined ) {
            continue;
         }
         if ( label.isShown ) {
            
            var labelHtml = "<span style='color: white; background-color:#"
               + label.colour + "'>&nbsp;" + label.label + "&nbsp;</span>";
            ret += "<p style='line-height:50%; font-size: x-small;'>" + labelHtml + "</p>";
            
            var desc = label.description === null ? "" : label.description;
            qtip += "<p>" + labelHtml + "&nbsp;" + desc + "</p>";
               
         }

      }

      metaData.tdAttr = 'data-qtip="' + qtip + '"';
      return ret;
   },
   
   /**
    * Extract IDs from the current selection.
    */
   getSelectedIds : function(selectedVariantRecords) {

      var selectedVariantIds = [];

      for (var i = 0; i < selectedVariantRecords.length; i++) {
         selectedVariantIds.push( selectedVariantRecords[i].data.id );
      }

      return selectedVariantIds;
   },
   
} );