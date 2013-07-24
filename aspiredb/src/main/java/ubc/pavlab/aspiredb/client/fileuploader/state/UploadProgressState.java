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
package ubc.pavlab.aspiredb.client.fileuploader.state;

import ubc.pavlab.aspiredb.shared.fileuploader.FileDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class UploadProgressState extends PageableState {

  public static final UploadProgressState instance = new UploadProgressState();
  private Map<String, Integer> uploadProgress;
  private List<FileDto> files;

  private UploadProgressState() {
    uploadProgress = new HashMap<String, Integer>();
  }

  public List<FileDto> getFiles() {
    return files;
  }

  public void setFiles(final List<FileDto> files) {
    List<FileDto> old = this.files;
    this.files = files;
    firePropertyChange("files", old, files);
  }

  public Integer getUploadProgress(final String filename) {
    return uploadProgress.get(filename);
  }

  public void setUploadProgress(final String filename, final Integer percentage) {
    Integer old = this.uploadProgress.get(filename);
    uploadProgress.put(filename, percentage);
    firePropertyChange("uploadProgress", old, uploadProgress);
  }
}