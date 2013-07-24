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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ubc.pavlab.aspiredb.cli.InvalidDataException;
import ubc.pavlab.aspiredb.server.model.CnvType;
import ubc.pavlab.aspiredb.server.util.ConfigUtils;
import ubc.pavlab.aspiredb.shared.*;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

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
            }catch ( Exception e ) {
                errorMessages.add( "Error on line number: " + lineNumber + " error message:"
                        + e.getMessage() );
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
        cnv.setCnvLength( cnv.getGenomicRange().getBaseEnd()-cnv.getGenomicRange().getBaseStart() );

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

        gr.setChromosome( results.getString( CommonVariantColumn.CHROM.key ).toUpperCase() );

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

        if ( copyNumberString == null || copyNumberString.isEmpty() ){
            vo.setCopyNumber( null );
        }else{
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