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
Ext.require( [ 'Ext.form.*', 'Ext.layout.container.Column', 'Ext.tab.Panel',

] );
/**
 * Upload Panel
 */
Ext.define( 'ASPIREdb.view.UploadManagerPanel', {
   extend : 'Ext.form.Panel',
   alias : 'widget.uploadManagerPanel',
   frame : true,
   bodyStyle : 'padding:5px 5px 0',
   // width: 350,
   fileUpload : true,
   config : {
      variantSrc : null,
   },
   fieldDefaults : {
      labelWidth : 75,
      msgTarget : 'side'
   },
   defaults : {
      anchor : '100%'
   },

   items : {
      xtype : 'tabpanel',
      activeTab : 0,
      defaults : {
         bodyStyle : 'padding:10px'
      },

      items : [ {
         title : 'New Project Upload',
         defaultType : 'textfield',
         width : 750,

         items : [ /**
                      * { xtype : 'fieldset', title : 'Project', collapsible : true, autoWidth : true, autoheight :
                      * true, defaultType : 'textfield', layout : 'anchor', defaults : { anchor : '100%' }, items : [ {
                      * fieldLabel : 'Project Name', name : 'projectName', id : 'projectName', value : '', labelWidth :
                      * 150,
                      *  }, { fieldLabel : 'Project Decription', name : 'projectDescription', id : 'projectDescription',
                      * value : '', labelWidth : 150, } ] },
                      */
         {
            xtype : 'fieldset',
            title : 'Variants',
            collapsible : true,
            autoWidth : true,
            autoheight : true,
            defaultType : 'textfield',
            layout : 'anchor',
            defaults : {
               anchor : '100%'
            },
            items : [ {
               /**
                * xtype: 'filefield', name: 'file', fieldLabel: 'File', labelWidth: 50, msgTarget: 'side', allowBlank:
                * false, anchor: '100%', buttonText: 'Select a File...'
                */

               xtype : 'filefield',
               id : 'variantFile',
               name : 'file',
               width : 600,
               allowBlanck : false,
               emptyText : 'Select variant file to upload',
               fieldLabel : 'File',
               labelWidth : 150,
               buttonText : 'Select',
               handler : function() {
                  console.log( 'file upload handler ' );
               },
               listeners : {
                  afterrender : function(el) {
                     var element = el.fileInputEl;
                     console.log( element );
                     return element;
                  },
                  change : function(fld, value) {

                     var newValue = value.replace( /C:\\fakepath\\/g, '' );
                     fld.setRawValue( newValue );
                  }
               }

            }, {
               xtype : 'combobox',
               fieldLabel : 'variant Type',
               id : 'variantType',
               editable : false,
               displayField : 'variantType',
               // allowBlank : false,
               valueField : 'id',
               store : Ext.create( 'Ext.data.Store', {
                  fields : [ 'id', 'variantType' ],
                  data : [ {
                     id : 'cnv',
                     variantType : 'CNV'
                  }, {
                     id : 'snv',
                     variantType : 'SNV'
                  }, {
                     id : 'indel',
                     variantType : 'INDEL'
                  }, {
                     id : 'inversion',
                     variantType : 'INVERSION'
                  }, {
                     id : 'translocation',
                     variantType : 'TRANSLOCATION'
                  }, {
                     id : 'decipher',
                     variantType : 'DECIPHER'
                  }, {
                     id : 'dvg',
                     variantType : 'DGV'
                  }, ]
               } ),
               labelWidth : 150,

            } ]
         }, {
            xtype : 'fieldset',
            title : 'Phenotypes',
            collapsible : true,
            autoWidth : true,
            autoheight : true,
            defaultType : 'textfield',
            layout : 'anchor',
            defaults : {
               anchor : '100%'
            },
            items : [ {
               xtype : 'filefield',
               id : 'phenotypeFile',
               name : 'phenotypeFile',
               width : 600,
               emptyText : 'Select phenotype file to upload',
               fieldLabel : 'Upload Phenotype List',
               labelWidth : 150,
               name : 'phenotypeUploadFile-path',
               buttonText : 'Select',
            } ]
         }

         ]
      } ]
   },

   buttons : [
              {
                 text : 'Upload',
                 // disabled : true,
                 // formBind : true,
                 handler : function() {
                    var form = this.up( 'uploadManagerPanel' ).getForm();
                    var me = this;
                    if ( form.isValid() ) {

                       // getting the form values
                       values = form.getFieldValues();
                       var projectName = values['projectName'];
                       var projectDescription = values['projectDescription'];

                       // create project
                       ProjectService.createUserProject( projectName, projectDescription, {
                          callback : function(projectId) {
                             console.log( 'reading uplodaed files' );

                          },
                          errorHandler : function(er, exception) {
                             Ext.Msg.alert( "create project Error", er + "\n" + exception.stack );
                             console.log( exception.stack );
                          }
                       } );

                       /**
                         * form.submit( {
                         * 
                         * method : 'POST', url : '/aspiredb/upload_action.html', // submitEmptyText : false,
                         * 
                         * waitMsg : 'Uploading your file...', headers: {'Content-Type':'multipart/form-data;
                         * charset=UTF-8'}, success : function(form, action) { Ext.Msg.alert( 'Success', 'Your file has
                         * been uploaded.' ); }, failure : function(form, action) { Ext.Msg.alert( 'Failed',
                         * action.result ? action.result.message : 'No response' ); } } );
                         */

                       var variantPath = form.findField( 'variantFile' ).getValue();
                       var variantDirectory = variantPath.substring( 0, variantPath.lastIndexOf( "\\" ) );
                       var variantFilename = variantPath.substring( variantPath.lastIndexOf( "\\" ) + 1,
                          variantPath.length );
                       var variantType = values['variantType-inputEl'].toUpperCase();
                       var file = Ext.getCmp( 'variantFile' ).getEl().down( 'input[type=file]' ).dom.files[0]; // variantType.setValue(values['variantType-inputEl']);
                                                                                                               // //
                       variantType.setValue( values['variantType-inputEl'] );
                       var phenotypePath = form.findField( 'phenotypeUploadFile-path' ).getValue();
                       var phenotypeDirectory = phenotypePath.substring( 0, phenotypePath.lastIndexOf( "\\" ) );
                       var phenotypeFilename = phenotypePath.substring( phenotypePath.lastIndexOf( "\\" ) + 1,
                          phenotypePath.length );

                       var fReader = new FileReader();
                       // fReader.readAsDataURL( file ); 
                       fReader.readAsBinaryString( file );

                       fReader.onloadend = function(event) {
                          var variantSrc = event.target.result;
                          console.log( 'reader finish reading' + variantSrc );
                          // add variants to the project
                          ProjectService.addSubjectVariantsToExistingProject( variantSrc, projectName, variantType, {
                             callback : function(errorMessage) {

                                Ext.Msg.alert( 'Error', 'Add to variant DWR returns Error :' + errorMessage );
                             },
                             errorHandler : function(er, exception) {
                                Ext.Msg.alert( "create variant Error", er + "\n" + exception.stack );
                                console.log( exception.stack );
                             }
                          } );
                       }

                    } else {
                       // Ext.Msg.alert( "Error!", "Your form is invalid!" );
                       fieldNames = [];
                       fields = form.getFields();
                       for (var i = 0; i < fields.length; i++) {
                          field = fields[i];
                          if ( field == undefined )
                             fieldNames.push( field.getName() );
                       }
                       console.debug( fieldNames );
                       Ext.MessageBox.alert( 'Invalid Fields', 'The following fields are invalid: '
                          + fieldNames.join( ', ' ) );
                    }

                 }

              }, {
                 text : 'Cancel'
              } ]
} );
