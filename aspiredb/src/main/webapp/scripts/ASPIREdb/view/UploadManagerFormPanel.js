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
Ext.require( [ 'Ext.form.*', 'Ext.layout.container.Column' ] );
/**
 * Upload Panel
 */
Ext.define( 'ASPIREdb.view.uploadManagerFormPanel', {
   extend : 'Ext.form.Panel',
   alias : 'widget.uploadManagerFormPanel',
   frame : true,
   bodyStyle : 'padding:5px 5px 0',
   fileUpload : true,
   config : {
      variantSrc : null,
      VariantFile :null,
      PhenotypeFile:null,
   },
   fieldDefaults : {
      labelWidth : 75,
      msgTarget : 'side'
   },
   defaults : {
      anchor : '100%'
   },

   items : [ {
      xtype : 'textfield',
      fieldLabel : 'Project Name',
      name : 'projectName',
      itemId : 'projectName',
      value : '',
      labelWidth : 200,
   }, {
      xtype : 'textfield',
      fieldLabel : 'Project Decription',
      name : 'projectDescription',
      itemId : 'projectDescription',
      value : '',
      labelWidth : 200,
   } ],

   buttons : [ {
      text : 'Submit',
      id : 'submitFiles',
      handler : function() {
         var form = this.up( 'uploadManagerFormPanel' ).getForm();
         var me = this;

         if ( form.isValid() ) {

            // getting the form values
            values = form.getFieldValues();
            var projectName = values['projectName'];
            var projectDescription = values['projectDescription'];
            var file = Ext.getCmp( 'variantFile' ).getEl().down( 'input[type=file]' ).dom.files[0];
            var phenotypeFile = Ext.getCmp( 'phenotypeFile' ).getEl().down( 'input[type=file]' ).dom.files[0];
            
            if ( file ) {
               var variantType = values['variantType-inputEl'].toUpperCase();
               // create project
               ProjectService.createUserProject( projectName, projectDescription, {
                  callback : function(message) {

                     if ( message == "Success" ) {
                        // add variants to the project
                        ProjectService.addSubjectVariantsToProject( file.name, false, projectName, variantType, {
                           callback : function(errorMessage) {
                              if ( errorMessage == 'Success' ) {
                                 Ext.Msg.alert( 'Success', 'You have successfully uploaded variant file' );
                              } else
                                 Ext.Msg.alert( 'Server Reply', 'Uploading Variants  :' + errorMessage );
                              Ext.getCmp( 'variantType' ).setValue( '' );
                              Ext.getCmp( 'variantFile' ).setRawValue( '' );

                              if ( phenotypeFile ) {
                                 // Uploading phenotypes to the created project
                                 ProjectService.addSubjectPhenotypeToProject( phenotypeFile.name, false, projectName, {
                                    callback : function(errorMessage) {
                                       if ( errorMessage == "Success" ) {
                                          Ext.Msg.alert( 'Success', 'You have successfully uploaded phenotype file' );
                                       } else
                                          Ext.Msg.alert( 'Server Reply', 'Uploading Phenotypes :' + errorMessage );
                                       Ext.getCmp( 'phenotypeFile' ).setRawValue( '' );

                                    },
                                    errorHandler : function(er, exception) {
                                       Ext.Msg.alert( "Upload phenotype Error", er + "\n" + exception.stack );
                                       console.log( exception.stack );
                                    }
                                 } );

                              }
                              Ext.getCmp( 'projectName' ).setValue( '' );
                              Ext.getCmp( 'projectDescription' ).setValue( '' );

                           },
                           errorHandler : function(er, exception) {
                              Ext.Msg.alert( "Upload variants Error", er + "\n" + exception.stack );
                              console.log( exception.stack );
                           }
                        } );

                     }

                  },
                  errorHandler : function(er, exception) {
                     Ext.Msg.alert( "create project Error", er + "\n" + exception.stack );
                     console.log( exception.stack );
                  }
               } );
            }

            else if ( phenotypeFile ) {
               // Uploading phenoytypes to the created project
               ProjectService.addSubjectPhenotypeToProject( phenotypeFile.name, true, projectName, {
                  callback : function(errorMessage) {
                     if ( errorMessage == "Success" ) {
                        Ext.Msg.alert( 'Success', 'You have successfully uploaded phenotype file' );
                     } else
                        Ext.Msg.alert( 'Server Reply', 'Uploading Phenotypes :' + errorMessage );
                     Ext.getCmp( 'phenotypeFile' ).setRawValue( '' );
                     Ext.getCmp( 'projectName' ).setValue( '' );
                     Ext.getCmp( 'projectDescription' ).setValue( '' );
                  },
                  errorHandler : function(er, exception) {
                     Ext.Msg.alert( "Upload phenotype Error", er + "\n" + exception.stack );
                     console.log( exception.stack );
                  }
               } );

            }

            ASPIREdb.EVENT_BUS.fireEvent( 'new_project_created' );

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
            Ext.MessageBox.alert( 'Invalid Fields', 'The following fields are invalid: ' + fieldNames.join( ', ' ) );
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
      var ref = this;

      var variantPanel = Ext.create( 'Ext.form.Panel', {
         id : 'variantUploadForm',
         title : 'Upload variants',
         width : '100%',
         bodyPadding : 5,
         padding : '10 10 10 10',

         layout : 'anchor',
         defaults : {
            anchor : '100%'
         },

         // The fields
         defaultType : 'textfield',
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

         } ],

         buttons : [ {
            id : 'uploadVariantFiles',
            text : 'Uplaod Variants',
            handler : function() {
               var form = variantPanel.getForm();
               var me = this;

               if ( form.isValid() ) {
                  form.submit( {
                     method : 'POST',
                     url : 'upload_action.html',
                     waitMsg : 'Uploading your variant file...',
                     headers : {
                        'Content-Type' : 'multipart/form-data;charset=UTF-8'
                     },
                     success : function(form, action) {
                        Ext.Msg.alert( 'Success', 'Your file has been uploaded.' );                        
                     },
                     failure : function(form, action) {
                        Ext.Msg.alert( 'Failed', action.result ? action.result.message : 'No response' );
                     }
                  } );

                  ASPIREdb.EVENT_BUS.fireEvent( 'new_project_created' );

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
                  Ext.MessageBox
                     .alert( 'Invalid Fields', 'The following fields are invalid: ' + fieldNames.join( ', ' ) );
               }
            }
         } ]
      } );

      this.add( variantPanel );

      var phenotypePanel = Ext.create( 'Ext.form.Panel', {
         id : 'phenotypeUploadForm',
         title : 'Upload phenotypes',
         bodyPadding : 5,
         padding : '10 10 10 10',

         layout : 'anchor',
         defaults : {
            anchor : '100%'
         },

         // The fields
         defaultType : 'textfield',
         items : [ {
            xtype : 'filefield',
            id : 'phenotypeFile',
            name : 'file',
            width : 400,
            allowBlanck : false,
            emptyText : 'Select phenotype file to upload',
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
         } ],

         buttons : [ {
            // xtype : 'Upload',
            id : 'UploadPhenotypeFiles',
            text : 'Uplaod Phenotypes',
            handler : function() {
               var form = phenotypePanel.getForm();
               var me = this;

               if ( form.isValid() ) {
                  form.submit( {

                     method : 'POST',
                     url : 'upload_action.html', // submitEmptyText : false,
                     waitMsg : 'Uploading your phenotype file...',
                     headers : {
                        'Content-Type' : 'multipart/form-data;charset=UTF-8'
                     },
                     success : function(form, action) {
                        Ext.Msg.alert( 'Success', 'Your file has been uploaded.' );
                     },
                     failure : function(form, action) {
                        Ext.Msg.alert( 'Failed', action.result ? action.result.message : 'No response' );
                     }
                  } );

                  ASPIREdb.EVENT_BUS.fireEvent( 'new_project_created' );

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
                  Ext.MessageBox
                     .alert( 'Invalid Fields', 'The following fields are invalid: ' + fieldNames.join( ', ' ) );
               }
            }
         } ]
      } );

      this.add( phenotypePanel );

   },

} );
