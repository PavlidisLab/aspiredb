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
   bodyPadding : 5,
   padding : '50 50 50 50',

   layout : 'anchor',
   defaults : {
      anchor : '100%'
   },
   // width: 350,
   fileUpload : true,
   config : {
      variantSrc : null,
   },
   fieldDefaults : {
      labelWidth : 75,
      msgTarget : 'side'
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
               listeners : {
                  specialkey : function(field, e) {
                     if ( e.getKey() == e.ENTER ) {
                        ref.submitHandler();
                     }
                  }
               }

            }, {
               fieldLabel : 'Project Decription',
               name : 'projectDescription',
               id : 'projectDescription',
               value : '',
               labelWidth : 150,
               listeners : {
                  specialkey : function(field, e) {
                     if ( e.getKey() == e.ENTER ) {
                        ref.submitHandler();
                     }
                  }
               }
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
               name : 'file',
               width : 600,
               allowBlanck : false,
               emptyText : 'Select variant file to upload',
               fieldLabel : 'Upload Variant List',
               labelWidth : 150,
               buttonText : 'Select',
               listeners : {
                  afterrender : function(el) {
                     var element = el.fileInputEl;
                     console.log( element );
                     return element;
                  },
                  change : function(fld, value) {
                     if ( value.indexOf( 'fakepath' ) ) {
                        var newValue = value.replace( /C:\\fakepath\\/g, '' );
                        fld.setRawValue( newValue );
                     }

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
         }, 
              { xtype : 'fieldset', title : 'Phenotypes', collapsible : true, autoWidth : true, autoheight : true,
              defaultType : 'textfield', layout : 'anchor', defaults : { anchor : '100%' }, items : [ { xtype :
              'filefield', id : 'phenotypeFile', name : 'phenotypeFile', width : 600, emptyText : 'Select phenotype
              file to upload', fieldLabel : 'Upload Phenotype List', labelWidth : 150, name :
              'phenotypeUploadFile-path', buttonText : 'Select', } ] },
             
         {
            xtype : 'label',
            itemId : 'message',
            style : 'font-family: sans-serif; font-size: 10px; color: red;',
            text : 'File Upload failed. Please check the size of the file you have uplaoded.',
            hidden : true
         } ]
      } ]
   },

   buttons : [ {
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
            var file = Ext.getCmp( 'variantFile' ).getEl().down( 'input[type=file]' ).dom.files[0];
            var variantPath = form.findField( 'variantFile' ).getValue();
            var variantDirectory = variantPath.substring( 0, variantPath.lastIndexOf( "\\" ) );
            var variantFilename = variantPath.substring( variantPath.lastIndexOf( "\\" ) + 1, variantPath.length );
            var variantType = values['variantType-inputEl'].toUpperCase();
            var variantSrc =null;
            var fReader = new FileReader();
          
            // create project
            ProjectService.createUserProject( projectName, projectDescription, {
               callback : function(projectId) {

                  console.log( 'reading uplodaed files' + file );
                 var fReader = new FileReader();
                 fReader.readAsBinaryString( file );

                  fReader.onloadend = function(event) {
                     var variantSrc = event.target.result;
                     console.log( 'reader finish reading' + variantSrc ); // add variants to the project

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
//github.com/ppavlidis/aspiredb.git

               },
               errorHandler : function(er, exception) {
                  Ext.Msg.alert( "create project Error", er + "\n" + exception.stack );
                  console.log( exception.stack );
               }
            } );

            form.submit( {
               clientValidation : true,
               scope : this,
               params : {
                  newStatus : 'delivered'
               },
               method : 'POST',
               url : '/aspiredb/upload_action.html', // submitEmptyText : false,

               waitMsg : 'Uploading your file...',
               success : function(form, action) {
                  Ext.Msg.alert( 'Success', 'Your file has been uploaded.' );
               },
               failure : function(form, action) {
                  Ext.Msg.alert( 'Failed', action.result ? action.result.message : 'No response' );
               }
            } );

            /**
             * var variantPath = form.findField( 'variantFile' ).getValue(); var variantDirectory =
             * variantPath.substring( 0, variantPath.lastIndexOf( "\\" ) ); var variantFilename = variantPath.substring(
             * variantPath.lastIndexOf( "\\" ) + 1, variantPath.length ); var variantType =
             * values['variantType-inputEl'].toUpperCase(); var file = Ext.getCmp( 'variantFile' ).getEl().down(
             * 'input[type=file]' ).dom.files[0]; // variantType.setValue(values['variantType-inputEl']); //
             * variantType.setValue( values['variantType-inputEl'] ); // var phenotypePath = form.findField(
             * 'phenotypeUploadFile-path' ).getValue(); // var phenotypeDirectory = phenotypePath.substring( 0,
             * phenotypePath.lastIndexOf( "\\" ) ); // var phenotypeFilename = phenotypePath.substring(
             * phenotypePath.lastIndexOf( "\\" ) + 1, // phenotypePath.length );
             * 
             * var fReader = new FileReader(); // fReader.readAsDataURL( file ); fReader.readAsBinaryString( file );
             * 
             * fReader.onloadend = function(event) { var variantSrc = event.target.result; console.log( 'reader finish
             * reading' + variantSrc );
             *  // add variants to the project ProjectService.addSubjectVariantsToExistingProject( variantSrc,
             * projectName, variantType, { callback : function(errorMessage) {
             * 
             * Ext.Msg.alert( 'Error', 'Add to variant DWR returns Error :' + errorMessage );
             *  }, errorHandler : function(er, exception) { Ext.Msg.alert( "create variant Error", er + "\n" +
             * exception.stack ); console.log( exception.stack ); } } ); }
             */
>>>>>>> branch 'master' of https://github.com/ppavlidis/aspiredb.git

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
      text : 'Cancel'
   } ],

   initComponent : function() {
      this.callParent();

      var ref = this;
   },

   submitHandler : function() {

      var me = this;
      Ext.Ajax.request( {
         url : 'j_spring_security_check',
         method : 'POST',
         headers : {
            'Content-Type' : 'application/x-www-form-urlencoded'
         },
         scope : me,
         params : Ext.Object.toQueryString( {
            'j_file' : file,
            'j_projectName' : projectName,
            'j_variantType' : 'CNV',

         } ),
         success : function(response) {
            var messageLabel = me.down( '#message' );
            var usernameTextfield = me.down( '#username' );
            var passwordTextfield = me.down( '#password' );

            usernameTextfield.reset();
            passwordTextfield.reset();

            if ( response.responseText === 'success' ) {
               window.location.href = "home.html";
               messageLabel.hide();
            } else {
               messageLabel.show();
            }
         },
         failure : function(response, opts) {
            var messageLabel = me.down( '#message' );
            messageLabel.show();
         }
      } );

   }

} );
