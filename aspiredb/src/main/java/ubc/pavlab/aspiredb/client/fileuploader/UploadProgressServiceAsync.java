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
package ubc.pavlab.aspiredb.client.fileuploader;

import com.google.gwt.user.client.rpc.AsyncCallback;
import ubc.pavlab.aspiredb.shared.fileuploader.FileDto;
import ubc.pavlab.aspiredb.shared.fileuploader.FileEvent;

import java.util.List;


public interface UploadProgressServiceAsync {

  void initialise(AsyncCallback<Void> asyncCallback);

  void countFiles(AsyncCallback<Integer> asyncCallback);

  void readFiles(int page, int pageSize, AsyncCallback<List<FileDto>> asyncCallback);

  void getEvents(AsyncCallback<List<FileEvent>> asyncCallback);
}