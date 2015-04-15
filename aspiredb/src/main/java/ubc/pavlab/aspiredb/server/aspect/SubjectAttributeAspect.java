/*
 * The aspiredb project
 * 
 * Copyright (c) 2015 University of British Columbia
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

package ubc.pavlab.aspiredb.server.aspect;

import gemma.gsec.SecurityService;
import gemma.gsec.authentication.UserManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.SubjectAttribute;

/**
 * Check permissions for any unsecured objects that implements the SubjectAttribute interface during update() and
 * remove() (e.g. Phenotype and Variant). Also see AclAfterSubjectAttributeCollectionFilter.
 * 
 * @author ptan
 * @version $Id$
 */
@Aspect
public class SubjectAttributeAspect {
    private static Log log = LogFactory.getLog( SubjectAttributeAspect.class );

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserManager userManager;

    @Pointcut("execution(* ubc.pavlab.aspiredb.server.dao.DaoBase.update(..))")
    private void anyDaoUpdateOperation() {
    }

    @Pointcut("execution(* ubc.pavlab.aspiredb.server.dao.DaoBase.remove(..))")
    private void anyDaoRemoveOperation() {
    }

    /**
     * Throw an exception if the current user has no permission to modify the object.
     * 
     * @param pjp
     * @param obj
     * @throws Throwable
     */
    @Around("(anyDaoUpdateOperation() || anyDaoRemoveOperation()) && args(obj) ")
    public void checkUpdateRemove( ProceedingJoinPoint pjp, Object obj ) throws Throwable {

        if ( isViewableByUser( getSubject( obj ), userManager.getCurrentUsername(), true ) ) {
            pjp.proceed( pjp.getArgs() );
        }
    }

    private Subject getSubject( Object obj ) {
        if ( SubjectAttribute.class.isAssignableFrom( obj.getClass() ) ) {
            SubjectAttribute sa = ( SubjectAttribute ) obj;
            return sa.getSubject();
        }
        return null;
    }

    /**
     * Helper function that performs the permission verification. This can be slow.
     * 
     * @param obj
     * @param username
     * @param throwException if true, an AccessDeniedException is thrown, otherwise no exception is thrown
     * @return
     * @throws AccessDeniedException
     */
    private boolean isViewableByUser( Subject subject, String username, boolean throwException )
            throws AccessDeniedException {

        // if we have a variant without a subject, the variant was probably created in a unit test
        if ( subject == null ) {
            return true;
        }

        try {
            return securityService.isViewableByUser( subject, username );
        } catch ( AccessDeniedException e ) {
            if ( throwException ) {
                throw ( e );
            }
            return false;
        }
    }

}
