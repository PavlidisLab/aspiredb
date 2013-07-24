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

public abstract class PageableState extends AbstractState {

  private static final int DEFAULT_PAGE = 1;
  private static final int DEFAULT_PAGES = 1;
  public static final int DEFAULT_PAGE_SIZE = 5;
  private int page = DEFAULT_PAGE;
  private int pages = DEFAULT_PAGES;
  private int pageSize = DEFAULT_PAGE_SIZE;

  public PageableState() {
  }

  public final int getPage() {
    return page;
  }

  public final int getPages() {
    return pages;
  }

  public final void setPage(final int page) {

    if (page < 1) {
      return;
    }

    int old = this.page;
    this.page = page;
    firePropertyChange("page", old, page);
  }

  public final void setPages(final int pages) {

    if (pages < 1) {
      return;
    }

    int old = this.pages;
    this.pages = pages;
    firePropertyChange("pages", old, pages);
  }

  public final int getPageSize() {
    return pageSize;
  }

  public final void setPageSize(final int pageSize) {

    if (pageSize < 1) {
      return;
    }

    this.pageSize = pageSize;
  }
}