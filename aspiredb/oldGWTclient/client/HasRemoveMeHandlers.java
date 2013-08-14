package ubc.pavlab.aspiredb.client;

import com.google.gwt.event.shared.HandlerRegistration;
import ubc.pavlab.aspiredb.client.handlers.RemoveMeHandler;

public interface HasRemoveMeHandlers {
	
    public HandlerRegistration addRemoveMeHandler( RemoveMeHandler handler );

}
