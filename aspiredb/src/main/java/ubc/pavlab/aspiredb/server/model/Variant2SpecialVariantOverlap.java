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
package ubc.pavlab.aspiredb.server.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * TODO Document Me
 * 
 * @author ??
 * @version $Id:$
 */
@Entity
@Table(name = "VARIANT2SPECIAL_VARIANT_INFO")
public class Variant2SpecialVariantOverlap implements Serializable  {

    /**
     * 
     */
    private static final long serialVersionUID = 6734779432249098068L;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( overlapSpecialVariantId == null ) ? 0 : overlapSpecialVariantId.hashCode() );
        result = prime * result + ( ( variantId == null ) ? 0 : variantId.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        Variant2SpecialVariantOverlap other = ( Variant2SpecialVariantOverlap ) obj;
        if ( overlapSpecialVariantId == null ) {
            if ( other.overlapSpecialVariantId != null ) return false;
        } else if ( !overlapSpecialVariantId.equals( other.overlapSpecialVariantId ) ) return false;
        if ( variantId == null ) {
            if ( other.variantId != null ) return false;
        } else if ( !variantId.equals( other.variantId ) ) return false;
        return true;
    }

    //note this is a composite key with variantId and overlapSpecialVariantId
    @Id    
    @Column(name="VARIANTID")
    private Long variantId;
    
    @Id
    @Column(name="OVERLAPPED_VARIANTID")
    private Long overlapSpecialVariantId;
    
    @Column(name="OVERLAP_LENGTH")
    private Integer overlap;
    
    //the percentage of the variantId-variant that is overlapped, storing for easier searching
    //using integer as it is better for comparison, easy to change to float later if need be
    @Column(name="OVERLAP_PERCENTAGE")
    private Integer overlapPercentage;
    
    //the percentage of the overlapSpecialVariantId-variant that is overlapped
    @Column(name="OVERLAPPED_OVERLAP_PERCENTAGE")
    private Integer overlappedOverlapPercentage;
    
    @Column(name="OVERLAP_PROJECTID")
    private Long overlapProjectId;
    
    public Variant2SpecialVariantOverlap(){
        
    }

    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId( Long variantId ) {
        this.variantId = variantId;
    }

    public Long getOverlapSpecialVariantId() {
        return overlapSpecialVariantId;
    }

    public void setOverlapSpecialVariantId( Long overlappedSpecialVariantId ) {
        this.overlapSpecialVariantId = overlappedSpecialVariantId;
    }

    public Integer getOverlap() {
        return overlap;
    }

    public void setOverlap( Integer overlap ) {
        this.overlap = overlap;
    }

    public Integer getOverlapPercentage() {
        return overlapPercentage;
    }

    public void setOverlapPercentage( Integer overlapPercentage ) {
        this.overlapPercentage = overlapPercentage;
    }

    public Integer getOverlappedOverlapPercentage() {
        return overlappedOverlapPercentage;
    }

    public void setOverlappedOverlapPercentage( Integer overlappedOverlapPercentage ) {
        this.overlappedOverlapPercentage = overlappedOverlapPercentage;
    }

    public Long getOverlapProjectId() {
        return overlapProjectId;
    }

    public void setOverlapProjectId( Long overlapProjectId ) {
        this.overlapProjectId = overlapProjectId;
    }
    
    
    
}
