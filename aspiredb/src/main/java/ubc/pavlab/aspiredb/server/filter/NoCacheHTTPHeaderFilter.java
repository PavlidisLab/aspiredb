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

package ubc.pavlab.aspiredb.server.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * Add Headers to .nocache. files so that browser thinks they are stale when the server is updated
 */
public class NoCacheHTTPHeaderFilter implements Filter {

    public void doFilter( ServletRequest request, ServletResponse response, FilterChain filterChain )
            throws IOException, ServletException {

        HttpServletRequest httpRequest = ( HttpServletRequest ) request;
        String requestURI = httpRequest.getRequestURI();

        if ( requestURI.contains( ".nocache." ) ) {
            Date now = new Date();
            HttpServletResponse httpResponse = ( HttpServletResponse ) response;
            httpResponse.setDateHeader( "Date", now.getTime() );            
            httpResponse.setDateHeader( "Expires", now.getTime() - 86400000L );
            httpResponse.setHeader( "Pragma", "no-cache" );
            httpResponse.setHeader( "Cache-control", "no-cache, no-store, must-revalidate" );
        }

        filterChain.doFilter( request, response );
    }

    public void destroy() {
    }

    public void init( FilterConfig config ) throws ServletException {
    }
}