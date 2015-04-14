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
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.SubjectAttribute;

/**
 * Provides a way of checking the permissions for any unsecured objects that implements the SubjectAttribute interface
 * (e.g. Phenotype and Variant).
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

    @Pointcut("execution(* ubc.pavlab.aspiredb.server.dao.DaoBase.load*(..))")
    private void anyDaoLoadOperation() {
    }

    // @Pointcut("execution(* ubc.pavlab.aspiredb.server.dao.VariantDao*.*(..))")
    @Pointcut("execution(* ubc.pavlab.aspiredb.server.dao.VariantDao*.*(..))")
    private void variantDaoOperation() {
    }

    @Pointcut("execution(* ubc.pavlab.aspiredb.server.dao.PhenotypeDao*.*(..))")
    private void phenotypeDaoOperation() {
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

        if ( isViewableByUser( obj, userManager.getCurrentUsername(), true ) ) {
            pjp.proceed( pjp.getArgs() );
        }
    }

    /**
     * Remove those objects which the current user has no permission to.
     * 
     * @param pjp
     * @return
     * @throws Throwable
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Around("anyDaoLoadOperation() || variantDaoOperation() || phenotypeDaoOperation()")
    public Object filterSubjectPermission( ProceedingJoinPoint pjp ) throws Throwable {

        Object rawResults = pjp.proceed();

        String username = userManager.getCurrentUsername();

        if ( rawResults == null ) {
            return rawResults;
        }

        if ( Collection.class.isAssignableFrom( rawResults.getClass() ) ) {

            Collection<Object> results = new ArrayList<>();
            Collection<Object> list = ( Collection<Object> ) rawResults;
            for ( Object obj : list ) {
                if ( isViewableByUser( obj, username, false ) ) {
                    results.add( obj );
                }
            }

            if ( rawResults instanceof Page ) {
                return new PageBean( results, results.size() );
            }

            return results;

        } else {

            // not a collection but a single object
            if ( isViewableByUser( rawResults, username, false ) ) {
                return rawResults;
            } else {
                return null;
            }
        }
    }

    /**
     * Helper function that performs the permission verification.
     * 
     * @param obj
     * @param username
     * @param throwException if true, an AccessDeniedException is thrown, otherwise no exception is thrown
     * @return
     * @throws AccessDeniedException
     */
    private boolean isViewableByUser( Object obj, String username, boolean throwException )
            throws AccessDeniedException {
        if ( SubjectAttribute.class.isAssignableFrom( obj.getClass() ) ) {
            SubjectAttribute sa = ( SubjectAttribute ) obj;
            Subject subject = sa.getSubject();

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
        return true; // there's no way to check the permission, eg. with a String, so just allow
    }

}
