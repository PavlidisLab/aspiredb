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
package ubc.pavlab.aspiredb.server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 * author: anton date: 19/02/13
 */
@Component("cytobandFileLoader")
public class CytobandFileLoaderImpl implements CytobandFileLoader {

    // TODO: use annotations to grab this property
    // @Value("${aspiredb.cytoband.path}")
    // private String filePath;

    private Map<String, Chromosome> chromosomes = new HashMap<String, Chromosome>();

    @Override
    public Map<String, Chromosome> load() {

        try {
            // Get file name with coordinate mappings (downloaded from UCSC)
            // String filePath = ConfigUtils.getString("aspiredb.cytoband.path");
            final InputStream cytobandResourceStream = this.getClass().getResourceAsStream( "/data/cytoBand.txt" );
            BufferedReader reader = new BufferedReader( new InputStreamReader( cytobandResourceStream ) );

            String line;
            while ( ( line = reader.readLine() ) != null ) {
                FileRow row = parseFileRow( line );

                Chromosome chromosome = getOrCreateChromosome( row.chromosome );
                chromosome.addBand( row.start, row.end, row.cytoband, row.staining );
            }

        } catch ( FileNotFoundException e ) {
            e.printStackTrace(); // TODO: log
        } catch ( IOException e ) {
            // TODO: log
        }

        return chromosomes;
    }

    private Chromosome getOrCreateChromosome( String name ) {
        Chromosome chromosome = chromosomes.get( name );
        if ( chromosome == null ) {
            chromosome = new Chromosome( name );
            chromosomes.put( name, chromosome );
        }
        return chromosome;
    }

    private static class FileRow {
        String chromosome;
        Integer start;
        Integer end;
        String cytoband;
        String staining;
    }

    private FileRow parseFileRow( String row ) {
        // Row format (tab delimited): chromosome, start, end, cytoband, staining agent
        final int CHROMOSOME = 0;
        final int START = 1;
        final int END = 2;
        final int CYTOBAND = 3;
        final int STAINING_AGENT = 4;

        row = StringUtils.strip( row );
        String[] fields = row.split( "\t" );

        FileRow parsedRow = new FileRow();
        parsedRow.chromosome = fields[CHROMOSOME].replaceAll( "chr", "" ); // trim 'chr' part
        parsedRow.start = Integer.parseInt( fields[START] );
        parsedRow.end = Integer.parseInt( fields[END] );
        parsedRow.cytoband = fields[CYTOBAND];
        parsedRow.staining = fields[STAINING_AGENT];

        return parsedRow;
    }

}
