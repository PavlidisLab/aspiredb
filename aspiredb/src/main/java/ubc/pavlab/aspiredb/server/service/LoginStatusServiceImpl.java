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
package ubc.pavlab.aspiredb.server.service;

import gemma.gsec.util.SecurityUtil;

import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.stereotype.Service;

/**
 * Simple service to find out if current user is logged in
 * 
 * @author cmcdonald
 * @version $Id: LoginStatusServiceImpl.java,v 1.8 2013/07/16 23:05:06 ptan Exp $
 */
@Service("loginStatusService")
@RemoteProxy(name = "LoginStatusService")
public class LoginStatusServiceImpl implements LoginStatusService {

    @Override
    @RemoteMethod
    public String getCurrentUsername() {
        return SecurityUtil.getCurrentUsername();
    }

    @Override
    public Boolean isLoggedIn() {
        return SecurityUtil.isUserLoggedIn();
    }

    @Override
    @RemoteMethod
    public Boolean isUserAdministrator() {
        return SecurityUtil.isUserAdmin();
    }
}
