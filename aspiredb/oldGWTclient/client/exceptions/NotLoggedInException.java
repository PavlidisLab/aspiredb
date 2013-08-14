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

package ubc.pavlab.aspiredb.client.exceptions;

import java.io.Serializable;

/**
 * TODO Document Me
 * 
 * @author cmcdonald
 * @version $Id: NotLoggedInException.java,v 1.2 2012/10/23 21:13:41 cmcdonald Exp $
 */
public class NotLoggedInException extends Exception implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 6417632929132260139L;

    public NotLoggedInException() {
    }

}