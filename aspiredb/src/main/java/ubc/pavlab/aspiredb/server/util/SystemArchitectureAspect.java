/*
 * The aspiredb project
 * 
 * Copyright (c) 2012 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package ubc.pavlab.aspiredb.server.util;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * General-purpose pointcuts to recognize CRUD operations etc.
 * 
 * @author paul
 * @version $Id: SystemArchitectureAspect.java,v 1.1 2012/09/01 01:42:29 cmcdonald Exp $
 */
@Aspect
public class SystemArchitectureAspect {

    /*
     * 
     * For help with expressions see http://static.springsource.org/spring/docs/2.5.x/reference/aop.html#6.2.3.4
     */

    /**
     * Encompasses the 'web' packages
     */
    /*
     * @Pointcut("within(ubc.pavlab.aspiredb.server..*)") public void inWebLayer() { }
     */

    /**
     * Methods that create new objects in the persistent store
     */
    @Pointcut("ubc.pavlab.aspiredb.server.util.SystemArchitectureAspect.daoMethod() && ( execution(* save(..)) || execution(* create*(..)) || execution(* findOrCreate(..)) || execution(* persist*(..))   )")
    public void creator() {//
    }

    @Pointcut("ubc.pavlab.aspiredb.server.util.SystemArchitectureAspect.deleter() ||ubc.pavlab.aspiredb.server.util.SystemArchitectureAspect.loader() || ubc.pavlab.aspiredb.server.util.SystemArchitectureAspect.creator() || ubc.pavlab.aspiredb.server.util.SystemArchitectureAspect.updater() || ubc.pavlab.aspiredb.server.util.SystemArchitectureAspect.deleter()")
    public void crud() {//
    }

    /**
     * This pointcut is used to apply audit and acl advice at DAO boundary.
     */
    @Pointcut("@target(org.springframework.stereotype.Repository) && execution(public * ubc.pavlab.aspiredb.server..*.*(..))")
    public void daoMethod() {//
    }

    /**
     * Methods that delete items in the persistent store
     */
    @Pointcut("ubc.pavlab.aspiredb.server.util.SystemArchitectureAspect.daoMethod() && (execution(* remove(..)) || execution(* delete*(..)))")
    public void deleter() {//
    }

    /**
     * Encompasses the 'model' packages
     */
    @Pointcut("within(ubc.pavlab.aspiredb.server..*)")
    public void inModelLayer() {
    }

    /**
     * Methods that load (read) from the persistent store
     */
    @Pointcut("ubc.pavlab.aspiredb.server.util.SystemArchitectureAspect.daoMethod() && (execution(* load(..)) || execution(* loadAll(..)) || execution(* loadSubset(..)) || execution(* read(..)))")
    public void loader() {//
    }

    /**
     * Create, delete or update methods
     */
    @Pointcut("ubc.pavlab.aspiredb.server.util.SystemArchitectureAspect.creator() || ubc.pavlab.aspiredb.server.util.SystemArchitectureAspect.updater() || ubc.pavlab.aspiredb.server.util.SystemArchitectureAspect.deleter()")
    public void modifier() {//
    }

    /**
     * A entity service method: a public method in a \@Service.
     */
    @Pointcut("@target(org.springframework.stereotype.Service) && execution(public * ubc.pavlab.aspiredb.server..*.*(..))")
    public void serviceMethod() {
        /*
         * Important document:
         * http://forum.springsource.org/showthread.php?28525-Difference-between-target-and-within-in-Spring-AOP
         * 
         * Using @target makes a proxy out of everything, which causes problems if services aren't implementing
         * interfaces -- seems for InitializingBeans in particular. @within doesn't work, at least for the ACLs.
         */
    }

    @Pointcut("@target(org.springframework.stereotype.Service) && (execution(public * ubc.pavlab.aspiredb.server..*.*(*)) || execution(public * ubc.pavlab.aspiredb.server..*.*(*,..)))")
    public void serviceMethodWithArg() {//
    }

    /**
     * Methods that update items in the persistent store
     */
    @Pointcut("ubc.pavlab.aspiredb.server.util.SystemArchitectureAspect.daoMethod() && execution(* update(..))")
    public void updater() {//
    }

}
