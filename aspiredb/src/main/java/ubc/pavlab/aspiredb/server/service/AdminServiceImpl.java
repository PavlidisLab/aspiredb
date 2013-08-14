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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ubc.pavlab.aspiredb.server.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.server.util.CacheMonitor;

/**
 * @author cmcdonald Threw this in for some easy debugging, flesh out later with
 *         more useful functions
 * 
 */
@Service("adminService")
public class AdminServiceImpl extends GwtService implements AdminService {

	@Autowired
	private CacheMonitor cacheMonitor;

	@Override
	public String logCaches() throws NotLoggedInException {
		// TODO this should also test for whether user is admin eventually if it
		// does anything adminy
		throwGwtExceptionIfNotLoggedIn();

		return cacheMonitor.getStats();
	}
}