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
package ubc.pavlab.aspiredb.client.view;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sencha.gxt.widget.core.client.Window;
import ubc.pavlab.aspiredb.shared.*;
import ubc.pavlab.aspiredb.shared.query.*;

import java.util.*;

public class TextDataDownloadWindow extends Window {

    public TextArea textArea = new TextArea();

    public Button selectAllButton;

    VerticalPanel panel;

    public TextDataDownloadWindow() {

    }

    public void showVariantsDownload( List<VariantValueObject> vvoList, Collection<Property> properties ) {

        StringBuffer text = new StringBuffer();

        ArrayList<Property> propertyList = new ArrayList<Property>();

        text.append( "Subject Id\t" );
        text.append( "Type\t" );
        text.append( "Genome Coordinates\t" );

        for ( Property p : properties ) {
            propertyList.add( p );
            text.append( p.getDisplayName() + "\t" );
        }

        text.append( "\n" );
        for ( VariantValueObject vvo : vvoList ) {

            text.append( vvo.getPatientId() + "\t" );
            text.append( vvo.getVariantType() + "\t" );
            text.append( vvo.getGenomeCoordinates() + "\t" );

            // Add Characteristics
            Map<String, CharacteristicValueObject> charMap = vvo.getCharacteristics();

            for ( Property p : propertyList ) {

                CharacteristicValueObject cvo = charMap.get( p.getDisplayName() );

                if ( cvo != null ) {
                    text.append( cvo.getValue() );
                } else {

                    if ( vvo instanceof CNVValueObject ) {

                        CNVValueObject cnv = ( CNVValueObject ) vvo;
                        if ( p instanceof CopyNumberProperty ) {
                            text.append( cnv.getCopyNumber() == null ? "" : cnv.getCopyNumber() );
                        } else if ( p instanceof CNVTypeProperty ) {
                            text.append( cnv.getType() == null ? "" : cnv.getType() );
                        }

                    } else if ( vvo instanceof SNVValueObject ) {

                        SNVValueObject snv = ( SNVValueObject ) vvo;
                        if ( p instanceof ReferenceBaseProperty ) {
                            text.append( snv.getReferenceBase() == null ? "" : snv.getReferenceBase() );
                        } else if ( p instanceof ObservedBaseProperty ) {
                            text.append( snv.getObservedBase() == null ? "" : snv.getObservedBase() );
                        } else if ( p instanceof DbSnpIdProperty ) {
                            text.append( snv.getDbSNPID() == null ? "" : snv.getDbSNPID() );
                        }

                    } else if ( vvo instanceof IndelValueObject ) {
                        IndelValueObject indel = ( IndelValueObject ) vvo;

                        if ( p instanceof IndelLengthProperty ) {
                            text.append( indel.getLength() );
                        }

                    }
                }

                text.append( "\t" );
            }

            text.append( "\n" );
        }

        makeWindow( text.toString() );

    }

    public void showSubjectDownload( List<SubjectValueObject> svoList ) {

        StringBuffer text = new StringBuffer();
        text.append( "Subject Id" );

        text.append( "\n" );
        for ( SubjectValueObject svo : svoList ) {

            text.append( svo.getPatientId() );
            text.append( "\n" );
        }

        makeWindow( text.toString() );

    }
    
    public void showGenesDownload( List<GeneValueObject> gvoList ) {

        StringBuffer text = new StringBuffer();
        text.append( "Gene Symbol\t" );
        text.append( "Type\t");
        text.append( "Gene Name" );

        text.append( "\n" );
        for ( GeneValueObject gvo : gvoList ) {

            text.append(gvo.getSymbol()+"\t" );
            text.append(gvo.getGeneBioType()+"\t" );
            text.append(gvo.getName()+"\t" );
            text.append( "\n" );
        }

        makeWindow( text.toString() );

    }
    
    public void showPhenotypeEnrichmentDownload(List<PhenotypeEnrichmentValueObject> pvoList){
        
        StringBuffer text = new StringBuffer();
        text.append( "Name\t" );
        text.append( "uri\t");
        text.append( "In Group\t" );
        text.append( "Out Group\t" );        
        text.append( "pValue\t" );
        text.append( "Corrected pValue\t" );

        text.append( "\n" );
        for ( PhenotypeEnrichmentValueObject pvo : pvoList ) {

            text.append(pvo.getName()+"\t" );
            text.append(pvo.getUri()+"\t" );
            text.append(pvo.getInGroupTotalString()+"\t" );
            text.append(pvo.getOutGroupTotalString()+"\t" );            
            text.append(pvo.getPValueString()+"\t" );
            text.append(pvo.getPValueCorrectedString()+"\t" );
            text.append( "\n" );
        }

        makeWindow( text.toString() );
        
    }

    public void showPhenotypesDownload( List<SubjectValueObject> svoList ) {

        StringBuffer text = new StringBuffer();

        LinkedHashMap<String, String> phenotypeFileColumnsMap = new LinkedHashMap<String, String>();

        for ( SubjectValueObject svo : svoList ) {

            for ( PhenotypeValueObject pvo : svo.getPhenotypes().values() ) {

                String columnName = pvo.getUri()!=null ? (pvo.getUri() +":"+pvo.getName() ): pvo
                        .getName();

                if ( !phenotypeFileColumnsMap.containsKey( columnName ) ) {
                    phenotypeFileColumnsMap.put( columnName, pvo.getName() );
                }

            }

        }

        text.append( "Subject Id\t" );

        for ( String columnName : phenotypeFileColumnsMap.keySet() ) {
            text.append( columnName + "\t" );
        }

        text.append( "\n" );
        for ( SubjectValueObject svo : svoList ) {

            text.append( svo.getPatientId() + "\t" );
            Map<String, PhenotypeValueObject> phenotypeMap = svo.getPhenotypes();

            for ( String columnName : phenotypeFileColumnsMap.keySet() ) {
                
                PhenotypeValueObject vo = phenotypeMap.get( phenotypeFileColumnsMap.get( columnName ) );
                
                if (vo !=null){                
                    text.append( vo.getDbValue() + "\t" );
                }
            }

            text.append( "\n" );
        }

        makeWindow( text.toString() );

    }

    public void makeWindow( String text ) {

        panel = new VerticalPanel();
        panel.setHeight( "500px" );
        panel.setWidth( "800px" );

        panel.add( textArea );

        textArea.setText( text );
        textArea.setHeight( "500px" );
        textArea.setWidth( "800px" );

        selectAllButton = new Button( "Select All", new ClickHandler() {
            public void onClick( ClickEvent event ) {
                textArea.selectAll();
            }
        } );

        panel.add( selectAllButton );

        this.add( panel );

        this.show();

    }

}
