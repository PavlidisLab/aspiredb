package ubc.pavlab.aspiredb.client.events;

import com.google.gwt.event.shared.GwtEvent;
import ubc.pavlab.aspiredb.client.handlers.ProjectSelectionHandler;

public class ProjectSelectionEvent extends GwtEvent<ProjectSelectionHandler> {

    public static Type<ProjectSelectionHandler> TYPE = new Type<ProjectSelectionHandler>();

    private Long projectId;
    
    public ProjectSelectionEvent ( Long projectId ) {
        this.projectId = projectId;
    }
        
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<ProjectSelectionHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch( ProjectSelectionHandler handler ) {
        handler.onProjectSelection( this );
    }


    public Long getProjectId() {
        return projectId;
    }


}