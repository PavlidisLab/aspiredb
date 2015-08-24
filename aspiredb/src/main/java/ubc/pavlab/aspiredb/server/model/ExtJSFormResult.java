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
package ubc.pavlab.aspiredb.server.model;

/**
 * A simple return message for Ext JS
 * 
 * @author Loiane Groner http://loiane.com http://loianegroner.com
 */
public class ExtJSFormResult {

    private boolean success;
    private String message;
    private Object data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess( boolean success ) {
        this.success = success;
    }

    public void setMessage( String message ) {
        this.message = message;
    }

    public void setData( Object data ) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "{\"success\":" + this.success + ", \"message\" : \"" + this.message + "\", \"data\" : "
                + this.data.toString() + " }";
    }
}