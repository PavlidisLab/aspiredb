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

/**
 * Ideogram colour legend for colouring variants based on variant type, CNV type, etc.
 */
Ext.define( 'ASPIREdb.view.ideogram.ColourLegend', {
   extend : 'Ext.window.Window',
   alias : 'widget.colourlegend',
   width : 150,
   height : 150,
   autoScroll : true,
   title : 'Ideogram',
   closable : false,
   resizable : true,
   layout : 'absolute',
   items : [ {
      xtype : 'component',
      autoEl : 'canvas',
      itemId : 'canvasBox',
      x : 0,
      y : 0,
      width : 200,
      height : 200
   } ],
   tools : [ {
      type : 'restore',
      hidden : true,
      handler : function(evt, toolEl, owner, tool) {
         var window = owner.up( 'window' );
         window.expand( '', false );
         window.setWidth( winWidth );
         window.center();
         isMinimized = false;
         this.hide();
         this.nextSibling().show();
      }
   }, {
      type : 'minimize',
      handler : function(evt, toolEl, owner, tool) {
         var window = owner.up( 'window' );
         window.collapse();
         winWidth = window.getWidth();
         window.setWidth( 150 );
         window.alignTo( Ext.getBody(), 'b-b' );
         this.hide();
         this.previousSibling().show();
         isMinimized = true;
      }
   } ]
} );