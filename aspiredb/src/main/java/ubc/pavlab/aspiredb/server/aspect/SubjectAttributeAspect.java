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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import ubc.pavlab.aspiredb.server.dao.Page;
import ubc.pavlab.aspiredb.server.dao.PageBean;
import ubc.pavlab.aspiredb.server.model.SubjectAttribute;

/**
 * Works similarly to gemma.gsec.acl.afterinvocation.AclEntryAfterInvocationCollectionFilteringProvider but looks at the
 * object's Subject for security permissions. Use with Phenotype and Variant objects.
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

    // @Pointcut("execution(* ubc.pavlab.aspiredb.server.dao.DaoBase*.update(..))")
    // private void anyDaoBaseUpdateOperation() {
    // }
    //
    //
    // @Before(value = "anyDaoBaseUpdateOperation() && target(ubc.pavlab.aspiredb.server.model.SubjectAttribute) ")
    // public void checkUpdate() throws Throwable {
    // log.info( "checkUpdate() " + pjp.getSignature().getName() + " result = " + rawResults );
    // }
    //

    @Pointcut("execution(* ubc.pavlab.aspiredb.server.dao.VariantDao*.*(..))")
    private void anyVariantDaoOperation() {
    }

    @Pointcut("execution(* ubc.pavlab.aspiredb.server.dao.PhenotypeDao*.*(..))")
    private void anyPhenotypeDaoOperation() {
    }

    @Around("anyVariantDaoOperation() || anyPhenotypeDaoOperation()")
    public Object filterSubjectPermission( ProceedingJoinPoint pjp ) throws Throwable {

        Object rawResults = pjp.proceed();

        String username = userManager.getCurrentUsername();
        log.info( "filterSubjectPermission() " + pjp.getSignature().getName() + " result = " + rawResults );

        if ( rawResults == null ) {
            return rawResults;
        }

        if ( Collection.class.isAssignableFrom( rawResults.getClass() ) ) {

            @SuppressWarnings("unchecked")
            Collection<Object> results = new ArrayList<>();
            Collection<Object> list = ( Collection<Object> ) rawResults;
            for ( Object obj : list ) {
                if ( isViewableByUser( obj, username ) ) {
                    results.add( obj );
                }
            }

            if ( rawResults instanceof Page ) {
                return new PageBean( results, results.size() );
            }

            return results;

        } else {

            // not a collection but a single object
            if ( isViewableByUser( rawResults, username ) ) {
                return rawResults;
            } else {
                return null;
            }
        }
    }

    private boolean isViewableByUser( Object obj, String username ) {
        if ( SubjectAttribute.class.isAssignableFrom( obj.getClass() ) ) {
            SubjectAttribute sa = ( SubjectAttribute ) obj;
            try {
                return securityService.isViewableByUser( sa.getSubject(), username );
            } catch ( AccessDeniedException e ) {
                return false;
            }
        }
        return true; // there's no way to check the permission, eg. with a String, so just allow
    }

}