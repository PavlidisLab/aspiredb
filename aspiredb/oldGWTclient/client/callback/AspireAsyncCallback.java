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

package ubc.pavlab.aspiredb.client.callback;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import ubc.pavlab.aspiredb.client.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.client.exceptions.NeurocartaServiceException;
import ubc.pavlab.aspiredb.client.exceptions.NotLoggedInException;
import ubc.pavlab.aspiredb.client.util.AuthenticationUtils;

/**
 *  custom callback to handle events such as user timeouts etc.
 * 
 * @author cmcdonald
 * @version $Id: AspireAsyncCallback.java,v 1.10 2013/06/11 22:30:40 anton Exp $
 * @param <T>
 */
public class AspireAsyncCallback<T> implements AsyncCallback<T> {

    private AsyncCallback<T> asyncCallback;

    public AspireAsyncCallback() {
    }

    public AspireAsyncCallback(AsyncCallback<T> asyncCallback) {
        this.asyncCallback = asyncCallback;
    }

	@Override
    public void onFailure( Throwable caught ) {
        MessageBox alertBox;
        
        if (caught instanceof NotLoggedInException) {
            AuthenticationUtils.logout();
            alertBox = new MessageBox("Session Timeout", "You have been logged out due to inactivity, Please Log in again" );
            alertBox.show();
            return;
        } else if ( caught instanceof StatusCodeException ) {
            alertBox = new MessageBox("Error","There has been a error: "+caught.toString());
            alertBox.show();
        } else if ( caught instanceof BioMartServiceException ) {
            alertBox = new MessageBox("Error","Error occurred when using BioMart service.");
            alertBox.show();
        } else if ( caught instanceof NeurocartaServiceException ) {
            alertBox = new MessageBox("Error","Error occurred when using Neurocarta service.");
            alertBox.show();
        }

        if (asyncCallback != null) {
            asyncCallback.onFailure(caught);
        }
    }

    @Override
    public void onSuccess(T result) {
        if (asyncCallback != null) {
            asyncCallback.onSuccess(result);
        }
    }
}