package ubc.pavlab.aspiredb.server.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * A PropertyPlaceholderConfigurer that also can take a Configuration. I got the idea for this from {@link http
 * ://mail-archives
 * .apache.org/mod_mbox/jakarta-commons-dev/200603.mbox/%3Cbug-39068-7685@http.issues.apache.org/bugzilla/%3E}
 * <p>
 * Currently values in the configuration overrides any in the properties files.
 * 
 * @author pavlidis
 * @version $Id: CommonsConfigurationPropertyPlaceholderConfigurer.java,v 1.1 2013/07/24 00:43:22 paul Exp $
 * @see org.apache.commons.configuration2.Configuration
 * @see org.springframework.beans.factory.config.PropertyPlaceholderConfigurer
 */
public class CommonsConfigurationPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    Configuration configuration;

    /**
     * @param conf the conf to set
     */
    public void setConfiguration( PropertiesConfiguration configuration ) {
        this.configuration = configuration;
    }

    @Override
    protected Properties mergeProperties() throws IOException {
        Properties result = super.mergeProperties();

        // now load properties from user's configuration (Gemma.properties), to override the
        // earlier properties from the xml
        if ( this.configuration != null ) {
            for ( Iterator<String> it = configuration.getKeys(); it.hasNext(); ) {
                String key = it.next();
                result.setProperty( key, configuration.getString( key ) );
                logger.debug( key + "=" + configuration.getString( key ) );
            }
        } else {
            logger.warn( "Configuration was null" );
        }

        return result;
    }
}
