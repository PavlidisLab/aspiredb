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
package ubc.pavlab.aspiredb.server.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ubc.pavlab.aspiredb.server.model.Label;
import ubc.pavlab.aspiredb.shared.LabelValueObject;

/**
 * author: anton date: 25/04/13
 */
@Repository
public class LabelDaoImpl extends SecurableDaoBaseImpl<Label> implements LabelDao {

    @Autowired
    public LabelDaoImpl( SessionFactory sessionFactory ) {
        super( Label.class );
        super.setSessionFactory( sessionFactory );
    }

    @Override
    @Transactional
    public Label findOrCreate( LabelValueObject labelVO ) {
        if ( labelVO.getId() == null ) {
            Label label = new Label( labelVO.getName(), labelVO.getColour(), labelVO.getIsShown() );
            this.create( label );
            return label;
        } else {
            return this.load( labelVO.getId() );
        }
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Label> getLabelsMatching( String partialName ) {
        return currentSession().createQuery( "select label from Label as label where label.name like :partialName" )
                .setParameter( "partialName", "%" + partialName + "%" ).list();
    }

    @Override
    public Collection<Label> getSubjectLabels() {
        Collection<BigInteger> labelIds = currentSession().createSQLQuery(
                "select distinct LABEL_FK from SUBJECT_LABEL" ).list();
        Collection<Long> ids = new ArrayList<Long>();
        for ( BigInteger labelId : labelIds ) {
            ids.add( labelId.longValue() );
        }
        return load( ids );
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Label> getSubjectLabelsByProjectId( Long projectId ) {

        String sqlString = "select distinct LABEL_FK from SUBJECT_LABEL sl, SUBJECT_PROJECTS sp, SUBJECT s WHERE sl.SUBJECT_FK = s.ID AND sp.SUBJECT_ID = s.ID AND sp.PROJECT_ID = :projectId ";
        Query query = currentSession().createSQLQuery( sqlString );
        query.setLong( "projectId", projectId );

        Collection<BigInteger> labelIds = query.list();

        Collection<Long> ids = new ArrayList<Long>();
        for ( BigInteger labelId : labelIds ) {
            ids.add( labelId.longValue() );
        }
        return load( ids );
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Label> getVariantLabelsByProjectId( Long projectId ) {

        String sqlString = "select distinct LABEL_FK from VARIANT_LABEL vl, SUBJECT_PROJECTS sp, SUBJECT s, VARIANT v WHERE vl.VARIANT_FK = v.ID AND v.PATIENT_ID = s.ID AND sp.SUBJECT_ID = s.ID AND sp.PROJECT_ID = :projectId ";
        Query query = currentSession().createSQLQuery( sqlString );
        query.setLong( "projectId", projectId );

        Collection<BigInteger> labelIds = query.list();

        Collection<Long> ids = new ArrayList<Long>();
        for ( BigInteger labelId : labelIds ) {
            ids.add( labelId.longValue() );
        }
        return load( ids );
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Label> getSubjectLabelsBySubjectId( Long id ) {

        String sqlString = "select distinct LABEL_FK from SUBJECT_LABEL sl WHERE sl.SUBJECT_FK = :subjectId ";
        Query query = currentSession().createSQLQuery( sqlString );
        query.setLong( "subjectId", id );

        Collection<BigInteger> labelIds = query.list();

        Collection<Long> ids = new ArrayList<Long>();
        for ( BigInteger labelId : labelIds ) {
            ids.add( labelId.longValue() );
        }
        return load( ids );
    }

    @Override
    public Collection<Label> getVariantLabels() {
        Collection<BigInteger> labelIds = currentSession().createSQLQuery(
                "select distinct LABEL_FK from VARIANT_LABEL" ).list();
        Collection<Long> ids = new ArrayList<Long>();
        for ( BigInteger labelId : labelIds ) {
            ids.add( labelId.longValue() );
        }
        return load( ids );
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Label> getVariantLabelsByVariantId( Long id ) {

        String sqlString = "select distinct LABEL_FK from VARIANT_LABEL vl WHERE vl.VARIANT_FK = :variantId ";
        Query query = currentSession().createSQLQuery( sqlString );
        query.setLong( "variantId", id );

        Collection<BigInteger> labelIds = query.list();

        Collection<Long> ids = new ArrayList<Long>();
        for ( BigInteger labelId : labelIds ) {
            ids.add( labelId.longValue() );
        }
        return load( ids );
    }
}
