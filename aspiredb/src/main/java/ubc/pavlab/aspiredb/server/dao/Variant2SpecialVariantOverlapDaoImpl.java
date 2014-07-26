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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.model.Variant2VariantOverlap;
import ubc.pavlab.aspiredb.shared.NumericValue;
import ubc.pavlab.aspiredb.shared.query.MutualOverlapPercentageProperty;
import ubc.pavlab.aspiredb.shared.query.Operator;
import ubc.pavlab.aspiredb.shared.query.OverlapBasesProperty;
import ubc.pavlab.aspiredb.shared.query.OverlapPercentageMyVariantProperty;
import ubc.pavlab.aspiredb.shared.query.OverlapPercentageOtherVariantProperty;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.query.restriction.SimpleRestriction;

@Repository("variant2SpecialVariantOverlapDao")
public class Variant2SpecialVariantOverlapDaoImpl extends DaoBaseImpl<Variant2VariantOverlap> implements
        Variant2SpecialVariantOverlapDao {

    protected static Log log = LogFactory.getLog( ProjectDaoImpl.class );

    @Autowired
    ProjectDao projectDao;

    @Autowired
    public Variant2SpecialVariantOverlapDaoImpl( SessionFactory sessionFactory ) {
        super( Variant2VariantOverlap.class );
        super.setSessionFactory( sessionFactory );
    }

    @Override
    @Transactional
    public void deleteByOverlapProjectId( Long id ) {

        Collection<Variant2VariantOverlap> overlaps = loadByOverlapProjectId( id );

        this.getHibernateTemplate().deleteAll( overlaps );

    }

    @Override
    @Transactional
    public void deleteByOverlapProjectIds( Collection<Long> ids ) {

        for ( Long id : ids ) {

            Collection<Variant2VariantOverlap> overlaps = loadByOverlapProjectId( id );

            this.getHibernateTemplate().deleteAll( overlaps );

        }

    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Variant2VariantOverlap> loadByOverlapProjectId( Long overlapProjectId ) {

        if ( overlapProjectId == null ) {
            return new ArrayList<Variant2VariantOverlap>();
        }

        String[] paramNames = { "overlapProjectId" };
        Object[] objectValues = { overlapProjectId };

        return this.getHibernateTemplate().findByNamedParam(
                "from Variant2VariantOverlap where overlapProjectId =:overlapProjectId", paramNames, objectValues );

    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Variant2VariantOverlap> loadByProjectIdAndOverlapProjectId( Long projectId, Long overlapProjectId ) {

        if ( overlapProjectId == null ) {
            return new ArrayList<Variant2VariantOverlap>();
        }

        String[] paramNames = { "projectId", "overlapProjectId" };
        Object[] objectValues = { projectId, overlapProjectId };

        return this.getHibernateTemplate().findByNamedParam(
                "from Variant2VariantOverlap where projectId =:projectId and overlapProjectId =:overlapProjectId",
                paramNames, objectValues );

    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Variant2VariantOverlap> loadByVariantId( Long id, Collection<Long> overlapProjectIds ) {

        if ( id == null ) {
            return new ArrayList<Variant2VariantOverlap>();
        }

        String[] paramNames = { "id", "overlapProjectIds" };
        Object[] objectValues = { id, overlapProjectIds };

        return this.getHibernateTemplate().findByNamedParam(
                "from Variant2VariantOverlap where variantId = :id and overlapProjectId in (:overlapProjectIds)",
                paramNames, objectValues );

    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Variant2VariantOverlap> loadByVariantIdAndOverlap( Long id, SimpleRestriction overlapRestriction,
            Collection<Long> overlapProjectIds ) {

        if ( id == null ) {
            return new ArrayList<Variant2VariantOverlap>();

        }

        String queryString = "from Variant2VariantOverlap where variantId = :id";

        Operator o = overlapRestriction.getOperator();

        String operatorString = "";

        if ( o.equals( Operator.NUMERIC_GREATER_OR_EQUAL ) ) {
            operatorString = ">=";
        } else if ( o.equals( Operator.NUMERIC_LESS_OR_EQUAL ) ) {
            operatorString = "<=";
        } else {

            throw new RuntimeException( "Invalid Operator" );

        }

        Property p = overlapRestriction.getProperty();

        String columnRestriction = "";

        if ( p instanceof OverlapBasesProperty ) {

            columnRestriction = " and overlap " + operatorString + " :overlap";

        } else if ( p instanceof MutualOverlapPercentageProperty ) {
            columnRestriction = " and overlapPercentage " + operatorString
                    + " :overlap and overlappedOverlapPercentage " + operatorString + " :overlap ";
        } else if ( p instanceof OverlapPercentageMyVariantProperty ) {
            columnRestriction = " and overlapPercentage " + operatorString + " :overlap";
        } else if ( p instanceof OverlapPercentageOtherVariantProperty ) {
            columnRestriction = " and overlappedOverlapPercentage " + operatorString + " :overlap ";
        }

        queryString = queryString + columnRestriction + " and overlapProjectId in (:overlapProjectIds)";

        NumericValue numeric = ( NumericValue ) overlapRestriction.getValue();

        String[] paramNames = { "id", "overlap", "overlapProjectIds" };
        Object[] objectValues = { id, numeric.getValue(), overlapProjectIds };

        return this.getHibernateTemplate().findByNamedParam( queryString, paramNames, objectValues );

    }

}