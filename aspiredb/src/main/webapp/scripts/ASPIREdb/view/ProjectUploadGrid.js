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
            variantFileEdit : '',
            phenotypeFileEdit : '',
            variantTypeEdit : '',
            variantServerFilename : '',
            phenotypeServerFilename : '',
         },
         fieldDefaults : {
            labelWidth : 75,
            msgTarget : 'side'
         },
         defaults : {
            anchor : '100%'
         },

         items : [ {
            xtype : 'label',
            html : "For help with the data file format, see <a target='_blank' href='http://aspiredb.chibi.ubc.ca/data-loaders-and-admin-tools-setup/data-loaders-2/'>this page</a>.",
         } ],

         buttons : [
                    {
                       text : 'Submit',
                       id : 'uploadFilesEdit',
                       disabled : false,
                       // formBind : true,
                       handler : function() {
                          var form = this.up( 'ProjectUploadGrid' ).getForm();
                          var me = this;

                          if ( form.isValid() ) {

                             Ext.Msg
                                .show( {
                                   title : '',
                                   msg : 'Your project has been submitted.<br/>You will receive an e-mail once the upload is complete.<br/>',
                                   width : 400,
                                   buttons : Ext.MessageBox.OK,
                                } )

                             // getting the form values
                             values = form.getFieldValues();
                             var variantfilename = me.up( "ProjectUploadGrid" ).variantServerFilename;
                             var phenotypefilename = me.up( "ProjectUploadGrid" ).phenotypeServerFilename;

                             if ( Ext.getCmp( 'ProjectUploadGrid' ).selectedProject == null ) {
                                Ext.Msg.alert( 'Error', "Please select a project name" );
                                return;
                             }

                             var projectName = Ext.getCmp( 'ProjectUploadGrid' ).selectedProject[0].data.ProjectName;

                             if ( variantfilename.length == 0 && phenotypefilename.length == 0 ) {
                                Ext.Msg.alert( 'Error', "Either a variant and / or <br>phenotype file is required." );
                                return;
                             }

                             var variantTypeEdit = form.owner.variantTypeEdit;
                             /** Uploading variants to the created project */

                             if ( variantfilename.length != 0 && variantTypeEdit.length == 0 ) {
                                Ext.Msg.alert( 'Error', "Please select a variant type" );
                                return;

                             }

                             var clearFilenames = function() {
                                me.up( "ProjectUploadGrid" ).variantServerFilename = "";
                                me.up( "ProjectUploadGrid" ).phenotypeServerFilename = "";
                             }

                             // commented out handlers so users can continue using ASPIREdb
                             // an e-mail will be sent when the upload has finished.

                             // me.up( "ProjectUploadGrid" ).setLoading( true );
                             ProjectService.addSubjectVariantsPhenotypeToProject( variantfilename, phenotypefilename,
                                false, projectName, variantTypeEdit
                             /*
                               * ,{ callback : function(message) { // this can be a long message, break it up with <br>
                               * //Ext.Msg.alert( 'Result', message.replace(/\n/g,'<br>').replace(/ /g,'&nbsp;') );
                               * console.log( message ); Ext.Msg.alert( 'Result', '<pre>' + message + '</pre>' );
                               * 
                               * ASPIREdb.EVENT_BUS.fireEvent( 'project_list_updated' ); me.up( "ProjectUploadGrid"
                               * ).setLoading( false ); }, errorHandler : function(er, exception) { Ext.Msg.alert(
                               * 'Error', er + "\n" + exception.stack ); console.log( exception.stack ); me.up(
                               * "ProjectUploadGrid" ).setLoading( false ); }
                               */
                             // }
                             );

                             ASPIREdb.EVENT_BUS.fireEvent( 'project_list_updated' );
                             clearFilenames();

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
            var ref = this;
            ASPIREdb.EVENT_BUS.on( 'project_selected', this.projectSelectHandler, this );

            var variantPanel = Ext
               .create(
                  'Ext.form.Panel',
                  {
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
                        fieldLabel : 'Variant Type',
                        id : 'variantTypeEdit',
                        editable : false,
                        displayField : 'variantTypeEdit',
                        valueField : 'id',
                        listeners : {
                           afterrender : function() {
                              // select the first item by default
                              this.setValue( this.getStore().getAt( '0' ).get( 'id' ) );
                           }
                        },
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

                     } ],

                     buttons : [ {
                        id : 'uploadVariantFilesEdit',
                        text : 'Upload Variants',
                        handler : function() {
                           var form = variantPanel;
                           var me = this;
                           values = form.getValues();
                           ref.variantTypeEdit = values['variantTypeEdit-inputEl'].toUpperCase();
                           var file = Ext.getCmp( 'variantFileEdit' ).getEl().down( 'input[type=file]' ).dom.files[0];

                           if ( file == null ) {
                              Ext.MessageBox.alert( 'Invalid Fields', 'File is required' );
                              return;
                           }

                           ref.variantFileEdit = file.name;

                           if ( form.isValid() ) {

                              Ext.MessageBox
                                 .alert( 'Success',
                                    'Your project has been submitted<br/>. We will send you an e-mail once the upload is complete.<br/>' );

                              form.submit( {
                                 method : 'POST',
                                 url : 'upload_action.html',
                                 waitMsg : 'Uploading your variant file...',
                                 headers : {
                                    'Content-Type' : 'multipart/form-data;charset=UTF-8'
                                 },

                                 success : function(form, action) {
                                    var fReader = new FileReader();
                                    fReader.readAsBinaryString( file );

                                    // cache the server file name
                                    me.up( "ProjectUploadGrid" ).variantServerFilename = action.result.data.filePath;

                                    var variantFilename = action.result.data.filePath;
                                    var createProject = false;
                                    var projectName = "testVariant";
                                    var variantType = ref.variantTypeEdit
                                    var dryRun = true;
                                    ProjectService.addSubjectVariantsToProject( variantFilename, createProject,
                                       projectName, variantType, dryRun, {
                                          callback : function(result) {

                                             if ( result.errorMessages.length > 0 ) {
                                                Ext.Msg.alert( 'Error', 'Your file has failed with these errors: <br/>'
                                                   + result.errorMessages.slice( 0, 5 ).join( '<br/>' ) );
                                             } else {
                                                Ext.Msg.alert( 'Success',
                                                   'Your file is ready to be submitted <br/><br/> Name: '
                                                      + ref.variantFileEdit + '<br/> Variant type: '
                                                      + ref.variantTypeEdit + '<br/> # Variants : '
                                                      + result.variantsToAdd.length + '<br/><br/>' );
                                             }

                                          }
                                       } );

                                 },
                                 failure : function(form, action) {
                                    Ext.Msg.alert( 'Failed', action.result ? action.result.message : 'No response' );
                                 }

                              } );

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
                  id : 'phenotypeFileEdit',
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
                  text : 'Upload Phenotypes',
                  handler : function() {
                     var form = phenotypePanel;
                     var me = this;
                     var file = Ext.getCmp( 'phenotypeFileEdit' ).getEl().down( 'input[type=file]' ).dom.files[0];

                     if ( file == null ) {
                        Ext.MessageBox.alert( 'Invalid Fields', 'File is required' );
                        return;
                     }

                     ref.phenotypeFileEdit = file.name;

                     if ( form.isValid() ) {

                        form.submit( {

                           method : 'POST',
                           url : 'upload_action.html', // submitEmptyText : false,
                           waitMsg : 'Uploading your phenotype file...',
                           headers : {
                              'Content-Type' : 'multipart/form-data;charset=UTF-8'
                           },
                           success : function(form, action) {
                              var fReader = new FileReader();
                              fReader.readAsBinaryString( file );

                              // cache the uploaded file's absolute server file path
                              me.up( "ProjectUploadGrid" ).phenotypeServerFilename = action.result.data.filePath;

                              var phenotypeFilename = action.result.data.filePath;
                              var createProject = false;
                              var projectName = "testPhenotype";
                              var dryRun = true;
                              ProjectService.addSubjectPhenotypeToProject( phenotypeFilename, createProject,
                                 projectName, dryRun, {
                                    callback : function(result) {

                                       if ( result.errorMessages.length > 0 ) {
                                          Ext.Msg.alert( 'Error', 'Your file has failed with these errors: <br/>'
                                             + result.errorMessages.slice( 0, 5 ).join( '<br/>' ) );
                                       } else {
                                          Ext.Msg.alert( 'Success',
                                             'Your file is ready to be submitted <br/><br/> Name: '
                                                + ref.variantFileEdit + '<br/> # Phenotypes : '
                                                + result.phenotypesToAdd.length + '<br/><br/>' );
                                       }

                                    }
                                 } );

                           },
                           failure : function(form, action) {
                              Ext.Msg.alert( 'Failed', action.result ? action.result.message : 'No response' );
                           }
                        } );

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
               } ]
            } );

            this.add( phenotypePanel );

         },

         projectSelectHandler : function(selProject) {
            this.selectedProject = selProject;
            this.down( '#uploadFilesEdit' ).setDisabled( false );
         }

      } );
