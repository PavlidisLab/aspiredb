package ubc.pavlab.aspiredb.client.handlers;

import com.google.gwt.event.shared.EventHandler;
import ubc.pavlab.aspiredb.client.events.ProjectSelectionEvent;

public interface ProjectSelectionHandler extends EventHandler {
    public void onProjectSelection( ProjectSelectionEvent event );
}
