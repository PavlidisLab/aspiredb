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
package ubc.pavlab.aspiredb.server.controller;

import gemma.gsec.util.SecurityUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RemoteProxy
@RequestMapping("/SpecRunner.html")
public class SpecRunnerController {

    protected static Log log = LogFactory.getLog( SpecRunnerController.class );

    @RequestMapping(method = RequestMethod.GET)
    public String showSpecRunner( ModelMap model ) {

        if ( !SecurityUtil.isUserLoggedIn() ) {

            log.info( "User not logged in, redirecting to login page" );
            return "login";
        }

        return "specRunner";

    }

}