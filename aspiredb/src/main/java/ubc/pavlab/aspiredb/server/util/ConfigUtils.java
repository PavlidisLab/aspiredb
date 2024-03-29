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

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;

import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;
import ubc.pavlab.aspiredb.shared.query.SubjectFilterConfig;
import ubc.pavlab.aspiredb.shared.query.VariantFilterConfig;

/**
 * Convenience class to access aspiredb properties defined in a resource. Methods will look in aspiredb.properties,
 * project.properties, build.properties and in the system properties, modified from Gemma code.
 * 
 * @author pavlidis
 * @version $Id: ConfigUtils.java,v 1.4 2013/07/23 23:19:33 ptan Exp $
 */
@RemoteProxy
public class ConfigUtils {

    public static final String CONFIG_NAME = "appConfig";

    /**
     * Name of the resource that is used to configure aspiredb internally.
     */
    private static final String BUILTIN_CONFIGURATION = "project.properties";

    private static CompositeConfiguration config;

    /**
     * Name of the resource containing defaults that the user can override (classpath)
     */
    private static final String DEFAULT_CONFIGURATION = "default.properties";

    private static Log log = LogFactory.getLog( ConfigUtils.class.getName() );

    /**
     * The name of the file users can use to configure aspiredb.
     */
    private static final String USER_CONFIGURATION = "aspiredb.properties";

    /**
     * The version of aspiredb.
     */
    private static final String VERSION_CONFIGURATION = "version.properties";

    static {

        config = new CompositeConfiguration();
        config.addConfiguration( new SystemConfiguration() );

        /*
         * the order matters - first come, first serve. Items added later do not overwrite items defined earlier. Thus
         * the user configuration has to be listed first.
         */

        try {
            PropertiesConfiguration pc = new PropertiesConfiguration();
            FileHandler handler = new FileHandler( pc );
            handler.setFileName( USER_CONFIGURATION );
            handler.load();
            config.addConfiguration( pc );
        } catch ( ConfigurationException e ) {
            // hmm, this is pretty much required.
            log.warn( USER_CONFIGURATION + " not found" );
        }

        try {
            // Default comes first.
            PropertiesConfiguration pc = new PropertiesConfiguration();
            FileHandler handler = new FileHandler( pc );
            handler.setFileName( DEFAULT_CONFIGURATION );
            handler.load();
            config.addConfiguration( pc );
        } catch ( ConfigurationException e ) {
            // hmm, this is pretty much required.
            log.warn( DEFAULT_CONFIGURATION + " not found" );
        }

        try {
            PropertiesConfiguration pc = new PropertiesConfiguration();
            FileHandler handler = new FileHandler( pc );
            handler.setFileName( BUILTIN_CONFIGURATION );
            handler.load();
            config.addConfiguration( pc );
        } catch ( ConfigurationException e ) {
            // that's okay, but warn
            log.warn( BUILTIN_CONFIGURATION + " not found" );
        }

        try {
            PropertiesConfiguration pc = new PropertiesConfiguration();
            FileHandler handler = new FileHandler( pc );
            handler.setFileName( VERSION_CONFIGURATION );
            handler.load();
            config.addConfiguration( pc );
        } catch ( ConfigurationException e ) {
            log.debug( "version.properties not found" );
        }

        // step through the result and do a final round of variable substitution
        for ( Iterator<String> it = config.getKeys(); it.hasNext(); ) {
            String key = it.next();
            String property = config.getString( key );
            if ( property != null && property.startsWith( "${" ) && property.endsWith( "}" ) ) {
                String keyToSubstitute = property.substring( 2, property.length() - 1 );
                String valueToSubstitute = config.getString( keyToSubstitute );
                log.debug( key + "=" + property + " -> " + valueToSubstitute );
                config.setProperty( key, valueToSubstitute );
            }
        }

        if ( log.isDebugEnabled() ) {
            log.debug( "********** Configuration details ***********" );
            for ( Iterator<?> it = config.getKeys(); it.hasNext(); ) {
                String key = ( String ) it.next();
                Object prop = config.getProperty( key );
                log.debug( key + " = " + prop );
            }
            log.debug( "********** End of configuration details ***********" );
        }

    }

    public static String getAdminEmailAddress() {
        return getString( "aspiredb.admin.email" );
    }

    @RemoteMethod
    public static String getAppVersion() {
        return getString( "aspiredb.version" );
    }

    /**
     * @return the configured base url (e.g., http://aspiredb.msl.ubc.ca/). It will always end in a slash.
     */
    public static String getBaseUrl() {
        String url = getString( "aspiredb.baseurl", getHostUrl() + getRootContext() + "/" );
        if ( !url.endsWith( "/" ) ) {
            return url + "/";
        }
        return url;
    }
    /**
     * @return host url e.g. http://aspiredb.msl.ubc.ca
     */
    public static String getHostUrl() {
        String host = getString( "aspiredb.hosturl", "http://aspiredb.msl.ubc.ca" );
        if ( host.length() > 1 && host.endsWith( "/" ) ) {
            return host.substring(0, host.length() - 1);
        }
        return host;
    }

    /**
     * @return root context e.g. /aspiredb
     */
    public static String getRootContext() {
        String ctx = getString( "aspiredb.rootcontext", "" );
        if (ctx.isEmpty() || ctx.equals( "/" ) ) {
            return "";
        }
        if ( !ctx.startsWith( "/" ) ) {
            ctx = "/" + ctx;
        }
        if ( ctx.length() > 1 && ctx.endsWith( "/" ) ) {
            return ctx.substring(0, ctx.length() - 1);
        }
        return ctx;
    }

    /**
     * @param key
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getBigDecimal(java.lang.String)
     */
    public static BigDecimal getBigDecimal( String key ) {
        return config.getBigDecimal( key );
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getBigDecimal(java.lang.String, java.math.BigDecimal)
     */
    public static BigDecimal getBigDecimal( String key, BigDecimal defaultValue ) {
        return config.getBigDecimal( key, defaultValue );
    }

    /**
     * @param key
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getBigInteger(java.lang.String)
     */
    public static BigInteger getBigInteger( String key ) {
        return config.getBigInteger( key );
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getBigInteger(java.lang.String, java.math.BigInteger)
     */
    public static BigInteger getBigInteger( String key, BigInteger defaultValue ) {
        return config.getBigInteger( key, defaultValue );
    }

    /**
     * @param key
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getBoolean(java.lang.String)
     */
    public static boolean getBoolean( String key ) {
        try {
            return config.getBoolean( key );
        } catch ( NoSuchElementException nsee ) {
            log.info( key + " is not configured, returning default value of false" );
            return false;
        }

    }

    /**
     * @param key
     * @param defaultValue
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getBoolean(java.lang.String, boolean)
     */
    public static boolean getBoolean( String key, boolean defaultValue ) {
        return config.getBoolean( key, defaultValue );
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getBoolean(java.lang.String, java.lang.Boolean)
     */
    public static Boolean getBoolean( String key, Boolean defaultValue ) {
        return config.getBoolean( key, defaultValue );
    }

    /**
     * @param key
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getByte(java.lang.String)
     */
    public static byte getByte( String key ) {
        try {
            return config.getByte( key );
        } catch ( NoSuchElementException nsee ) {
            log.info( key + " is not configured, returning default value of 1" );
            return 1;
        }
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getByte(java.lang.String, byte)
     */
    public static byte getByte( String key, byte defaultValue ) {
        return config.getByte( key, defaultValue );
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getByte(java.lang.String, java.lang.Byte)
     */
    public static Byte getByte( String key, Byte defaultValue ) {
        return config.getByte( key, defaultValue );
    }

    /**
     * @param index
     * @return
     * @see org.apache.commons.configuration2.CompositeConfiguration#getConfiguration(int)
     */
    public static Configuration getConfiguration( int index ) {
        return config.getConfiguration( index );
    }

    /**
     * The default value given if none is defined is AND.
     * 
     * @return
     */
    public static String getDefaultSearchOperator() {
        return getString( "gemma.search.defaultOperator", "AND" );
    }

    /**
     * @param key
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getDouble(java.lang.String)
     */
    public static double getDouble( String key ) {
        try {
            return config.getDouble( key );
        } catch ( NoSuchElementException nsee ) {
            log.info( key + " is not configured, returning default value of 1" );
            return 1;
        }
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getDouble(java.lang.String, double)
     */
    public static double getDouble( String key, double defaultValue ) {
        return config.getDouble( key, defaultValue );
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getDouble(java.lang.String, java.lang.Double)
     */
    public static Double getDouble( String key, Double defaultValue ) {
        return config.getDouble( key, defaultValue );
    }

    /**
     * @return The local directory where files downloaded/uploaded are stored. It will end in a file separator ("/" on
     *         unix).
     */
    public static String getDownloadPath() {
        String val = getString( "aspiredb.download.path" );
        if ( val.endsWith( File.separator ) ) {
            return val;
        }
        return val + File.separatorChar;
    }

    /**
     * @param key
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getFloat(java.lang.String)
     */
    public static float getFloat( String key ) {
        try {
            return config.getFloat( key );
        } catch ( NoSuchElementException nsee ) {
            log.info( key + " is not configured, returning default value of 1" );
            return 1;
        }
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getFloat(java.lang.String, float)
     */
    public static float getFloat( String key, float defaultValue ) {
        return config.getFloat( key, defaultValue );
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getFloat(java.lang.String, java.lang.Float)
     */
    public static Float getFloat( String key, Float defaultValue ) {
        return config.getFloat( key, defaultValue );
    }

    public static String getGemmaBaseUrl() {
        String url = getString( "gemma.base.url", "https://gemma.msl.ubc.ca" );

        return url;
    }

    /**
     * @return
     * @see org.apache.commons.configuration2.CompositeConfiguration#getInMemoryConfiguration()
     */
    public static Configuration getInMemoryConfiguration() {
        return config.getInMemoryConfiguration();
    }

    /**
     * @param key
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getInt(java.lang.String)
     */
    public static int getInt( String key ) {
        try {
            return config.getInt( key );
        } catch ( NoSuchElementException nsee ) {
            log.info( key + " is not configured, returning default value of 1" );
            return 1;
        }
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getInt(java.lang.String, int)
     */
    public static int getInt( String key, int defaultValue ) {
        return config.getInt( key, defaultValue );
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getInteger(java.lang.String, java.lang.Integer)
     */
    public static Integer getInteger( String key, Integer defaultValue ) {
        return config.getInteger( key, defaultValue );
    }

    /**
     * @return
     * @see org.apache.commons.configuration2.CompositeConfiguration#getKeys()
     */
    public static Iterator<String> getKeys() {
        return config.getKeys();
    }

    /**
     * @param key
     * @return
     * @see org.apache.commons.configuration2.CompositeConfiguration#getKeys(java.lang.String)
     */
    public static Iterator<?> getKeys( String key ) {
        return config.getKeys( key );
    }

    /**
     * @param key
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getList(java.lang.String)
     */
    public static List<?> getList( String key ) {

        try {
            return config.getList( key );

        } catch ( NoSuchElementException nsee ) {
            log.info( key + " is not configured, returning empty arrayList" );
            return new ArrayList<Object>();
        }
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     * @see org.apache.commons.configuration2.CompositeConfiguration#getList(java.lang.String, java.util.List)
     */
    public static List<Object> getList( String key, List<Object> defaultValue ) {
        return config.getList( key, defaultValue );
    }

    /**
     * @param key
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getLong(java.lang.String)
     */
    public static long getLong( String key ) {
        try {
            return config.getLong( key );
        } catch ( NoSuchElementException nsee ) {
            log.info( key + " is not configured, returning default value of 1" );
            return 1;
        }
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getLong(java.lang.String, long)
     */
    public static long getLong( String key, long defaultValue ) {
        return config.getLong( key, defaultValue );
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getLong(java.lang.String, java.lang.Long)
     */
    public static Long getLong( String key, Long defaultValue ) {
        return config.getLong( key, defaultValue );
    }

    /**
     * @return
     * @see org.apache.commons.configuration2.CompositeConfiguration#getNumberOfConfigurations()
     */
    public static int getNumberOfConfigurations() {
        return config.getNumberOfConfigurations();
    }

    /**
     * @param key
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getProperties(java.lang.String)
     */
    public static Properties getProperties( String key ) {
        return config.getProperties( key );
    }

    /**
     * @param key
     * @param defaults
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getProperties(java.lang.String, java.util.Properties)
     */
    public static Properties getProperties( String key, Properties defaults ) {
        return config.getProperties( key, defaults );
    }

    /**
     * @param key
     * @return
     * @see org.apache.commons.configuration2.CompositeConfiguration#getProperty(java.lang.String)
     */
    public static Object getProperty( String key ) {
        return config.getProperty( key );
    }

    /**
     * @param key
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getShort(java.lang.String)
     */
    public static short getShort( String key ) {
        try {
            return config.getShort( key );

        } catch ( NoSuchElementException nsee ) {
            log.info( key + " is not configured, returning default value of 1" );
            return 1;
        }
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getShort(java.lang.String, short)
     */
    public static short getShort( String key, short defaultValue ) {
        return config.getShort( key, defaultValue );
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getShort(java.lang.String, java.lang.Short)
     */
    public static Short getShort( String key, Short defaultValue ) {
        return config.getShort( key, defaultValue );
    }

    /**
     * @param key
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getString(java.lang.String)
     */
    public static String getString( String key ) {
        try {
            return config.getString( key );
        } catch ( NoSuchElementException nsee ) {
            log.info( key + " is not configured, returning empty string" );
            return "";
        }
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     * @see org.apache.commons.configuration2.AbstractConfiguration#getString(java.lang.String, java.lang.String)
     */
    public static String getString( String key, String defaultValue ) {
        return config.getString( key, defaultValue );
    }

    /**
     * @param key
     * @return
     * @see org.apache.commons.configuration2.CompositeConfiguration#getStringArray(java.lang.String)
     */
    public static String[] getStringArray( String key ) {
        try {
            return config.getStringArray( key );
        } catch ( NoSuchElementException nsee ) {
            log.info( key + " is not configured, returning default value of null" );
            return null;
        }
    }

    /**
     * Returns true if filters has an instance of {@link SubjectFilterConfig}
     * 
     * @param filters
     * @return
     */
    public static boolean hasSubjectConfig( Set<AspireDbFilterConfig> filters ) {
        boolean found = false;
        for ( AspireDbFilterConfig cfg : filters ) {
            if ( cfg instanceof SubjectFilterConfig ) {
                return true;
            }
        }
        return found;
    }

    /**
     * Returns true if filters has an instance of {@link SubjectFilterConfig}
     * 
     * @param filters
     * @return
     */
    public static boolean hasVariantConfig( Set<AspireDbFilterConfig> filters ) {
        boolean found = false;
        for ( AspireDbFilterConfig cfg : filters ) {
            if ( cfg instanceof VariantFilterConfig ) {
                return true;
            }
        }
        return found;
    }

    /**
     * Set an environment/application variable programatically.
     * 
     * @param key
     * @param value
     */
    public static void setProperty( String key, Object value ) {
        config.setProperty( key, value );
    }

}
