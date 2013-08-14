package ubc.pavlab.aspiredb.client.view.fileuploader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import ubc.pavlab.aspiredb.client.fileuploader.state.UploadProgressState;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public final class Paging extends Composite {

  public interface Images extends ClientBundle {

    @Source("first.png")
    ImageResource first();

    @Source("last.png")
    ImageResource last();

    @Source("previous.png")
    ImageResource previous();

    @Source("next.png")
    ImageResource next();

    @Source("firstDisabled.png")
    ImageResource firstDisabled();

    @Source("lastDisabled.png")
    ImageResource lastDisabled();

    @Source("previousDisabled.png")
    ImageResource previousDisabled();

    @Source("nextDisabled.png")
    ImageResource nextDisabled();
  }
  private static final Images IMAGES = (Images) GWT.create(Images.class);
  private static final Image FIRST_DISABLED = new Image(IMAGES.firstDisabled());
  private static final Image PREVIOUS_DISABLED = new Image(IMAGES.previousDisabled());
  private static final Image NEXT_DISABLED = new Image(IMAGES.nextDisabled());
  private static final Image LAST_DISABLED = new Image(IMAGES.lastDisabled());
  private HorizontalPanel panel;

  public Paging() {

    panel = new HorizontalPanel();
    //panel.setStyleName("Paging");
    panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

    this.initWidget(panel);

    UploadProgressState.instance.addPropertyChangeListener("page", new PageListener());
    UploadProgressState.instance.addPropertyChangeListener("pages", new PagesListener());
  }

  @Override
  protected void onLoad() {
    redraw();
  }

  private void redraw() {
    int currentPage = UploadProgressState.instance.getPage();
    int pages = UploadProgressState.instance.getPages();
    int page = UploadProgressState.instance.getPage();

    panel.clear();

    if (currentPage > 1) {
      Image first = new Image(IMAGES.first());
      first.addClickHandler(new PageClickHandler(1));
      panel.add(first);
      Image previous = new Image(IMAGES.previous());
      previous.addClickHandler(new PageClickHandler(currentPage - 1));
      panel.add(previous);
    } else {
      panel.add(FIRST_DISABLED);
      panel.add(PREVIOUS_DISABLED);
    }

    TextBox pageBox = new TextBox();
    pageBox.setText(String.valueOf(page));
    pageBox.addChangeHandler(new PageChangeHandler());

    panel.add(pageBox);

    HTML ofPages = new HTML("&nbsp;of&nbsp;" + String.valueOf(pages));
    panel.add(ofPages);

    if (currentPage < pages) {
      Image next = new Image(IMAGES.next());
      next.addClickHandler(new PageClickHandler(currentPage + 1));
      panel.add(next);
      Image last = new Image(IMAGES.last());
      last.addClickHandler(new PageClickHandler(pages));
      panel.add(last);
    } else {
      panel.add(NEXT_DISABLED);
      panel.add(LAST_DISABLED);
    }
  }

  private final class PageListener implements PropertyChangeListener {

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
      redraw();
    }
  }

  private final class PagesListener implements PropertyChangeListener {

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
      redraw();
    }
  }

  private static final class PageClickHandler implements ClickHandler {

    private int page;

    public PageClickHandler(final int page) {
      this.page = page;
    }

    @Override
    public void onClick(final ClickEvent event) {
      UploadProgressState.instance.setPage(page);
    }
  }

  private static final class PageChangeHandler implements ChangeHandler {

    @Override
    public void onChange(final ChangeEvent event) {

      TextBox pageBox = (TextBox) event.getSource();
      Integer page = Integer.valueOf(pageBox.getText());
      int pages = UploadProgressState.instance.getPages();

      if (page >= 1 && page <= pages) {
        UploadProgressState.instance.setPage(page);
      }
    }
  }
}