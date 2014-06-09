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
Ext.require( [ 'Ext.form.*', 'Ext.layout.container.Column', 'Ext.tab.Panel', 'Ext.ProgressBar' ] );
/**
 * Upload Panel
 */
Ext
   .define(
      'ASPIREdb.view.ProjectUploadGrid',
      {
         extend : 'Ext.form.Panel',
         alias : 'widget.ProjectUploadGrid',
         id : 'ProjectUploadGrid',
         frame : true,
         bodyStyle : 'padding:5px 5px 0',
         fileUpload : true,

         config : {
            variantSrc : null,
            selectedProject : [],
         },
         fieldDefaults : {
            labelWidth : 75,
            msgTarget : 'side'
         },
         defaults : {
            anchor : '100%'
         },

         items : {
            activeTab : 0,
            defaults : {
               bodyStyle : 'padding:10px'
            },

            items : [ {
               defaultType : 'textfield',
               width : '100%',

               items : [ {
                  xtype : 'fieldset',
                  title : 'Variants',
                  autoWidth : true,
                  autoheight : true,
                  defaultType : 'textfield',
                  layout : 'anchor',
                  defaults : {
                     anchor : '100%'
                  },
                  items : [ {
                     xtype : 'filefield',
                     id : 'variantFileEdit',
                     name : 'file',
                     width : 400,
                     allowBlanck : false,
                     emptyText : 'Select variant file to upload',
                     fieldLabel : 'File',
                     labelWidth : 100,
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
                     id : 'variantTypeEdit',
                     editable : false,
                     displayField : 'variantTypeEdit',
                     valueField : 'id',
                     store : Ext.create( 'Ext.data.Store', {
                        fields : [ 'id', 'variantTypeEdit' ],
                        data : [ {
                           id : 'cnv',
                           variantTypeEdit : 'CNV'
                        }, {
                           id : 'snv',
                           variantTypeEdit : 'SNV'
                        }, {
                           id : 'indel',
                           variantTypeEdit : 'INDEL'
                        }, {
                           id : 'inversion',
                           variantTypeEdit : 'INVERSION'
                        }, {
                           id : 'translocation',
                           variantTypeEdit : 'TRANSLOCATION'
                        } ]
                     } ),
                     labelWidth : 100,

                  } ]
               }, {
                  xtype : 'fieldset',
                  title : 'Phenotypes',
                  autoWidth : true,
                  autoheight : true,
                  defaultType : 'textfield',
                  layout : 'anchor',
                  defaults : {
                     anchor : '100%'
                  },
                  items : [ {
                     xtype : 'filefield',
                     id : 'phenotypeFileEdit',
                     name : 'phenotypeFileEdit',
                     width : 400,
                     emptyText : 'Select phenotype file to upload',
                     fieldLabel : 'Upload Phenotype List',
                     labelWidth : 100,
                     buttonText : 'Select',
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
                  } ]
               }

               ]
            } ]
         },

         buttons : [
                    {
                       text : 'Upload',
                       id : 'uploadFilesEdit',
                       disabled : true,
                       // formBind : true,
                       handler : function() {
                          var form = this.up( 'ProjectUploadGrid' ).getForm();
                          var me = this;

                    /**      var Runner = function() {
                             var f = function(v, pbar, btn, count, cb) {
                                return function() {
                                   if ( v > count ) {
                                      btn.dom.disabled = false;
                                      cb();
                                   } else {
                                      if ( pbar.id == 'pbar4' ) {
                                         // give this one a
                                         // different count style
                                         // for fun
                                         var i = v / count;
                                         pbar.updateProgress( i, Math.round( 100 * i ) + '% completed...' );
                                      } else {
                                         pbar.updateProgress( v / count, 'Loading item ' + v + ' of ' + count + '...' );
                                      }
                                   }
                                };
                             };
                             return {
                                run : function(pbar, btn, count, cb) {
                                   btn.dom.disabled = true;
                                   var ms = 5000 / count;
                                   for (var i = 1; i < (count + 2); i++) {
                                      setTimeout( f( i, pbar, btn, count, cb ), i * ms );
                                   }
                                }
                             };
                          }();*/

                          if ( form.isValid() ) {
                             // getting the form values
                             values = form.getFieldValues();
                             var variantTypeEdit = values['variantTypeEdit-inputEl'].toUpperCase();
                             var fileEdit = Ext.getCmp( 'variantFileEdit' ).getEl().down( 'input[type=file]' ).dom.files[0];
                             var phenotypeFileEdit = Ext.getCmp( 'phenotypeFileEdit' ).getEl().down( 'input[type=file]' ).dom.files[0];
                             var projectName = Ext.getCmp('ProjectUploadGrid').selectedProject[0].data.ProjectName;

                             if ( fileEdit ) {
                                /** Uploading variants to the created project */
                                var fReader = new FileReader();
                                fReader.readAsBinaryString( fileEdit );

                                fReader.onloadend = function(event) {
                                   var variantSrc = event.target.result;

                                   // add
                                   // variants
                                   // to
                                   // the
                                   // project
                                   ProjectService
                                      .addSubjectVariantsToExistingProject( variantSrc, false, projectName,
                                         {
                                            callback : function(errorMessage) {
                                               if ( errorMessage == 'Success' ) {
                                                  Ext.Msg.alert( 'Success',
                                                     'You have successfully uploaded variant file' );
                                               } else
                                                  Ext.Msg
                                                     .alert( 'Server Reply', 'Uploading Variants  :' + errorMessage );
                                            },
                                            errorHandler : function(er, exception) {
                                               Ext.Msg.alert( "Upload variants Error", er + "\n" + exception.stack );
                                               console.log( exception.stack );
                                            }
                                         } );
                                };

                                if ( phenotypeFileEdit ) {

                                   // Uploading
                                   // phenoypes
                                   // to the
                                   // created
                                   // project
                                   var fpReader = new FileReader();
                                   fpReader.readAsBinaryString( phenotypeFileEdit );

                                   fpReader.onloadend = function(event) {
                                      var variantSrc = event.target.result;

                                      // add
                                      // variants
                                      // to
                                      // the
                                      // project
                                      ProjectService.addSubjectPhenotypeToExistingProject( variantSrc, false,
                                         projectName, variantTypeEdit, {
                                            callback : function(errorMessage) {
                                               if ( errorMessage == "Success" ) {
                                                  Ext.Msg.alert( 'Success',
                                                     'You have successfully uploaded phenotype file' );
                                               } else
                                                  Ext.Msg.alert( 'Server Reply', 'Uploading Phenotypes :'
                                                     + errorMessage );
                                            },
                                            errorHandler : function(er, exception) {
                                               Ext.Msg.alert( "Upload phenotype Error", er + "\n" + exception.stack );
                                               console.log( exception.stack );
                                            }
                                         } );
                                   };
                                }

                             }
                             if ( phenotypeFileEdit ) {

                                // Uploading
                                // phenoypes
                                // to the
                                // created
                                // project
                                var fpReader = new FileReader();
                                fpReader.readAsBinaryString( phenotypeFileEdit );

                                fpReader.onloadend = function(event) {
                                   var variantSrc = event.target.result;

                                   // add
                                   // variants
                                   // to
                                   // the
                                   // project
                                   ProjectService
                                      .addSubjectPhenotypeToExistingProject( variantSrc, false, projectName,
                                         variantTypeEdit, {
                                            callback : function(errorMessage) {
                                               if ( errorMessage == "Success" ) {
                                                  Ext.Msg.alert( 'Success',
                                                     'You have successfully uploaded phenotype file' );
                                               } else
                                                  Ext.Msg.alert( 'Server Reply', 'Uploading Phenotypes :'
                                                     + errorMessage );
                                            },
                                            errorHandler : function(er, exception) {
                                               Ext.Msg.alert( "Upload phenotype Error", er + "\n" + exception.stack );
                                               console.log( exception.stack );
                                            }
                                         } );
                                };

                             }

                             ASPIREdb.EVENT_BUS.fireEvent( 'new_project_created' );

                          } else {
                             // Ext.Msg.alert( "Error!", "Your form
                             // is invalid!" );
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
                       xtype : 'button',
                       text : 'Cancel',
                       itemId : 'cancelButton',
                       handler : function() {
                          ASPIREdb.view.ProjectManagerWindow.hide();
                       },
                    // scope :this,
                    } ],

         initComponent : function() {
            this.callParent();
            ASPIREdb.EVENT_BUS.on( 'project_selected', this.projectSelectHandler, this );

         },

         projectSelectHandler : function(selProject) {
            this.selectedProject = selProject;
            this.down( '#uploadFilesEdit' ).setDisabled( false );
         }

      } );
