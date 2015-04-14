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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.time.StopWatch;
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
    @Pointcut("execution(java.util.Collection<ubc.pavlab.aspiredb.server.model.Variant> ubc.pavlab.aspiredb.server.dao.VariantDao*.*(..))")
    private void variantDaoOperation() {
    }

    @Pointcut("execution(java.util.Collection<ubc.pavlab.aspiredb.server.model.Phenotype> ubc.pavlab.aspiredb.server.dao.PhenotypeDao*.*(..))")
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

        if ( isViewableByUser( getSubject( obj ), userManager.getCurrentUsername(), true ) ) {
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
    @Around("variantDaoOperation() || phenotypeDaoOperation()")
    public Object filterSubjectPermission( ProceedingJoinPoint pjp ) throws Throwable {

        Object rawResults = pjp.proceed();

        String username = userManager.getCurrentUsername();

        if ( rawResults == null ) {
            return rawResults;
        }

        if ( Collection.class.isAssignableFrom( rawResults.getClass() ) ) {

            Collection<Object> results = new ArrayList<>();
            Collection<Object> list = ( Collection<Object> ) rawResults;

            if ( list.size() == 0 ) {
                return rawResults;
            }

            // group objects by subject so we avoid checking permissions for each object
            Map<Subject, Collection<Object>> subjectObject = new HashMap<>();
            for ( Object obj : list ) {
                Subject subject = getSubject( obj );

                // no subject so no permissions to check!
                if ( subject == null ) {
                    results.add( obj );
                    continue;
                }

                if ( !subjectObject.containsKey( subject ) ) {
                    subjectObject.put( subject, new ArrayList<>() );
                }
                subjectObject.get( subject ).add( obj );
            }

            StopWatch timer = new StopWatch();
            timer.start();

            // check subject permissions
            // TODO: maybe only check the first one for speedup?
            int i = 0;
            for ( Subject subject : subjectObject.keySet() ) {

                if ( !isViewableByUser( subject, username, false ) ) {
                    continue;
                }

                results.addAll( subjectObject.get( subject ) );

                if ( timer.getTime() > 10000 ) {
                    String percent = String.format( "%.2f", new Double( 100.00 * i / subjectObject.keySet().size() ) );
                    log.info( "checking permissions for " + i + "/" + subjectObject.keySet().size() + "(" + percent
                            + "%) subjects ... " );
                    timer.reset();
                    timer.start();
                }

                i++;

            }

            if ( rawResults instanceof Page ) {
                return new PageBean( results, results.size() );
            }

            return results;

        } else {

            // not a collection but a single object
            if ( isViewableByUser( getSubject( rawResults ), username, false ) ) {
                return rawResults;
            } else {
                return null;
            }
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
