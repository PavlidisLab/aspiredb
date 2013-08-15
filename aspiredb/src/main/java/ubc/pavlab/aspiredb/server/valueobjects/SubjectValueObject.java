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

package ubc.pavlab.aspiredb.server.valueobjects;

import java.io.Serializable;
import java.util.Collection;

import org.directwebremoting.annotations.DataTransferObject;

import ubc.pavlab.aspiredb.shared.LabelValueObject;

/**
 * @see Subject
 * @author ptan
 * @version $Id$
 */
@DataTransferObject
public class SubjectValueObject implements Serializable {

    private static final long serialVersionUID = 6701485127497073745L;

    private Long id;

    private String patientId;

    private Collection<LabelValueObject> labels;

    /**
     * 
     */
    public SubjectValueObject() {
        // TODO Auto-generated constructor stub
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId( String patientId ) {
        this.patientId = patientId;
    }

    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public Collection<LabelValueObject> getLabels() {
        return labels;
    }

    public void setLabels( Collection<LabelValueObject> labels ) {
        this.labels = labels;
    }

}
