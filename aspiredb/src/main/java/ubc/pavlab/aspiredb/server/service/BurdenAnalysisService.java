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
package ubc.pavlab.aspiredb.server.service;

import java.util.Collection;
import java.util.Map;

import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.service.BurdenAnalysisServiceImpl.CnvBurdenAnalysisPerSubject;
import ubc.pavlab.aspiredb.shared.BurdenAnalysisValueObject;
import ubc.pavlab.aspiredb.shared.LabelValueObject;
import ubc.pavlab.aspiredb.shared.query.CharacteristicProperty;

/**
 * 
 */
public interface BurdenAnalysisService {

    public Collection<Map<CnvBurdenAnalysisPerSubject, String>> getBurdenAnalysisPerSubject( Collection<Long> subjectIds )
            throws NotLoggedInException, BioMartServiceException;

    /**
     * Performs a burden analysis between subject labels group1 and group2 filtered variants.
     * 
     * @param group1
     * @param group2
     * @param variantIds
     * @return
     * @throws NotLoggedInException
     * @throws BioMartServiceException
     */
    public Collection<BurdenAnalysisValueObject> getBurdenAnalysisPerSubjectLabel( LabelValueObject group1,
            LabelValueObject group2, Collection<Long> variantIds ) throws NotLoggedInException, BioMartServiceException;

    /**
     * Performs a burden analysis between subject labels group1 and group2 filtered variants for each of the
     * characteristic values.
     * 
     * @param characteristic
     * @param group1
     * @param group2
     * @param variantIds
     * @return
     * @throws NotLoggedInException
     * @throws BioMartServiceException
     * @throws NeurocartaServiceException
     */
    public Collection<BurdenAnalysisValueObject> getBurdenAnalysisCharacteristic(
            CharacteristicProperty characteristic, LabelValueObject group1, LabelValueObject group2,
            Collection<Long> variantIds ) throws NotLoggedInException, BioMartServiceException,
            NeurocartaServiceException;

}
