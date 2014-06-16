package ubc.pavlab.aspiredb.server.dao;

import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.Query;
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
