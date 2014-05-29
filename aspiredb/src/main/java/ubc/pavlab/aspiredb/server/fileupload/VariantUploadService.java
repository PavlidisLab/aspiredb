/*
 * The aspiredb project
 * 
 * Copyright (c) 2013 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubc.pavlab.aspiredb.server.fileupload;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubc.pavlab.aspiredb.cli.InvalidDataException;
import ubc.pavlab.aspiredb.server.model.CnvType;
import ubc.pavlab.aspiredb.server.util.ConfigUtils;
import ubc.pavlab.aspiredb.shared.CNVValueObject;
import ubc.pavlab.aspiredb.shared.CharacteristicValueObject;
import ubc.pavlab.aspiredb.shared.GenomicRange;
import ubc.pavlab.aspiredb.shared.IndelValueObject;
import ubc.pavlab.aspiredb.shared.InversionValueObject;
import ubc.pavlab.aspiredb.shared.SNVValueObject;
import ubc.pavlab.aspiredb.shared.VariantType;
import ubc.pavlab.aspiredb.shared.VariantValueObject;

public class VariantUploadService {

    protected static Log log = LogFactory.getLog( VariantUploadService.class );

    public static VariantUploadServiceResult makeVariantValueObjectsFromResultSet( ResultSet results,
            VariantType variantType ) throws Exception {

        ArrayList<VariantValueObject> variantsToAdd = new ArrayList<VariantValueObject>();
        int lineNumber = 1;
        ArrayList<String> errorMessages = new ArrayList<String>();

        // TODO maybe validate columns first, and bail if they are all not there
        while ( results.next() ) {
            lineNumber++;
            try {
                variantsToAdd.add( makeVariantValueObjectFromResultSet( results, variantType ) );
            } catch ( InvalidDataException e ) {
                errorMessages.add( "Invalid data on line number: " + lineNumber + " error message:" + e.getMessage() );
            } catch ( NumberFormatException e ) {
                errorMessages.add( "Invalid data on line number: " + lineNumber + " error message:" + e.getMessage() );
            } catch ( SQLException e ) {
                errorMessages.add( "Invalid data format on line number: " + lineNumber + " error message:"
                        + e.getMessage() );
            } catch ( Exception e ) {
                errorMessages.add( "Error on line number: " + lineNumber + " error message:" + e.getMessage() );
            }
        }

        VariantUploadServiceResult serviceResult = new VariantUploadServiceResult( variantsToAdd, errorMessages );

        return serviceResult;

    }

    // Decipher gave us a weird file, this may only need to be done as a one off
    public static VariantUploadServiceResult makeVariantValueObjectsFromDecipherResultSet( ResultSet results )
            throws Exception {

        ArrayList<VariantValueObject> variantsToAdd = new ArrayList<VariantValueObject>();
        int lineNumber = 1;
        ArrayList<String> errorMessages = new ArrayList<String>();

        // TODO maybe validate columns first, and bail if they are all not there
        while ( results.next() ) {
            lineNumber++;
            try {
                variantsToAdd.add( makeVariantValueObjectFromResultSet( results, VariantType.DECIPHER ) );
            } catch ( InvalidDataException e ) {
                errorMessages.add( "Invalid data on line number: " + lineNumber + " error message:" + e.getMessage() );
            } catch ( NumberFormatException e ) {
                errorMessages.add( "Invalid data on line number: " + lineNumber + " error message:" + e.getMessage() );
            } catch ( SQLException e ) {
                errorMessages.add( "Invalid data format on line number: " + lineNumber + " error message:"
                        + e.getMessage() );
            } catch ( Exception e ) {
                errorMessages.add( "Error on line number: " + lineNumber + " error message:" + e.getMessage() );
            }
        }

        VariantUploadServiceResult serviceResult = new VariantUploadServiceResult( variantsToAdd, errorMessages );

        return serviceResult;

    }

    // DGV file parser
    public static VariantUploadServiceResult makeVariantValueObjectsFromDGVResultSet( ResultSet results )
            throws Exception {

        ArrayList<VariantValueObject> variantsToAdd = new ArrayList<VariantValueObject>();
        int lineNumber = 1;
        ArrayList<String> errorMessages = new ArrayList<String>();

        // TODO maybe validate columns first, and bail if they are all not there
        while ( results.next() ) {
            lineNumber++;
            try {
                variantsToAdd.add( makeVariantValueObjectFromResultSet( results, VariantType.DGV ) );
            } catch ( InvalidDataException e ) {
                errorMessages.add( "Invalid data on line number: " + lineNumber + " error message:" + e.getMessage() );
            } catch ( NumberFormatException e ) {
                errorMessages.add( "Invalid data on line number: " + lineNumber + " error message:" + e.getMessage() );
            } catch ( SQLException e ) {
                errorMessages.add( "Invalid data format on line number: " + lineNumber + " error message:"
                        + e.getMessage() );
            } catch ( Exception e ) {

                // should just be ignored data
                // errorMessages.add( "Error on line number: " + lineNumber + " error message:" + e.getMessage() );
            }
        }

        VariantUploadServiceResult serviceResult = new VariantUploadServiceResult( variantsToAdd, errorMessages );

        return serviceResult;

    }

    public static VariantValueObject makeVariantValueObjectFromResultSet( ResultSet results, VariantType variantType )
            throws Exception {

        if ( variantType.equals( VariantType.CNV ) ) {
            return makeCNVFromResultSet( results );
        } else if ( variantType.equals( VariantType.SNV ) ) {
            return makeSNVFromResultSet( results );
        } else if ( variantType.equals( VariantType.INDEL ) ) {
            return makeIndelFromResultSet( results );
        } else if ( variantType.equals( VariantType.INVERSION ) ) {
            return makeInversionFromResultSet( results );
        } else if ( variantType.equals( VariantType.DECIPHER ) ) {
            return makeDecipherCNVFromResultSet( results );
        } else if ( variantType.equals( VariantType.DGV ) ) {
            return makeDGVCNVFromResultSet( results );
        } else {
            log.error( "VariantType not supported" );
            throw new InvalidDataException( "VariantType not supported" );
        }

    }

    public static CNVValueObject makeCNVFromResultSet( ResultSet results ) throws Exception {

        String cnvType = ConfigUtils.getString( "aspiredb.cli.variant.cnv.type" );

        CNVValueObject cnv = new CNVValueObject();

        cnv.setPatientId( results.getString( CommonVariantColumn.SUBJECTID.key ) );
        cnv.setGenomicRange( getGenomicRangeFromResultSet( results ) );
        cnv.setCnvLength( cnv.getGenomicRange().getBaseEnd() - cnv.getGenomicRange().getBaseStart() );

        cnv.setType( results.getString( cnvType ) );

        if ( !cnv.getType().toUpperCase().equals( CnvType.GAIN.name() )
                && !cnv.getType().toUpperCase().equals( CnvType.LOSS.name() ) ) {
            throw new InvalidDataException( "invalid " + cnvType + ":" + cnv.getType() );
        }

        populateOptionalVariantColumns( results, cnv );
        populateOptionalCNVColumns( results, cnv );

        ArrayList<String> reservedCNVColumns = getReservedVariantColumns();

        reservedCNVColumns.add( cnvType );
        reservedCNVColumns.addAll( OptionalCNVColumn.getOptionalCNVColumnNames() );

        cnv.setCharacteristics( getCharacteristicsFromResultSet( results, reservedCNVColumns ) );

        return cnv;

    }

  
    // Quick and dirty method to grab data from decipher's poorly formatted data file they gave us
    // no point in making this pretty because of the one off nature of the file
    public static CNVValueObject makeDecipherCNVFromResultSet( ResultSet results ) throws Exception {

        // RGB colour for browser display - this will tell us if it's a GAIN or LOSS: 255,0,0 is for LOSS and 0,0,255 is
        // GAIN
        String rgbTypeString = "type";

        CNVValueObject cnv = new CNVValueObject();

        cnv.setPatientId( results.getString( CommonVariantColumn.SUBJECTID.key ) );
        cnv.setGenomicRange( getGenomicRangeFromResultSet( results ) );
        cnv.setCnvLength( cnv.getGenomicRange().getBaseEnd() - cnv.getGenomicRange().getBaseStart() );

        if ( results.getString( rgbTypeString ).equals( "255,0,0" ) ) {
            cnv.setType( CnvType.LOSS.name() );
        } else if ( results.getString( rgbTypeString ).equals( "0,0,255" ) ) {
            cnv.setType( CnvType.GAIN.name() );
        }

        if ( !cnv.getType().toUpperCase().equals( CnvType.GAIN.name() )
                && !cnv.getType().toUpperCase().equals( CnvType.LOSS.name() ) ) {
            throw new InvalidDataException( "invalid type:" + cnv.getType() );
        }

        String html = results.getString( "html" );

        String[] characteristicsAndPhenotypes = html.split( "<p>" );

        Map<String, CharacteristicValueObject> characteristics = new HashMap<String, CharacteristicValueObject>();

        for ( String entry : characteristicsAndPhenotypes ) {

            if ( entry.contains( "Inheritance" ) ) {

                entry = entry.replaceAll( "Inheritance:", "" );

                entry = entry.replaceAll( "</p>", "" );

                CharacteristicValueObject charVO = new CharacteristicValueObject();
                charVO.setKey( "Inheritance" );
                charVO.setValue( entry.trim() );
                characteristics.put( charVO.getKey(), charVO );

            } else if ( entry.contains( "Mean Ratio" ) ) {
                entry = entry.replaceAll( "Mean Ratio:", "" );
                entry = entry.replaceAll( "</p>", "" );

                CharacteristicValueObject charVO = new CharacteristicValueObject();
                charVO.setKey( "Mean Ratio" );
                charVO.setValue( entry.trim() );
                characteristics.put( charVO.getKey(), charVO );
            }
        }

        cnv.setCharacteristics( characteristics );

        return cnv;

    }

    // Quick and dirty method to grab data from DGV data file they gave us
    public static CNVValueObject makeDGVCNVFromResultSet( ResultSet results ) throws Exception {

        String variantsubtype = results.getString( "variantsubtype" );

        // Sanja says to exclude these
        if ( variantsubtype.equals( "Complex" ) || variantsubtype.equals( "Inversion" )
                || variantsubtype.equals( "Insertion" ) ) {

            log.info( "ignored variant subtype" );
            throw new Exception( "ignored variant subtype" );
        }

        CNVValueObject cnv = new CNVValueObject();

        // set patientId as variantid, so each variant will have a 'subject' associated with it
        // might not be right choice, other option would be all variants with one subject, or no subject at all
        cnv.setPatientId( results.getString( "variantaccession" ) );
        cnv.setUserVariantId( results.getString( "variantaccession" ) );
        cnv.setGenomicRange( getGenomicRangeFromResultSet( results ) );
        cnv.setCnvLength( cnv.getGenomicRange().getBaseEnd() - cnv.getGenomicRange().getBaseStart() );

        if ( variantsubtype.equals( "Gain" ) || variantsubtype.equals( "Duplication" ) ) {
            cnv.setType( CnvType.GAIN.name() );
        } else if ( variantsubtype.equals( "Loss" ) || variantsubtype.equals( "Deletion" ) ) {
            cnv.setType( CnvType.LOSS.name() );
        } else if ( variantsubtype.equals( "Gain+Loss" ) ) {
            cnv.setType( CnvType.GAINLOSS.name() );
        } else if ( variantsubtype.equals( "CNV" ) ) {
            cnv.setType( CnvType.UNKNOWN.name() );
        } else {

            log.info( "unrecognized variant subtype: " + variantsubtype );
            throw new Exception( "unrecognized variant subtype" );
        }

        // Just add pubmedid for now
        Map<String, CharacteristicValueObject> characteristics = new HashMap<String, CharacteristicValueObject>();

        CharacteristicValueObject charVO = new CharacteristicValueObject();
        charVO.setKey( "pubmedid" );
        charVO.setValue( results.getString( "pubmedid" ) );
        characteristics.put( charVO.getKey(), charVO );

        cnv.setCharacteristics( characteristics );

        return cnv;

    }

    public static SNVValueObject makeSNVFromResultSet( ResultSet results ) throws Exception {

        List<String> acceptableValues = Arrays.asList( new String[] { "A", "C", "G", "T", "N" } );

        String refBase = ConfigUtils.getString( "aspiredb.cli.variant.snv.referencebase" );
        String obsBase = ConfigUtils.getString( "aspiredb.cli.variant.snv.observedbase" );

        SNVValueObject snv = new SNVValueObject();

        snv.setGenomicRange( getGenomicRangeFromResultSet( results ) );
        snv.setPatientId( results.getString( CommonVariantColumn.SUBJECTID.key ) );

        snv.setReferenceBase( results.getString( refBase ) );
        snv.setObservedBase( results.getString( obsBase ) );

        if ( !acceptableValues.contains( snv.getReferenceBase().toUpperCase() ) ) {
            throw new InvalidDataException( "invalid " + refBase + ":" + snv.getReferenceBase() );
        }
        if ( !acceptableValues.contains( snv.getObservedBase().toUpperCase() ) ) {
            throw new InvalidDataException( "invalid " + obsBase + ":" + snv.getObservedBase() );
        }

        try {
            snv.setDbSNPID( results.getString( OptionalSNVColumn.DBSNPID.key ) );
        } catch ( SQLException e ) {
            // dbsnpid column not present
            log.debug( "dbsnpid not found" );
        }

        ArrayList<String> reservedSNVColumns = getReservedVariantColumns();

        reservedSNVColumns.add( refBase );
        reservedSNVColumns.add( obsBase );

        reservedSNVColumns.addAll( OptionalSNVColumn.getOptionalSNVColumnNames() );

        snv.setCharacteristics( getCharacteristicsFromResultSet( results, reservedSNVColumns ) );

        return snv;

    }

    /**
     * Compares query SNVs in chrMap against annotated SNVs in dbResults. If a match is found, save the functional
     * prediction's value as the query SNV's CharacteristicValueObject.
     * 
     * @param chrMap
     * @param dbResults
     * @param dbPredColname functional prediction method name in the database (e.g. LR_pred)
     * @return
     * @throws NumberFormatException
     * @throws SQLException
     */
    public static Collection<SNVValueObject> predictSNVFunction( HashMap<Integer, Collection<SNVValueObject>> chrMap,
            ResultSet dbResults, String dbPredColname ) {

        Collection<SNVValueObject> matched = new ArrayList<>();
        int posFound = 0;
        int line = 0;

        if ( chrMap == null ) {
            return matched;
        }

        String dbChr = "";

        StopWatch timer = new StopWatch();
        timer.start();

        while ( true ) {

            try {
                if ( !dbResults.next() ) {
                    break;
                }

                line++;
                if ( ( line % 1e5 ) == 0 ) {
                    log.debug( "Read " + line + " lines ..." );
                }
                dbChr = dbResults.getString( "#chr" );
                String dbPos = dbResults.getString( "pos(1-coor)" );
                String resultRef = dbResults.getString( "ref" );
                String resultAlt = dbResults.getString( "alt" );
                String resultPred = dbResults.getString( dbPredColname );
                Collection<SNVValueObject> resultVoList;

                try {
                    resultVoList = chrMap.get( Integer.parseInt( dbPos ) );
                } catch ( NumberFormatException e ) {
                    log.error( dbPos + " is not a valid Integer" );
                    continue;
                }

                if ( resultVoList == null ) {
                    continue;
                } else {
                    posFound++;
                }

                for ( SNVValueObject snvResultVo : resultVoList ) {

                    String refBaseVo = snvResultVo.getReferenceBase();
                    String obsBaseVo = snvResultVo.getObservedBase();

                    if ( !snvResultVo.getGenomicRange().getChromosome().equals( dbChr ) ) {
                        log.warn( "Chromosomes do not match!" );
                        return matched;
                    }
                    Map<String, CharacteristicValueObject> characteristics = snvResultVo.getCharacteristics();
                    if ( characteristics == null ) {
                        continue;
                    }

                    if ( ( refBaseVo != null ) && ( obsBaseVo != null ) && ( resultRef != null )
                            && ( resultAlt != null ) ) {
                        if ( ( refBaseVo.equals( resultRef ) ) && ( obsBaseVo.equals( resultAlt ) ) ) {
                            matched.add( snvResultVo );
                            CharacteristicValueObject dbPredVo = new CharacteristicValueObject();
                            dbPredVo.setKey( dbPredColname );
                            dbPredVo.setValue( resultPred );
                            characteristics.put( dbPredColname, dbPredVo );
                        }
                    }

                    // found all query positions
                    if ( posFound >= chrMap.keySet().size() ) {
                        break;
                    }
                }
            } catch ( SQLException e ) {
                log.error( e );
                continue;
            }
        }

        log.info( "Read " + line + " variants in chr" + dbChr + " which took " + timer.getTime() + " ms. "
                + matched.size() + " variants matched the query." );

        return matched;
    }

/**
     * Returns a position based map of SNVs.
     * 
     * @param vos
     * @return HashMap< 'chr', HashMap< 'base start position', Collection<SNVValueObject> > >
     */
    public static HashMap<String, HashMap<Integer, Collection<SNVValueObject>>> constructQuerySNVMap(
            ArrayList<VariantValueObject> vos ) {
        HashMap<String, HashMap<Integer, Collection<SNVValueObject>>> map = new HashMap<>();

        // store variant positions in memory
        for ( VariantValueObject vvo : vos ) {
            if ( vvo == null ) {
                continue;
            }
            if ( !( vvo instanceof SNVValueObject ) ) {
                log.warn( "SNVValueObject expected." );
                continue;
            }
            SNVValueObject vo = ( SNVValueObject ) vvo;

            GenomicRange coord = vo.getGenomicRange();
            if ( coord == null ) {
                continue;
            }
            int snvLength = Math.abs( coord.getBaseEnd() - coord.getBaseStart() );
            if ( snvLength > 1 ) {
                log.warn( "Variant " + vo.getGenomeCoordinates() + " is " + snvLength + " bases long!" );
            }
            Collection<SNVValueObject> volist;
            if ( map.get( coord.getChromosome() ) == null ) {
                map.put( coord.getChromosome(), new HashMap<Integer, Collection<SNVValueObject>>() );
            }
            volist = map.get( coord.getChromosome() ).get( coord.getBaseStart() );
            if ( volist == null ) {
                volist = new ArrayList<>();
                map.get( coord.getChromosome() ).put( coord.getBaseStart(), volist );
            }

            volist.add( vo );
        }

        return map;
    }

    /**
     * Compares the SNVs with those in the database. If a match is found, store the function prediction as the variant's
     * CharacteristicValueObject using the chosen function prediction method name as key (e.g. LR_pred). LR_pred:
     * Prediction of our LR based ensemble prediction score,"T(olerated)" or "D(amaging)". The score cutoff between "D"
     * and "T" is 0.5. The rankscore cutoff between "D" and "T" is 0.82268.
     * 
     * @see Bug 3958 - Gene variant prioritization
     * @link http://dbnsfp.houstonbioinformatics.org/dbNSFPzip/dbNSFP2.4.readme.txt
     * @param vos
     * @return totalVariantsPredicted
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static Collection<SNVValueObject> predictDbNsfpSNVFunction( ArrayList<VariantValueObject> vos )
            throws ClassNotFoundException, IOException {
        final String[] chrs = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16",
                "17", "18", "19", "20", "21", "22", "X", "Y" };
        final String propnamePath = "aspiredb.cli.variant.functionalprediction.dbDirectory";
        final String propnamePredictionColumn = "aspiredb.cli.variant.functionalprediction.predictionColumn";
        final String propnameRefBase = "aspiredb.cli.variant.snv.referencebase";
        final String propnameObsBase = "aspiredb.cli.variant.snv.observedbase";
        final String dbPrefix = "dbNSFP2.4_variant";
        Collection<SNVValueObject> matched = new ArrayList<>();
        StopWatch timer = new StopWatch();
        timer.start();

        // get db source paths from properties file
        String aspireRefBaseColumn = ConfigUtils.getString( propnameRefBase );
        String aspireObsBaseColumn = ConfigUtils.getString( propnameObsBase );
        String dbDirectory = ConfigUtils.getString( propnamePath );
        String dbPredColname = ConfigUtils.getString( propnamePredictionColumn );
        if ( dbDirectory == null || dbDirectory.length() == 0 ) {
            log.warn( "Property " + propnamePath + " not set. Functional prediction will not be computed." );
            return matched;
        }
        if ( dbPredColname == null || dbPredColname.length() == 0 ) {
            dbPredColname = "LR_pred";
            log.info( "Property " + propnamePredictionColumn + " not set. Defaulting to " + dbPredColname );
        }
        if ( aspireRefBaseColumn == null || aspireRefBaseColumn.length() == 0 ) {
            aspireRefBaseColumn = "ref_base";
            log.info( "Property " + propnameRefBase + " not set. Defaulting to " + aspireRefBaseColumn );
        }
        if ( aspireObsBaseColumn == null || aspireObsBaseColumn.length() == 0 ) {
            aspireObsBaseColumn = "obs_base";
            log.info( "Property " + propnameObsBase + " not set. Defaulting to " + aspireObsBaseColumn );
        }

        // check to see if the files are there
        for ( int i = 0; i < chrs.length; i++ ) {
            String path = dbDirectory + "/" + dbPrefix + ".chr" + chrs[i];
            try (BufferedReader br = new BufferedReader( new FileReader( path ) )) {
                String line = br.readLine();
                if ( !line.contains( dbPredColname ) ) {
                    log.warn( "File '" + path + "' does not contain column '" + dbPredColname + "'" );
                }
            }
        }

        // map.get('chr').get('position') = Collection<VariantValueObject>
        HashMap<String, HashMap<Integer, Collection<SNVValueObject>>> map = constructQuerySNVMap( vos );

        // search the database of functional predictions using our map of variants
        Class.forName( "org.relique.jdbc.csv.CsvDriver" );

        String delimiter = URLEncoder.encode( "\t", "UTF-8" );

        for ( String chr : map.keySet() ) {
            try {
                // create a connection
                // arg[0] is the directory in which the .csv files are held
                String connStr = "jdbc:relique:csv:" + dbDirectory + "?" + "separator=" + delimiter + "&"
                        + "fileExtension=" + ".chr" + chr;
                Connection conn = DriverManager.getConnection( connStr );
                Statement stmt = conn.createStatement();

                // String colnames = "#chr,pos(1-coor),ref,alt," + dbPredColname;
                String colnames = "*";
                String query = "SELECT " + colnames + " FROM " + dbPrefix + " WHERE " + " NOT (" + dbPredColname
                        + " IS NULL) AND " + dbPredColname + " != '.' ";

                log.debug( "Target database=[" + connStr + "] query=[" + query + "]" );

                try (ResultSet dbResults = stmt.executeQuery( query )) {
                    HashMap<Integer, Collection<SNVValueObject>> chrMap = map.get( chr );
                    if ( chrMap == null ) {
                        continue;
                    }

                    Collection<SNVValueObject> matchedPerChr = predictSNVFunction( chrMap, dbResults, dbPredColname );

                    matched.addAll( matchedPerChr );

                } finally {
                    stmt.close();
                    conn.close();
                }
            } catch ( SQLException e ) {
                log.error( "Error occured while trying to compute variant functional prediction.", e );
                continue;
            }
        }

        log.info( "Total of " + matched.size() + " function predictions were made which took " + timer.getTime()
                + " ms." );

        return matched;
    }

    public static IndelValueObject makeIndelFromResultSet( ResultSet results ) throws Exception {

        String indelLength = ConfigUtils.getString( "aspiredb.cli.variant.indel.length" );

        IndelValueObject indel = new IndelValueObject();

        indel.setGenomicRange( getGenomicRangeFromResultSet( results ) );
        indel.setPatientId( results.getString( CommonVariantColumn.SUBJECTID.key ) );
        indel.setLength( results.getInt( indelLength ) );

        if ( indel.getLength() < 0 ) {
            throw new InvalidDataException( indelLength + " " + indel.getLength() + " is not allowed" );
        }

        ArrayList<String> requiredIndelColumns = new ArrayList<String>();
        requiredIndelColumns.add( indelLength );
        requiredIndelColumns.addAll( CommonVariantColumn.getCommonVariantColumnNames() );

        indel.setCharacteristics( getCharacteristicsFromResultSet( results, requiredIndelColumns ) );

        return indel;

    }

    public static InversionValueObject makeInversionFromResultSet( ResultSet results ) throws Exception {

        List<String> reservedIndelColumns = CommonVariantColumn.getCommonVariantColumnNames();

        InversionValueObject inversion = new InversionValueObject();

        inversion.setGenomicRange( getGenomicRangeFromResultSet( results ) );
        inversion.setPatientId( results.getString( CommonVariantColumn.SUBJECTID.key ) );

        inversion.setCharacteristics( getCharacteristicsFromResultSet( results, reservedIndelColumns ) );

        return inversion;

    }

    private static GenomicRange getGenomicRangeFromResultSet( ResultSet results ) throws Exception {
        GenomicRange gr = new GenomicRange();

        String chrom = results.getString( CommonVariantColumn.CHROM.key ).toUpperCase();

        // For decipher data, this really shouldn't be here, adding it to quickly add in decipher data
        if ( chrom.startsWith( "CHR" ) ) {
            chrom = chrom.replace( "CHR", "" );
        }

        gr.setChromosome( chrom );

        // Note that results.getInt return 0 if it is not a number
        gr.setBaseStart( results.getInt( CommonVariantColumn.START.key ) );
        gr.setBaseEnd( results.getInt( CommonVariantColumn.END.key ) );

        validateGenomicRange( gr );

        return gr;

    }

    // this can be more strict, when we start importing different data files we should factor this kind of validation
    // out into its own class
    private static void validateGenomicRange( GenomicRange gr ) throws Exception {

        if ( gr.getBaseStart() < 1 || gr.getBaseEnd() < 1 ) {
            throw new InvalidDataException( "Invalid Genomic Coordinates" );
        }

        if ( StringUtils.isAlpha( gr.getChromosome() ) ) {
            if ( !gr.getChromosome().equals( "X" ) && !gr.getChromosome().equals( "Y" ) ) {
                throw new InvalidDataException( "Chromosome value " + gr.getChromosome() + " is not allowed" );
            }

        }

        if ( StringUtils.isNumeric( gr.getChromosome() ) ) {
            Integer chrom = Integer.parseInt( gr.getChromosome() );

            if ( chrom < 1 || chrom > 22 ) {
                throw new InvalidDataException( "Chromosome value " + gr.getChromosome() + " is not allowed" );
            }

        }

    }

    private static Map<String, CharacteristicValueObject> getCharacteristicsFromResultSet( ResultSet results,
            List<String> requiredColumns ) throws SQLException {

        Map<String, CharacteristicValueObject> characteristics = new HashMap<String, CharacteristicValueObject>();

        ResultSetMetaData rsmd = results.getMetaData();
        int numColumns = rsmd.getColumnCount();

        // Get the column names; column indices start from 1
        for ( int i = 1; i < numColumns + 1; i++ ) {
            String columnName = rsmd.getColumnName( i ).trim();
            String value = results.getString( i ).trim();

            if ( !requiredColumns.contains( columnName ) && !value.isEmpty() ) {

                CharacteristicValueObject charVO = new CharacteristicValueObject();
                charVO.setKey( columnName );
                charVO.setValue( value );
                characteristics.put( charVO.getKey(), charVO );
            }
        }

        return characteristics;
    }

    private static ArrayList<String> getReservedVariantColumns() {

        ArrayList<String> reservedColumns = new ArrayList<String>();
        reservedColumns.addAll( CommonVariantColumn.getCommonVariantColumnNames() );
        reservedColumns.addAll( OptionalVariantColumn.getOptionalVariantColumnNames() );

        return reservedColumns;

    }

    private static void populateOptionalVariantColumns( ResultSet results, VariantValueObject vo ) throws Exception {

        vo.setUserVariantId( getValueFromResultSet( OptionalVariantColumn.USERVARIANTID.key, results ) );
        vo.setDescription( getValueFromResultSet( OptionalVariantColumn.DESCRIPTION.key, results ) );
        vo.setExternalId( getValueFromResultSet( OptionalVariantColumn.EXTERNALID.key, results ) );

    }

    private static void populateOptionalCNVColumns( ResultSet results, CNVValueObject vo ) throws Exception {

        Integer copyNumber = null;
        String copyNumberString = getValueFromResultSet( OptionalCNVColumn.COPYNUMBER.key, results );

        if ( copyNumberString == null || copyNumberString.isEmpty() ) {
            vo.setCopyNumber( null );
        } else {
            copyNumber = Integer.parseInt( copyNumberString );
            vo.setCopyNumber( copyNumber );
        }

    }

    private static String getValueFromResultSet( String columnName, ResultSet results ) throws Exception {

        ResultSetMetaData rsmd = results.getMetaData();
        int numColumns = rsmd.getColumnCount();

        for ( int i = 1; i < numColumns + 1; i++ ) {
            String column = rsmd.getColumnName( i );

            if ( column.equals( columnName ) ) {
                return results.getString( i );
            }
        }

        return null;
    }

    public enum CommonVariantColumn {
        SUBJECTID(ConfigUtils.getString( "aspiredb.cli.variant.subjectid" )), CHROM(ConfigUtils
                .getString( "aspiredb.cli.variant.chrom" )), START(ConfigUtils.getString( "aspiredb.cli.variant.start" )), END(
                ConfigUtils.getString( "aspiredb.cli.variant.end" ));

        public String key;

        private CommonVariantColumn( String key ) {
            this.key = key;
        }

        public static List<String> getCommonVariantColumnNames() {
            ArrayList<String> columnNames = new ArrayList<String>();

            for ( CommonVariantColumn column : CommonVariantColumn.values() ) {
                columnNames.add( column.key );
            }

            return columnNames;
        }
    }

    public enum OptionalVariantColumn {
        USERVARIANTID(ConfigUtils.getString( "aspiredb.cli.variant.uservariantid" )), DESCRIPTION(ConfigUtils
                .getString( "aspiredb.cli.variant.description" )), EXTERNALID(ConfigUtils
                .getString( "aspiredb.cli.variant.externalid" ));

        String key;

        private OptionalVariantColumn( String key ) {
            this.key = key;
        }

        public static List<String> getOptionalVariantColumnNames() {
            ArrayList<String> columnNames = new ArrayList<String>();

            for ( OptionalVariantColumn column : OptionalVariantColumn.values() ) {
                columnNames.add( column.key );
            }

            return columnNames;
        }
    }

    public enum OptionalSNVColumn {
        DBSNPID(ConfigUtils.getString( "aspiredb.cli.variant.snv.dbsnpid" ));

        String key;

        private OptionalSNVColumn( String key ) {
            this.key = key;
        }

        public static List<String> getOptionalSNVColumnNames() {
            ArrayList<String> columnNames = new ArrayList<String>();

            for ( OptionalSNVColumn column : OptionalSNVColumn.values() ) {
                columnNames.add( column.key );
            }

            return columnNames;
        }
    }

    public enum OptionalCNVColumn {
        COPYNUMBER(ConfigUtils.getString( "aspiredb.cli.variant.cnv.copynumber" ));

        String key;

        private OptionalCNVColumn( String key ) {
            this.key = key;
        }

        public static List<String> getOptionalCNVColumnNames() {
            ArrayList<String> columnNames = new ArrayList<String>();

            for ( OptionalCNVColumn column : OptionalCNVColumn.values() ) {
                columnNames.add( column.key );
            }

            return columnNames;
        }
    }

}