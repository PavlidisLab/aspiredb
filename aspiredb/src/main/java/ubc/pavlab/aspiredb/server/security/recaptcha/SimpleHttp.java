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
package ubc.pavlab.aspiredb.server.security.recaptcha;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SimpleHttp {

    public static String get( String url, String urlParameters ) {

        HttpURLConnection con = null;
        try {
            con = openConnection( url + "?" + urlParameters );

            // optional default is GET
            // con.setRequestMethod("GET");

            try (BufferedReader in = new BufferedReader( new InputStreamReader( con.getInputStream() ) )) {
                String response = bufferToString( in );
                return response;
            } catch (IOException e) {
                throw new ReCaptchaException( "I/O error receiving the response ", e );
            }

        } finally {
            if ( con != null ) {
                con.disconnect();
            }
        }

    }

    private static HttpURLConnection openConnection( String url ) {
        try {
            return (HttpURLConnection) new URL( url ).openConnection();
        } catch (IOException e) {
            throw new ReCaptchaException( "Unable to create URL for posting", e );
        }
    }

    private static String bufferToString( BufferedReader in ) throws IOException {
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append( inputLine );
        }
        return response.toString();
    }

}
