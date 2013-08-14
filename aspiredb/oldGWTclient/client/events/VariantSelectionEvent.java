package ubc.pavlab.aspiredb.client.events;

import com.google.gwt.event.shared.GwtEvent;
import ubc.pavlab.aspiredb.client.handlers.VariantSelectionHandler;
import ubc.pavlab.aspiredb.shared.VariantValueObject;

public class VariantSelectionEvent extends GwtEvent<VariantSelectionHandler> {

    public static Type<VariantSelectionHandler> TYPE = new Type<VariantSelectionHandler>();

    private VariantValueObject variant;
    
    public VariantSelectionEvent ( VariantValueObject variant ) {
        this.variant = variant;
    }

    public VariantSelectionEvent ( String variantId ) {
    	VariantValueObject vo = new VariantValueObject();
    	vo.setId( Long.parseLong( variantId ) );
    	this.variant = vo;
    }

    
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<VariantSelectionHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch( VariantSelectionHandler handler ) {
        handler.onVariantSelection( this );
    }


    public VariantValueObject getVariant() {
        return variant;
    }


}