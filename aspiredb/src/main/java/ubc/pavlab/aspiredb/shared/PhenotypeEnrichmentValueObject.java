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
package ubc.pavlab.aspiredb.shared;

import java.io.Serializable;

import org.directwebremoting.annotations.DataTransferObject;

/**
 * TODO Document Me <<<<<<< HEAD ======= >>>>>>> 0a87450512a15eeb817865f623adcb0eefa7cc1e
 * 
 * @version $Id: PhenotypeEnrichmentValueObject.java,v 1.2 2013/07/09 21:43:18 cmcdonald Exp $
 */

@DataTransferObject
public class PhenotypeEnrichmentValueObject implements Serializable {

    /**
     * first pass at value object for phenotype enrichment, will change once the enrichment functionality becomes more
     * clear a couple of these fields are probably unnecessary
     */
    private static final long serialVersionUID = 1802977811089034726L;

    private Long id;

    private String uri;
    private String name;
    private Integer total;
    private Integer inGroupTotal;
    private String inGroupTotalString;
    private Integer outGroupTotal;
    private String outGroupTotalString;
    private Double pValue;
    private Double pValueCorrected;
    private String pValueString;
    private String pValueCorrectedString;

    public PhenotypeEnrichmentValueObject() {
    }

    public Long getId() {
        return id;
    }

    public Integer getInGroupTotal() {
        return inGroupTotal;
    }

    public String getInGroupTotalString() {
        return inGroupTotalString;
    }

    public String getName() {
        return name;
    }

    public Integer getOutGroupTotal() {
        return outGroupTotal;
    }

    public String getOutGroupTotalString() {
        return outGroupTotalString;
    }

    public Double getPValue() {
        return pValue;
    }

    public Double getPValueCorrected() {
        return pValueCorrected;
    }

    public String getPValueCorrectedString() {
        return pValueCorrectedString;
    }

    public String getPValueString() {
        return pValueString;
    }

    public Integer getTotal() {
        return total;
    }

    public String getUri() {
        return uri;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public void setInGroupTotal( Integer inGroupTotal ) {
        this.inGroupTotal = inGroupTotal;
    }

    public void setInGroupTotalString( String inGroupTotalString ) {
        this.inGroupTotalString = inGroupTotalString;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public void setOutGroupTotal( Integer outGroupTotal ) {
        this.outGroupTotal = outGroupTotal;
    }

    public void setOutGroupTotalString( String outGroupTotalString ) {
        this.outGroupTotalString = outGroupTotalString;
    }

    public void setPValue( Double pValue ) {
        this.pValue = pValue;
    }

    public void setPValueCorrected( Double pValueCorrected ) {
        this.pValueCorrected = pValueCorrected;
    }

    public void setPValueCorrectedString( String pValueCorrectedString ) {
        this.pValueCorrectedString = pValueCorrectedString;
    }

    public void setPValueString( String pValueString ) {
        this.pValueString = pValueString;
    }

    public void setTotal( Integer total ) {
        this.total = total;
    }

    public void setUri( String uri ) {
        this.uri = uri;
    }

}
