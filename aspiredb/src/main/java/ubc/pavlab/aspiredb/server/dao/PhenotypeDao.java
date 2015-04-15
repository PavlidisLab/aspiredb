/*
 * The aspiredb project
 * 
 * Copyright (c) 2012 University of British Columbia
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
package ubc.pavlab.aspiredb.server.dao;

import java.util.Collection;
import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.model.Phenotype;

/**
 * TODO Document Me
 * 
 * @author ??
 * @version $Id: PhenotypeDao.java,v 1.24 2013/07/12 17:11:46 cmcdonald Exp $
 */
public interface PhenotypeDao extends DaoBase<Phenotype> {

    @Secured({ "GROUP_USER", "AFTER_ACL_SUBJECT_ATTRIBUTE_COLLECTION_READ" })
    public Collection<Phenotype> findBySubjectId( Long id );

    public Integer findPhenotypeCountBySubjectId( Long id );

    @Secured({ "GROUP_USER", "AFTER_ACL_SUBJECT_ATTRIBUTE_COLLECTION_READ" })
    public Collection<Phenotype> findPresentByProjectIdsAndUri( Collection<Long> ids, String uri );

    public List<String> getDistinctOntologyUris( Collection<Long> activeProjects );

    /**
     * Returns the list of Phenotype names that is specific to activeProjectIds. If activeProjectIds is null, return all
     * the Phenotype names.
     * 
     * @param activeProjectIds
     * @return
     */
    @Transactional(readOnly = true)
    public Collection<String> getExistingNames( Collection<Long> activeProjectIds );

    // TODO: reuse for suggestions
    @Transactional(readOnly = true)
    public List<String> getExistingPhenotypes( String query, boolean isExactMatch, Collection<Long> activeProjects );

    @Transactional(readOnly = true)
    public List<String> getExistingURIs( String name );

    @Transactional(readOnly = true)
    public List<String> getExistingValues( String name );

    // TODO: reuse for suggestions
    public List<String> getListOfPossibleValuesByName( Collection<Long> projectIds, String name );

    public List<String> getListOfPossibleValuesByUri( Collection<Long> projectIds, String uri );

    @Transactional(readOnly = true)
    @Secured({ "GROUP_USER", "AFTER_ACL_SUBJECT_ATTRIBUTE_COLLECTION_READ" })
    public Collection<Phenotype> loadAllByProjectIds( Collection<Long> projectIds );

    @Secured({ "GROUP_USER", "AFTER_ACL_SUBJECT_ATTRIBUTE_COLLECTION_READ" })
    public Collection<Phenotype> loadBySubjectIds( Collection<Long> subjectIds );

    @Transactional(readOnly = true)
    boolean isInDatabase( Collection<String> names );

}