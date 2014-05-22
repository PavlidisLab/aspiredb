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

         items : [ {
            xtype : 'fieldset',
            title : 'Project',
            collapsible : true,
            autoWidth : true,
            autoheight : true,
            defaultType : 'textfield',
            layout : 'anchor',
            defaults : {
               anchor : '100%'
            },
            items : [ {
               fieldLabel : 'Project Name',
               name : 'projectName',
               id : 'projectName',
               value : '',
               labelWidth : 150,

            }, {
               fieldLabel : 'Project Decription',
               name : 'projectDescription',
               id : 'projectDescription',
               value : '',
               labelWidth : 150,
            } ]
         }, {
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
               xtype : 'filefield',
               id : 'variantFile',
               width : 600,
               emptyText : 'Select variant file to upload',
               fieldLabel : 'Upload Variant List',
               labelWidth : 150,
               name : 'varintUploadFile-path',
               buttonText : 'Select',

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
         }, /**{
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
               width : 600,
               emptyText : 'Select phenotype file to upload',
               fieldLabel : 'Upload Phenotype List',
               labelWidth : 150,
               name : 'phenotypeUploadFile-path',
               buttonText : 'Select',

            } ]
         }*/]
      },/** {
         title : 'Existing Project Upload',
         defaultType : 'textfield',
         width : 750,

         items : [ {
            xtype : 'combobox',
            fieldLabel : 'Project',
            id : 'userProjectField',
            name : 'unit',
            editable : false,
            displayField : 'name',
            allowBlank : false,
            valueField : 'id',
            store : Ext.create( 'Ext.data.Store', {
               proxy : {
                  type : 'dwr',
                  dwrFunction : ProjectService.getProjects,
                  model : 'ASPIREdb.model.Project',
                  reader : {
                     type : 'json',
                     root : 'name'
                  }
               }
            } ),
            // typeAhead : true,
            // queryMode : 'local',
            emptyText : 'Choose project...',
            forceSelection : true,
            msgTarget : 'qtip',

         } ]
      }*/ ]
   },

   buttons : [
              {
                 text : 'Upload',
                 // disabled : true,
                  //formBind : true,
                 handler : function() {
                    var form = this.up( 'uploadManagerPanel' ).getForm();
                     console.log('form project name: '+form);
                    if ( form.isValid() ) {
                       form.submit( {
                          clientValidation: true,
                          scope: this,
                          params: {
                             newStatus: 'delivered'
                         },
                          // method :'POST',
                          // url : 'upload.action', // 'xml-form-errors.xml',
                          // submitEmptyText : false,
                          waitMsg : 'Uploading your file...',
                          success : function(form,action) {
                             Ext.Msg.alert( 'Success', 'Your file has been uploaded.' );
                             // getting the form values
                             var projectName = form.findField( 'projectName' ).getValue();
                             var projectDescription = form.findField( 'projectDescription' ).getValue();
                             var variantPath = form.findField( 'varintUploadFile-path' ).getValue();
                             var variantDirectory = variantPath.substring( 0, variantPath.lastIndexOf( "\\" ) );
                             var variantFilename = variantPath.substring( variantPath.lastIndexOf( "\\" ) + 1,
                                variantPath.length );
                             var variantType = new VariantType();
                             variantType.setValue( form.findField( 'variantType' ).getValue() );
                             var phenotypePath = form.findField( 'phenotypeUploadFile-path' ).getValue();
                             var phenotypeDirectory = phenotypePath.substring( 0, phenotypePath.lastIndexOf( "\\" ) );
                             var phenotypeFilename = phenotypePath.substring( phenotypePath.lastIndexOf( "\\" ) + 1,
                                phenotypePath.length );

                             // create project
                             ProjectService.createUserProject( projectName, projectDescription, {
                                callback : function(projectId) {
                                   ProjectService.addSubjectVariantsToExistingProject( variantDirectory,
                                      variantFilename, projectName, variantType, {
                                         callback : function(errorMessage) {

                                         },
                                         errorHandler : function(er, exception) {
                                            xt.Msg.alert( "create variant Error", er + "\n" + exception.stack );
                                            console.log( exception.stack );
                                         }
                                      } );

                                },
                                errorHandler : function(er, exception) {
                                   xt.Msg.alert( "create project Error", er + "\n" + exception.stack );
                                   console.log( exception.stack );
                                }
                             } );

                          },
                          failure : function(form, action) {
                             Ext.Msg.alert( 'Failed', action.result ? action.result.message : 'No response' );
                          }
                       /**
                         * handler: function() { var form = this.up('form').getForm(); if(form.isValid()){ form.submit({
                         * url: 'upload.action', waitMsg: 'Uploading your file...', success: function(fp, o) {
                         * Ext.Msg.alert('Success', 'Your file has been uploaded.'); } }); } }
                         */
                       } );
                    } else {
                      // Ext.Msg.alert( "Error!", "Your form is invalid!" );
                       fieldNames = [];                
                       fields = form.getFields();
                       for(var i=0; i <  fields.length; i++){
                           field = fields[i];
                           if (field == undefined)
                            fieldNames.push(field.getName());
                        }
                       console.debug(fieldNames);
                       Ext.MessageBox.alert('Invalid Fields', 'The following fields are invalid: ' + fieldNames.join(', '));
                    }

                 }

              }, {
                 text : 'Cancel'
              } ]
} );

// fsf.render(document.body);
