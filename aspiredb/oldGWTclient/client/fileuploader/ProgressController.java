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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ubc.pavlab.aspiredb.client.fileuploader.state.UploadProgressState;
import ubc.pavlab.aspiredb.shared.fileuploader.FileDto;
import ubc.pavlab.aspiredb.shared.fileuploader.FileEvent;
import ubc.pavlab.aspiredb.shared.fileuploader.UploadProgressChangeEvent;

import java.util.List;


public final class ProgressController {
    
    protected static final UploadProgressServiceAsync service = GWT.create(UploadProgressService.class);

  public static final ProgressController instance = new ProgressController();

  private ProgressController() {
  }

  public void findFiles(final int page, final int pageSize) {
    service.readFiles(page, pageSize, new AsyncCallback<List<FileDto>>() {

      @Override
      public void onFailure(final Throwable t) {
        GWT.log("error find files", t);
      }

      @Override
      public void onSuccess(final List<FileDto> files) {
        UploadProgressState.instance.setFiles(files);
      }
    });
  }

  private void getEvents() {

    service.getEvents(new AsyncCallback<List<FileEvent>>() {

      @Override
      public void onFailure(final Throwable t) {
        GWT.log("error get events", t);
      }

      @Override
      public void onSuccess(final List<FileEvent> events) {

        for (FileEvent event : events) {
          handleEvent(event);
        }
        service.getEvents(this);
      }

      private void handleEvent(final FileEvent event) {

        if (event instanceof UploadProgressChangeEvent) {
          UploadProgressChangeEvent uploadPercentChangeEvent = (UploadProgressChangeEvent) event;
          String filename = uploadPercentChangeEvent.getFilename();
          Integer percentage = uploadPercentChangeEvent.getPercentage();

          UploadProgressState.instance.setUploadProgress(filename, percentage);
        }
      }
    });
  }

  public void initialise() {
    service.initialise(new AsyncCallback<Void>() {

      @Override
      public void onFailure(final Throwable t) {
        GWT.log("error initialise", t);
      }

      @Override
      public void onSuccess(final Void result) {
        getEvents();
      }
    });
  }

  public void countFiles() {
    service.countFiles(new AsyncCallback<Integer>() {

      @Override
      public void onFailure(final Throwable t) {
        GWT.log("error count files", t);
      }

      @Override
      public void onSuccess(final Integer result) {
        int pageSize = UploadProgressState.instance.getPageSize();
        int pages = (int) Math.ceil((double) result / (double) pageSize);
        UploadProgressState.instance.setPages(pages);
      }
    });
  }
}