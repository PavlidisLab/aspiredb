package ubc.pavlab.aspiredb.client.view.fileuploader;

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;

public final class ProgressBar extends SimplePanel {

  private static final double COMPLETE_PERECENTAGE = 100d;
  private static final double START_PERECENTAGE = 0d;
  private Panel progress;

  public ProgressBar() {

    setStyleName("ProgressBar");

    progress = new SimplePanel();
    progress.setStyleName("progress");
    progress.setWidth("0px");

    add(progress);
  }

  public void update(final int percentage) {
    if (percentage < START_PERECENTAGE || percentage > COMPLETE_PERECENTAGE) {
      throw new IllegalArgumentException("invalid value for percentage");
    }

    int decorationWidth = progress.getAbsoluteLeft() - getAbsoluteLeft();

    int barWidth = this.getOffsetWidth();
    int progressWidth = (int) (((barWidth - (decorationWidth * 2)) / COMPLETE_PERECENTAGE) * percentage);

    progress.setWidth(progressWidth + "px");
  }
}