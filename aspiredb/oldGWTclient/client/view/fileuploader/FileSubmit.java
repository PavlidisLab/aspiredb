package ubc.pavlab.aspiredb.client.view.fileuploader;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import ubc.pavlab.aspiredb.client.fileuploader.ProgressController;
import ubc.pavlab.aspiredb.client.fileuploader.state.UploadProgressState;


public final class FileSubmit extends Composite {

  private static final String URL = "upload";
  private Button submit;
  private FileUpload file;
  private FormPanel form;

  public FileSubmit() {

    file = new FileUpload();
    file.setName("file");
    file.setTitle("select a file");

    submit = new Button("Submit");
    submit.setTitle("upload file");

    Panel uploadPanel = new FlowPanel();
    //uploadPanel.setStyleName("FileSubmit");
    uploadPanel.add(file);
    uploadPanel.add(submit);

    form = new FormPanel();
    form.setEncoding(FormPanel.ENCODING_MULTIPART);
    form.setMethod(FormPanel.METHOD_POST);
    form.setAction(URL);
    form.setWidget(uploadPanel);

    this.initWidget(form);

    submit.addClickHandler(new SubmitClickHandler());
    form.addSubmitHandler(new FormSubmitHandler());
    form.addSubmitCompleteHandler(new FormSubmitCompleteHandler());
  }

  @Override
  protected void onLoad() {
    ProgressController.instance.initialise();
  }

  private class FormSubmitHandler implements SubmitHandler {

    @Override
    public void onSubmit(final SubmitEvent event) {
      submit.setEnabled(false);
    }
  }

  private class FormSubmitCompleteHandler implements SubmitCompleteHandler {

    @Override
    public void onSubmitComplete(final SubmitCompleteEvent event) {
      form.reset();
      submit.setEnabled(true);
      ProgressController.instance.countFiles();
      ProgressController.instance.findFiles(UploadProgressState.instance.getPage(), UploadProgressState.instance.getPageSize());
    }
  }

  private final class SubmitClickHandler implements ClickHandler {

    @Override
    public void onClick(final ClickEvent event) {
      String filename = file.getFilename();

      if (filename.isEmpty()) {
        Window.alert("Select a file");
        return;
      }

      form.submit();
    }
  }
}