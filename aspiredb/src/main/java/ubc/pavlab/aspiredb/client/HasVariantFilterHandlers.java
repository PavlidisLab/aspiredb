package ubc.pavlab.aspiredb.client;

import com.google.gwt.event.shared.HandlerRegistration;
import ubc.pavlab.aspiredb.client.handlers.VariantFilterHandler;

public interface HasVariantFilterHandlers {
    public HandlerRegistration addVariantFilterHandler( VariantFilterHandler event );
}
