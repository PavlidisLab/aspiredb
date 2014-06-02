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

Ext.require([ 'Ext.Window', 'ASPIREdb.view.LabelManagerPanel','ASPIREdb.GemmaURLUtils' ]);
/**
 * Label manager has Label Panel
 */
Ext.define('ASPIREdb.view.LabelManagerWindow', {
   extend : 'Ext.Window',
   alias : 'widget.LabelManagerWindow',
   singleton : true,
   title : 'Label Manager',
   closable : true,
   closeAction : 'hide',
   width : 1000,
   height : 500,
   layout : 'fit',
   bodyStyle : 'padding: 5px;',
   
   
   items : [{
      region : 'center',
      itemId : 'ASPIREdb_Labelmanagerpanel',
      xtype : 'ASPIREdb_Labelmanagerpanel',
   }],

    config :{
       LabelsetSize :[],
    },
   
   initComponent : function() {
   
      var ref = this;
      this.callParent();      

   },
   
   /**
    * Show the Label manager window
    */   
   initGridAndShow : function(){
      
      var ref = this;
      var panel = ASPIREdb.view.LabelManagerWindow.down('#ASPIREdb_Labelmanagerpanel');
      var grid =panel.down ('#LabelGrid');
      
      ref.show();
      grid.setLoading(true);
      
      ref.LabelsetSize=[]

   
      // ASPIREdb.view.LabelManagerWindow.populateLabelSetGrid(LabelSetNames,ref.LabelsetSize);
    /**  LabelService.getSavedUserLabelSets( {
         callback : function(gvos) { 
            ASPIREdb.view.LabelManagerWindow.populateLabelSetGrid(gvos);
         }
      });*/
   
   
   },
   
   
   
   /**
    * Populate and Label set names in the Label set grid
    */
   populateLabelSetGrid : function(gvos) {
      
      var panel = ASPIREdb.view.LabelManagerWindow.down('#ASPIREdb_Labelmanagerpanel');
      var grid =panel.down ('#LabelGrid');
      
         
      var data = [];
      for ( var i = 0; i < gvos.length; i++) {
         var row = [ gvos[i].name,'',gvos[i].object.length];      
         data.push(row);               
      }
         
      grid.store.loadData(data);
      grid.setLoading(false);    
      grid.getView().refresh();
      grid.enableToolbar();
   }, 
   
   
      
   clearGridAndMask : function(){
      ASPIREdb.view.LabelManagerWindow.getComponent('ASPIREdb_Labelmanagerpanel').store.removeAll();
      ASPIREdb.view.LabelManagerWindow.getComponent('ASPIREdb_Labelmanagerpanel').setLoading(true);           
   }

});