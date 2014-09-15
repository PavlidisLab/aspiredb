/*
 * The aspiredb project
 * 
 * Copyright (c) 2014 University of British Columbia
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

package ubc.pavlab.aspiredb.server.security.authorization.acl;

import gemma.gsec.acl.afterinvocation.ByAssociationFilteringProvider;

import java.util.List;

import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.Permission;

import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Variant;

/**
 * For this particular AfterInvocationProvider, characteristic authorization is determined based on the secured Project
 * acl. ie. characteristic security is determined from an owning project's security.
 * 
 * @author ptan
 * @version $Id$
 */
public class AclAfterCollectionCharacteristicByProjectFilter extends ByAssociationFilteringProvider<Project, Object> {

    private static final String CONFIG_ATTRIBUTE = "AFTER_ACL_CHARACTERISTIC_COLLECTION_READ";

    public AclAfterCollectionCharacteristicByProjectFilter( AclService aclService, List<Permission> requirePermission ) {
        super( aclService, CONFIG_ATTRIBUTE, requirePermission );
    }

    @Override
    public String getProcessConfigAttribute() {
        return CONFIG_ATTRIBUTE;
    }

    /**
     * @param targetDomainObject
     * @return
     */
    @Override
    protected Project getAssociatedSecurable( Object targetDomainObject ) {

        if ( Variant.class.isAssignableFrom( targetDomainObject.getClass() ) ) {
            return ( ( Variant ) targetDomainObject ).getSubject().getProjects().iterator().next();
        }

        throw new IllegalArgumentException( "Don't know how to filter a "
                + targetDomainObject.getClass().getSimpleName() );
    }

}