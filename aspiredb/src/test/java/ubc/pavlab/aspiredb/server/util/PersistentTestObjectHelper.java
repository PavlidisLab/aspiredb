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
package ubc.pavlab.aspiredb.server.util;

import ubc.pavlab.aspiredb.server.model.CNV;
import ubc.pavlab.aspiredb.server.model.Indel;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.SNV;
import ubc.pavlab.aspiredb.server.model.Subject;

/**
 * Class for tests to use to create and remove persistent objects This interface will become unnecessary one we have
 * services that do these operations. Using this class in the meantime to get some basic test coverage.
 * 
 * @author cmcdonald
 * @version $Id: PersistentTestObjectHelper.java,v 1.16 2013/07/09 21:25:44 cmcdonald Exp $
 */

public interface PersistentTestObjectHelper {

    public SNV createPersistentTestSNVObject();

    public SNV createDetachedTestSNVObject();

    public Indel createPersistentTestIndelObject();

    public Indel createDetachedTestIndelObject();

    public CNV createPersistentTestCNVObject();

    public CNV createDetachedTestCNVObject();

    public Subject createPersistentTestSubjectObjectWithCNV( String patientId );
    
    public Subject createPersistentTestSubjectObjectWithHPOntologyPhenotypes( String patientId );
    
    public Subject createPersistentTestSubjectObjectWithHPOntologyPhenotypesForEnrichmentTest( String patientId, String phenName, String phenUri, String phenValue );

    public Subject createPersistentTestIndividualObject( String patientId );

    public Subject createDetachedIndividualObject( String patientId );

    public Project createPersistentProject( Project p );

}