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

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.shared.LabelValueObject;
import ubc.pavlab.aspiredb.shared.query.Operator;
import ubc.pavlab.aspiredb.shared.query.VariantLabelProperty;
import ubc.pavlab.aspiredb.shared.query.restriction.SimpleRestriction;

@Repository("variantDao")
public class VariantDaoImpl extends VariantDaoBaseImpl<Variant> implements VariantDao {

    @Autowired
    public VariantDaoImpl( SessionFactory sessionFactory ) {
        super( Variant.class );
        super.setSessionFactory( sessionFactory );
    }

    private Session currentSession() {
        return getSession();
    }

    @Override
    public Collection<Variant> findByLabel( LabelValueObject label ) {
        Criteria criteria = currentSession().createCriteria( Variant.class );
        SimpleRestriction restrictionExpression = new SimpleRestriction( new VariantLabelProperty(),
                Operator.TEXT_EQUAL, label );
        Criterion criterion = CriteriaBuilder.buildCriteriaRestriction( restrictionExpression,
                CriteriaBuilder.EntityType.VARIANT );
        criteria.add( criterion );
        return criteria.list();
    }

    @Override
    @Transactional(readOnly = true)
    public Variant findByUserVariantId( String userVariantId, String patientId ) {

        Query query = this
                .getSessionFactory()
                .getCurrentSession()
                .createQuery(
                        "from Variant as variant where variant.userVariantId=:variantId and variant.subject.patientId = :patientId" );

        query.setParameter( "variantId", userVariantId );
        query.setParameter( "patientId", patientId );

        return ( Variant ) query.uniqueResult();

    }

}
