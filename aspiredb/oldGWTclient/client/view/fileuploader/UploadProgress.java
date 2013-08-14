package ubc.pavlab.aspiredb.client.view.fileuploader;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import ubc.pavlab.aspiredb.client.fileuploader.state.UploadProgressState;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;


public final class UploadProgress extends Composite {

  private Panel panel;
  private Map<String, UploadPanel> uploads;

  public UploadProgress() {

    panel = new VerticalPanel();
    //panel.setStyleName("UploadProgress");
    uploads = new HashMap<String, UploadPanel>();

    this.initWidget(panel);

    UploadProgressState.instance.addPropertyChangeListener("uploadProgress", new UploadProgressListener());
  }

  private final class UploadProgressListener implements PropertyChangeListener {

    private static final int COMPLETE_PERECENTAGE = 100;
    private static final int REMOVE_DELAY = 3000;

    @Override
    public void propertyChange(final PropertyChangeEvent event) {

      Map<String, Integer> uploadPercentage = (Map<String, Integer>) event.getNewValue();

      for (Map.Entry<String, Integer> entry : uploadPercentage.entrySet()) {
        String file = entry.getKey();
        Integer percentage = entry.getValue();

        final UploadPanel uploadPanel;
        if (!uploads.containsKey(file)) {
          uploadPanel = new UploadPanel(file);
          uploads.put(file, uploadPanel);
          panel.add(uploadPanel);
        } else {
          uploadPanel = uploads.get(file);
        }

        uploadPanel.update(percentage);

        if (percentage == COMPLETE_PERECENTAGE) {
          Timer timer = new Timer() {

            @Override
            public void run() {
              panel.remove(uploadPanel);
            }
          };
          timer.schedule(REMOVE_DELAY);
        }
      }
    }
  }

  private static final class UploadPanel extends HorizontalPanel {

    private ProgressBar bar;
    private Label label;

    public UploadPanel(final String file) {

      //setStyleName("UploadPanel");

      bar = new ProgressBar();
      label = new Label(file);

      add(bar);
      add(label);
    }

    public void update(final int percentage) {
      bar.update(percentage);
    }
  }
}