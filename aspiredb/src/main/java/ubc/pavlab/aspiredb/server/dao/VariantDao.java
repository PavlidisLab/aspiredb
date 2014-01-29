package ubc.pavlab.aspiredb.server.dao;

import org.springframework.security.access.annotation.Secured;
import ubc.pavlab.aspiredb.server.model.Variant;
import ubc.pavlab.aspiredb.shared.LabelValueObject;
import ubc.pavlab.aspiredb.shared.query.ProjectOverlapFilterConfig;

import java.util.Collection;
import java.util.List;

public interface VariantDao extends VariantDaoBase<Variant>, RemotePaging<Variant> {
    
    @Secured({ "GROUP_USER", "AFTER_ACL_READ" })
    public Variant findByUserVariantId( String userVariantId, String patientId);

    @Secured({ "GROUP_USER", "AFTER_ACL_READ" })
    public Collection<Variant> findByLabel( LabelValueObject label);
    
    @Secured({"GROUP_USER"})
    public List<Long> getProjectOverlapVariantIds(ProjectOverlapFilterConfig overlapFilter);

}
