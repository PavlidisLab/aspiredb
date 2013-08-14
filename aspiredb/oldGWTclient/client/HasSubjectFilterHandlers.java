package ubc.pavlab.aspiredb.client;

import com.google.gwt.event.shared.HandlerRegistration;
import ubc.pavlab.aspiredb.client.handlers.SubjectFilterHandler;

public interface HasSubjectFilterHandlers {

    public HandlerRegistration addSubjectFilterHandler( SubjectFilterHandler handler );
    
}
