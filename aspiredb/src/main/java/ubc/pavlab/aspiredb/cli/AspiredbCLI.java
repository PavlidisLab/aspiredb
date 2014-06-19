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

package ubc.pavlab.aspiredb.cli;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import ubc.pavlab.aspiredb.server.util.ConfigUtils;

/**
 * Generic command information for aspiredb. This doesn't do anything but print some help.
 * 
 * @author ptan
 * @version $Id: AspiredbCLI.java,v 1.1 2013/07/18 23:04:30 ptan Exp $
 */
public class AspiredbCLI {

    private static final String[] apps = new String[] { "ubc.pavlab.aspiredb.cli.GroupManagerCLI",
            "ubc.pavlab.aspiredb.cli.PhenotypeUploadCLI", "ubc.pavlab.aspiredb.cli.ProjectManagerCLI",
            "ubc.pavlab.aspiredb.cli.VariantUploadCLI" };

    /**
     * @param args
     */
    public static void main( String[] args ) {
        System.err.println( "============ ASPIREdb command line tools ============" );
        System.err.print( "ASPIREdb version " + ConfigUtils.getAppVersion() + "\n\n");

        System.err
                .print( "You've evoked the ASPIREdb CLI in a mode that doesn't do anything.\n"
                        + "To operate ASPIREdb tools, run a command like:\n\njava [jre options] -classpath /path/to/aspiredbCLI.jar <classname> [options]\n\n"
                        + "Here is a list of the classnames for some available tools:\n\n" );
        Arrays.sort( apps );
        for ( String a : apps ) {
            String desc = "";
            try {
                Class<?> aclazz = Class.forName( a );
                Object cliinstance = aclazz.newInstance();
                Method method = aclazz.getMethod( "getShortDesc", new Class[] {} );
                desc = ( String ) method.invoke( cliinstance, new Object[] {} );
            } catch ( ClassNotFoundException e ) {
                e.printStackTrace();
            } catch ( IllegalArgumentException e ) {
                e.printStackTrace();
            } catch ( IllegalAccessException e ) {
                e.printStackTrace();
            } catch ( InvocationTargetException e ) {
                e.printStackTrace();
            } catch ( SecurityException e ) {
                e.printStackTrace();
            } catch ( NoSuchMethodException e ) {
                e.printStackTrace();
            } catch ( InstantiationException e ) {
                e.printStackTrace();
            }

            System.err.println( a + " :\t" + desc );
        }
        System.err
                .println( "\nTo get help for a specific tool, use \n\njava -classpath /path/to/aspiredbCLI.jar <classname> --help" );
        System.err.print( "\n" + AbstractCLI.FOOTER + "\n=========================================\n" );

    }

}
