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

package ubc.pavlab.aspiredb.server.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;
import ubc.pavlab.aspiredb.shared.query.ProjectFilterConfig;
import ubc.pavlab.aspiredb.shared.query.SubjectFilterConfig;

/**
 * @author ptan
 * @version $Id$
 */
public class ConfigUtilsTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testHasSubjectConfig() {
        Set<AspireDbFilterConfig> filters = new HashSet<AspireDbFilterConfig>();
        assertFalse( ConfigUtils.hasSubjectConfig( filters ) );
        filters.add( new SubjectFilterConfig() );
        assertTrue( ConfigUtils.hasSubjectConfig( filters ) );
    }

    public static ProjectFilterConfig getProjectFilterConfigById( Project p ) {

        ProjectFilterConfig projectFilterConfig = new ProjectFilterConfig();

        List<Long> projectIds = new ArrayList<>();

        projectIds.add( p.getId() );

        projectFilterConfig.setProjectIds( projectIds );

        return projectFilterConfig;

    }

}
