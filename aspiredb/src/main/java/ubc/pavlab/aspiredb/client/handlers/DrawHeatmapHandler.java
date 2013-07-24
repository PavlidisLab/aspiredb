package ubc.pavlab.aspiredb.client.handlers;

import com.google.gwt.event.shared.EventHandler;
import ubc.pavlab.aspiredb.client.events.DrawHeatmapEvent;

public interface DrawHeatmapHandler extends EventHandler {

	public void onDrawHeatmap( DrawHeatmapEvent event );
}
