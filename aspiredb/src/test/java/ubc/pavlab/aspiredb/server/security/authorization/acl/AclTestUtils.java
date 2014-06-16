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

package ubc.pavlab.aspiredb.server.security.authorization.acl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import gemma.gsec.acl.domain.AclObjectIdentity;
import gemma.gsec.acl.domain.AclService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.stereotype.Component;

/**
 * Methods for checking ACLs.
 * 
 * @author paul
 * @version $Id: AclTestUtils.java,v 1.3 2012/10/23 23:54:32 cmcdonald Exp $
 */
@Component
public class AclTestUtils {

    private static Log log = LogFactory.getLog( AclTestUtils.class );

    @Autowired
    private AclService aclService;

    /**
     * Make sure object f has no ACLs
     * 
     * @param f
     */
    public void checkDeletedAcl( Object f ) {
        try {
            Acl acl = getAcl( f );
            fail( "Failed to  delete ACL for " + f + ", got " + acl );
        } catch ( NotFoundException okaye ) {
            // okay
            if ( log.isDebugEnabled() ) {
                log.debug( "As expected, there was no acl for " + f.getClass().getSimpleName() );
            }
        }
    }

    public void checkHasAces( Object f ) {
        Acl a = getAcl( f );
        assertTrue( a + " doesn't have ACEs, it should", a.getEntries().size() > 0 );
    }

    public void checkHasAcl( Object f ) {
        try {
            aclService.readAclById( new AclObjectIdentity( f ) );
            log.debug( "Have acl for " + f );
        } catch ( NotFoundException okaye ) {
            fail( "Failed to create ACL for " + f );
        }
    }

    public void checkHasAclParent( Object f, Object parent ) {
        Acl parentAcl = getParentAcl( f );
        assertNotNull( "No ACL for parent of " + f, parentAcl );

        if ( parent != null ) {
            Acl b = getAcl( parent );
            assertEquals( b, parentAcl );
        }

        assertNotNull( parentAcl );

        log.debug( "ACL has correct parent for " + f + " <----- " + parentAcl.getObjectIdentity() );
    }

    public void checkLacksAces( Object f ) {
        Acl a = getAcl( f );
        assertTrue( f + " has ACEs, it shouldn't", a.getEntries().size() == 0 );
    }

    public Acl getAcl( Object f ) {
        Acl a = aclService.readAclById( new AclObjectIdentity( f ) );
        return a;
    }

    public Acl getParentAcl( Object f ) {
        Acl a = getAcl( f );
        Acl parentAcl = a.getParentAcl();
        return parentAcl;
    }

    public void update( MutableAcl acl ) {
        this.aclService.updateAcl( acl );
    }

}
