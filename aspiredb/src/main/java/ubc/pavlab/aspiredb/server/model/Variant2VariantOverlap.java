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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This table is used to hold data about overlap between two variants. Originally this was only to be used for overlap
 * with 'special' variants in DGV and DECIPHER, hence the 'special' in the name of the table new requirements were added
 * to allow this functionality between two user projects so the 'special' is unnecessary in the name and should be
 * removed/renamed
 * 
 * @author cmcdonald
 * @version $Id:$
 */
@Entity
@Table(name = "VARIANT2VARIANTOVERLAP")
public class Variant2VariantOverlap implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6734779432249098068L;

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "VARIANTID")
    private Long variantId;

    @Column(name = "OVERLAPPED_VARIANTID")
    private Long overlapSpecialVariantId;

    @Column(name = "OVERLAP_LENGTH")
    private Integer overlap;

    // the percentage of the variantId-variant that is overlapped, storing for easier searching
    // using integer as it is better for comparison, easy to change to float later if need be
    @Column(name = "OVERLAP_PERCENTAGE")
    private Integer overlapPercentage;

    // the percentage of the overlapSpecialVariantId-variant that is overlapped
    @Column(name = "OVERLAPPED_OVERLAP_PERCENTAGE")
    private Integer overlappedOverlapPercentage;

    @Column(name = "OVERLAP_PROJECTID")
    private Long overlapProjectId;

    @Column(name = "PROJECTID")
    private Long projectId;

    public Variant2VariantOverlap() {

    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        Variant2VariantOverlap other = ( Variant2VariantOverlap ) obj;
        if ( overlapSpecialVariantId == null ) {
            if ( other.overlapSpecialVariantId != null ) {
                return false;
            }
        } else if ( !overlapSpecialVariantId.equals( other.overlapSpecialVariantId ) ) {
            return false;
        }
        if ( variantId == null ) {
            if ( other.variantId != null ) {
                return false;
            }
        } else if ( !variantId.equals( other.variantId ) ) {
            return false;
        }
        return true;
    }

    public Integer getOverlap() {
        return overlap;
    }

    public Integer getOverlappedOverlapPercentage() {
        return overlappedOverlapPercentage;
    }

    public Integer getOverlapPercentage() {
        return overlapPercentage;
    }

    public Long getOverlapProjectId() {
        return overlapProjectId;
    }

    public Long getOverlapSpecialVariantId() {
        return overlapSpecialVariantId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getVariantId() {
        return variantId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( overlapSpecialVariantId == null ) ? 0 : overlapSpecialVariantId.hashCode() );
        result = prime * result + ( ( variantId == null ) ? 0 : variantId.hashCode() );
        return result;
    }

    public void setOverlap( Integer overlap ) {
        this.overlap = overlap;
    }

    public void setOverlappedOverlapPercentage( Integer overlappedOverlapPercentage ) {
        this.overlappedOverlapPercentage = overlappedOverlapPercentage;
    }

    public void setOverlapPercentage( Integer overlapPercentage ) {
        this.overlapPercentage = overlapPercentage;
    }

    public void setOverlapProjectId( Long overlapProjectId ) {
        this.overlapProjectId = overlapProjectId;
    }

    public void setOverlapSpecialVariantId( Long overlappedSpecialVariantId ) {
        this.overlapSpecialVariantId = overlappedSpecialVariantId;
    }

    public void setProjectId( Long projectId ) {
        this.projectId = projectId;
    }

    public void setVariantId( Long variantId ) {
        this.variantId = variantId;
    }

}