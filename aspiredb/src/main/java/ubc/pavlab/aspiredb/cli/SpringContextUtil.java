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
package ubc.pavlab.aspiredb.cli;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Methods to create Spring context. This is not used for webapp. It is normally used for CLIs only.
 * 
 * @author pavlidis
 */
public class SpringContextUtil {
    private static Log log = LogFactory.getLog( SpringContextUtil.class.getName() );

    private static BeanFactory ctx = null;

    /**
     * @param testing If true, it will get a test configured-BeanFactory
     * @param additionalConfigurationLocations, like "classpath*:/myproject/applicationContext-mine.xml"
     * @return BeanFactory or null if no context could be created.
     */
    public static BeanFactory getApplicationContext( boolean testing, String[] additionalConfigurationLocations ) {
        if ( ctx == null ) {
            String[] paths = getConfigLocations( testing );

            if ( additionalConfigurationLocations != null ) {
                paths = addPaths( additionalConfigurationLocations, paths );
            }

            StopWatch timer = new StopWatch();
            timer.start();
            ctx = new ClassPathXmlApplicationContext( paths );
            timer.stop();
            if ( ctx != null ) {
                log.info( "Got context in " + timer.getTime() + "ms" );
            } else {
                log.fatal( "Failed to load context!" );
            }
        }
        return ctx;
    }

    /**
     * @param additionalConfigurationLocations
     * @param paths
     * @return
     */
    private static String[] addPaths( String[] additionalConfigurationLocations, String[] paths ) {
        Object[] allPaths = ArrayUtils.addAll( paths, additionalConfigurationLocations );
        paths = new String[allPaths.length];
        for ( int i = 0; i < allPaths.length; i++ ) {
            paths[i] = ( String ) allPaths[i];
        }
        return paths;
    }

    /**
     * @param testing If true, it will get a test configured-BeanFactory
     * @return BeanFactory or null if no context could be created.
     */
    public static BeanFactory getApplicationContext( boolean testing ) {
        return getApplicationContext( testing, new String[] {} );
    }

    /**
     * Find the configuration file locations. The files must be in your class path for this to work.
     * 
     * @param testing - if true, it will use the test configuration.
     * @return
     * @see getApplicationContext
     */
    public static String[] getConfigLocations( boolean testing ) {
        if ( testing ) {
            return getTestConfigLocations();
        } else {
            return getStandardConfigLocations();
        }
    }

    /**
     * @param isWebapp
     * @param paths
     */
    private static void addCommonConfig( List<String> paths ) {
        paths.add( "classpath*:application-context.xml" );
        paths.add( "classpath*:applicationContext-security.xml" );
    }

    /**
     * @param compassOn
     * @param isWebapp
     * @return
     */
    private static String[] getStandardConfigLocations() {
        List<String> paths = new ArrayList<String>();
        paths.add( "classpath*:production-data-source.xml" );

        addCommonConfig( paths );
        return paths.toArray( new String[] {} );
    }

    /**
     * @param compassOn
     * @param isWebapp
     * @return
     */
    private static String[] getTestConfigLocations() {
        List<String> paths = new ArrayList<String>();
        paths.add( "classpath*:test-data-source.xml" );

        addCommonConfig( paths );
        return paths.toArray( new String[] {} );
    }

}
