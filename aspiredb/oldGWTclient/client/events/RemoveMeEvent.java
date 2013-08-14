package ubc.pavlab.aspiredb.client.events;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;
import ubc.pavlab.aspiredb.client.handlers.RemoveMeHandler;

public class RemoveMeEvent extends GwtEvent<RemoveMeHandler> {

	public static Type<RemoveMeHandler> TYPE = new Type<RemoveMeHandler>();

	private Widget me;
	    
	public RemoveMeEvent( Widget me ) {
	  	this.me = me;
	}

    public Widget getWidget() {
	    	return this.me;
	    }
	    
	    @Override
	    public Type<RemoveMeHandler> getAssociatedType() {
	        return TYPE;
	    }

	    @Override
	    protected void dispatch( RemoveMeHandler handler ) {
	        handler.onRemoveMe( this );
	    }

}
