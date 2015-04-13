package ubc.pavlab.aspiredb.server.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.shared.LabelValueObject;
import ubc.pavlab.aspiredb.shared.query.PhenotypeFilterConfig;
import ubc.pavlab.aspiredb.shared.query.ProjectOverlapFilterConfig;

public interface VariantDao extends VariantDaoBase<Variant>, RemotePaging<Variant> {

    /**
     * Keys for {@link VariantDao#getSubjectVariantIdsByPhenotype(PhenotypeFilterConfig) }
     */
    public static final int SUBJECT_IDS_KEY = 0;
    public static final int VARIANT_IDS_KEY = 1;

    public Collection<Variant> findByLabel( LabelValueObject label );

    public List<Variant> findByPhenotype( PhenotypeFilterConfig filterConfig );

    public Variant findByUserVariantId( String userVariantId, String patientId );

    public List<Long> getProjectOverlapVariantIds( ProjectOverlapFilterConfig overlapFilter );

    public Map<Integer, Collection<Long>> getSubjectVariantIdsByPhenotype( PhenotypeFilterConfig filterConfig );
}
