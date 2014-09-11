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
      'ASPIREdb.view.UploadManagerPanel',
      {
         extend : 'Ext.form.Panel',
         alias : 'widget.uploadManagerPanel',
         frame : true,
         bodyStyle : 'padding:5px 5px 0',
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
            activeTab : 0,
            defaults : {
               bodyStyle : 'padding:10px'
            },

            items : [ {
               defaultType : 'textfield',
               width : '100%',

               items : [ {
                  xtype : 'fieldset',
                  title : 'Project',
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
                     labelWidth : 100,
                  }, {
                     fieldLabel : 'Project Description',
                     name : 'projectDescription',
                     id : 'projectDescription',
                     value : '',
                     labelWidth : 100,
                  } ]
               }, {
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
                     id : 'variantFile',
                     name : 'file',
                     width : 400,
                     allowBlanck : false,
                     emptyText : 'Select variant file to upload',
                     fieldLabel : 'File',
                     labelWidth : 100,
                     buttonText : 'Select',
                     /**handler : function() {
                        console.log( 'file upload handler ' );
                        form.submit( {

                           method : 'POST',
                           url : 'upload_action.html', // submitEmptyText : false,
                           waitMsg : 'Uploading your variant file...',
                           // headers : {
                           // 'Content-Type' : 'multipart/form-data;charset=UTF-8'
                           // 'Content-Type' :'application/x-www-form-urlencoded'
                           // },
                           success : function(form, action) {
                              Ext.Msg.alert( 'Success', 'Your variant file has been uploaded.' );
                           },
                           failure : function(form, action) {
                              Ext.Msg.alert( 'Failed', action.result ? action.result.message : 'No response' );
                           }
                        } );
                     },*/
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
                     id : 'phenotypeFile',
                     name : 'phenotypeFile',
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
                       id : 'uploadFiles',
                       handler : function() {
                          var form = this.up( 'uploadManagerPanel' ).getForm();
                          var me = this;

                          if ( form.isValid() ) {

                             // progress bar runner code

                             var Runner = function() {
                                var f = function(v, pbar, btn, count, cb) {
                                   return function() {
                                      if ( v > count ) {
                                         btn.dom.disabled = false;
                                         cb();
                                      } else {
                                         if ( pbar.id == 'pbar4' ) { // give this one a different count style for
                                            fun
                                            var i = v / count;
                                            pbar.updateProgress( i, Math.round( 100 * i ) + '% completed...' );
                                         } else {
                                            pbar.updateProgress( v / count, 'Loading item ' + v + ' of ' + count
                                               + '...' );
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
                             }();

                             var pbar4 = Ext.create( 'Ext.ProgressBar', {
                                text : 'Waiting on you...',
                                id : 'pbar4',
                                textEl : 'p4text',
                                cls : 'custom',
                                renderTo : me,
                             } );

                             var btn4 = Ext.getCmp( 'uploadFiles' );
                             console.log( 'test btn4 : ' + btn4 ); // btn4.dom.disabled = false;

                             btn4.on( 'click', function() {
                                Runner.run( pbar4, btn4, 19, function() {
                                   pbar4.updateText( 'All finished!' );
                                } );
                             } );

                             // getting the form values values = form.getFieldValues(); var projectName =
                              // values['projectName']; var
                             projectDescription = values['projectDescription'];
                             var file = Ext.getCmp( 'variantFile' ).getEl().down( 'input[type=file]' ).dom.files[0];
                             var phenotypeFile = Ext.getCmp( 'phenotypeFile' ).getEl().down( 'input[type=file]' ).dom.files[0];

                             if ( file ) {
                                var variantType = values['variantType-inputEl'].toUpperCase(); // create project
                                ProjectService
                                   .createUserProject(
                                      projectName,
                                      projectDescription,
                                      {
                                         callback : function(message) {

                                            if ( message == "Success" ) {
                                               // Uploading variants to the created project
                                               var fReader = new FileReader();
                                               fReader.readAsBinaryString( file );

                                               fReader.onloadend = function(event) {
                                                  var variantSrc = event.target.result;
                                                  // add variants to the project
                                                  ProjectService
                                                     .addSubjectVariantsToExistingProject(
                                                        variantSrc,
                                                        false,
                                                        projectName,
                                                        variantType,
                                                        {
                                                           callback : function(errorMessage) {
                                                              if ( errorMessage == 'Success' ) {
                                                                 Ext.Msg.alert( 'Success',
                                                                    'You have successfully uploaded variant file' );
                                                              } else
                                                                 Ext.Msg.alert( 'Server Reply', 'Uploading Variants :'
                                                                    + errorMessage );
                                                              Ext.getCmp( 'variantType' ).setValue( '' );
                                                              Ext.getCmp( 'variantFile' ).setRawValue( '' );

                                                              if ( phenotypeFile ) { // Uploading phenoypes to the
                                                                                       // created project var fpReader =
                                                                                       // new FileReader();
                                                                 fpReader.readAsBinaryString( phenotypeFile );

                                                                 fpReader.onloadend = function(event) {
                                                                    var variantSrc = event.target.result;
                                                                    // add variants to the project
                                                                    ProjectService
                                                                       .addSubjectPhenotypeToExistingProject(
                                                                          variantSrc,
                                                                          false,
                                                                          projectName,
                                                                          {
                                                                             callback : function(errorMessage) {
                                                                                if ( errorMessage == "Success" ) {
                                                                                   Ext.Msg
                                                                                      .alert( 'Success',
                                                                                         'You have successfully uploaded phenotype file' );
                                                                                } else
                                                                                   Ext.Msg.alert( 'Server Reply',
                                                                                      'Uploading Phenotypes :'
                                                                                         + errorMessage );
                                                                                Ext.getCmp( 'phenotypeFile' )
                                                                                   .setRawValue( '' );
                                                                             },
                                                                             errorHandler : function(er, exception) {
                                                                                Ext.Msg.alert(
                                                                                   "Upload phenotype Error", er + "\n"
                                                                                      + exception.stack );
                                                                                console.log( exception.stack );
                                                                             }
                                                                          } );
                                                                 };
                                                              }
                                                              Ext.getCmp( 'projectName' ).setValue( '' );
                                                              Ext.getCmp( 'projectDescription' ).setValue( '' );
                                                           },
                                                           errorHandler : function(er, exception) {
                                                              Ext.Msg.alert( "Upload variants Error", er + "\n"
                                                                 + exception.stack );
                                                              console.log( exception.stack );
                                                           }
                                                        } );
                                               };
                                            }
                                         },
                                         errorHandler : function(er, exception) {
                                            Ext.Msg.alert( "create project Error", er + "\n" + exception.stack );
                                            console.log( exception.stack );
                                         }
                                      } );
                             } else if ( phenotypeFile ) {
                                // Uploading phenoytypes to the created project
                                var fpReader = new FileReader();
                                fpReader.readAsBinaryString( phenotypeFile );

                                fpReader.onloadend = function(event) {
                                   var variantSrc = event.target.result;
                                   // add variants to the project
                                   ProjectService
                                      .addSubjectPhenotypeToExistingProject( variantSrc, true, projectName,
                                         {
                                            callback : function(errorMessage) {
                                               if ( errorMessage == "Success" ) {
                                                  Ext.Msg.alert( 'Success',
                                                     'You have successfully uploaded phenotype file' );
                                               } else
                                                  Ext.Msg.alert( 'Server Reply', 'Uploading Phenotypes :'
                                                     + errorMessage );
                                               Ext.getCmp( 'phenotypeFile' ).setRawValue( '' );
                                               Ext.getCmp( 'projectName' ).setValue( '' );
                                               Ext.getCmp( 'projectDescription' ).setValue( '' );
                                            },
                                            errorHandler : function(er, exception) {
                                               Ext.Msg.alert( "Upload phenotype Error", er + "\n" + exception.stack );
                                               console.log( exception.stack );
                                            }
                                         } );
                                };
                             }

                      /**       form.submit( {

                                method : 'POST',
                                url : 'upload_action.html', // submitEmptyText : false,
                                waitMsg : 'Uploading your file...',
                                headers : {
                                   'Content-Type' : 'multipart/form-data;charset=UTF-8'
                                // 'Content-Type' :'application/x-www-form-urlencoded'
                                },
                                success : function(form, action) {
                                   Ext.Msg.alert( 'Success', 'Your file has been uploaded.' );
                                },
                                failure : function(form, action) {
                                   Ext.Msg.alert( 'Failed', action.result ? action.result.message : 'No response' );
                                }
                             } );*/

                             ASPIREdb.EVENT_BUS.fireEvent( 'project_list_updated' );

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
                       xtype : 'button',
                       text : 'Cancel',
                       itemId : 'cancelButton',
                       handler : function() {
                          ASPIREdb.view.UploadDataManagerWindow.hide();
                       },
                    // scope :this,
                    } ],

         /**
          * init
          */
         initComponent : function() {
            this.callParent();

         },

      } );
