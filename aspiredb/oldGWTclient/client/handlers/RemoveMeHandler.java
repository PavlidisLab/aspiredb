package ubc.pavlab.aspiredb.client.handlers;

import com.google.gwt.event.shared.EventHandler;
import ubc.pavlab.aspiredb.client.events.RemoveMeEvent;

public interface RemoveMeHandler extends EventHandler {
	
	void onRemoveMe (RemoveMeEvent event);
	
}