/*
 * The aspiredb project
 * 
 * Copyright (c) 2013 University of British Columbia
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

package ubc.pavlab.aspiredb.client.listener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.ContextLoaderListener;

import ubc.pavlab.aspiredb.server.util.ConfigUtils;

/**
 * StartupListener class used to initialize the spring context and make it available to the servlet context, so filters
 * that need the spring context can be configured. Modified from Gemma code. It also fills in parameters used by the application:
 * <ul>
 * <li>Google analytics tracking
 * </ul>
 * 
 * @author ptan
 * @author Matt Raible (original version)
 * @version $Id: StartupListener.java,v 1.1 2013/07/23 23:19:33 ptan Exp $
 */
public class StartupListener extends ContextLoaderListener {

    private static final Log log = LogFactory.getLog( StartupListener.class );

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.web.context.ContextLoaderListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized( ServletContextEvent event ) {
        log.info( "Initializing application context..." );
        StopWatch sw = new StopWatch();
        sw.start();

        // call Spring's context ContextLoaderListener to initialize
        // all the context files specified in web.xml
        super.contextInitialized( event );
        ServletContext servletContext = event.getServletContext();
        Map<String, Object> config = initializeConfiguration( servletContext );
        servletContext.setAttribute( ConfigUtils.CONFIG_NAME, config );

        sw.stop();
        double time = sw.getTime() / 1000.00;
        log.info( "Initialization of ASPIREdb Spring context in " + time + " s " );
    }

    /**
     * Loads config entries from {@link ConfigUtils}
     * 
     * @param context
     * @return
     */
    private Map<String, Object> initializeConfiguration( ServletContext context ) {
        // Check if the config
        // object already exists
        @SuppressWarnings("unchecked")
        Map<String, Object> config = ( Map<String, Object> ) context.getAttribute( ConfigUtils.CONFIG_NAME );

        if ( config == null ) {
            config = new HashMap<String, Object>();

            for ( @SuppressWarnings("unchecked")
            Iterator<String> it = ( Iterator<String> ) ConfigUtils.getKeys(); it.hasNext(); ) {
                String key = it.next();
                config.put( key, ConfigUtils.getProperty( key ) );
            }
        }

        return config;
    }

}
