package ubc.pavlab.aspiredb.client.handlers;

import com.google.gwt.event.shared.EventHandler;
import ubc.pavlab.aspiredb.client.events.VariantSelectionEvent;

public interface VariantSelectionHandler extends EventHandler {
    public void onVariantSelection( VariantSelectionEvent event );
}
