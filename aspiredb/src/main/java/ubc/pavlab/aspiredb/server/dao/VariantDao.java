package ubc.pavlab.aspiredb.server.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.annotation.Secured;

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


    
    @Secured({"GROUP_USER"})
    public List<Long> getProjectOverlapVariantIds(ProjectOverlapFilterConfig overlapFilter);

    @Secured({"GROUP_USER", "AFTER_ACL_READ"})
    public Variant findByUserVariantId( String userVariantId, String patientId );


    @Secured({ "GROUP_USER", "AFTER_ACL_READ" })
    public Collection<Variant> findByLabel( LabelValueObject label );


    @Secured({ "GROUP_USER", "AFTER_ACL_READ" })
    public List<Variant> findByPhenotype( PhenotypeFilterConfig filterConfig );

    @Secured({ "GROUP_USER", "AFTER_ACL_READ" })
    public Map<Integer, Collection<Long>> getSubjectVariantIdsByPhenotype( PhenotypeFilterConfig filterConfig );
}
