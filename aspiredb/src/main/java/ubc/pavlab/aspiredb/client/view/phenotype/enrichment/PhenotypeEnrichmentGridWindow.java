package ubc.pavlab.aspiredb.client.view.phenotype.enrichment;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Window;

public class PhenotypeEnrichmentGridWindow extends Window {

    interface MyUIBinder extends UiBinder<Widget, PhenotypeEnrichmentGridWindow> {
    }

    private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );

    @UiField
    PhenotypeEnrichmentGrid phenotypeEnrichmentGrid;

    public PhenotypeEnrichmentGridWindow() {
        setWidget( uiBinder.createAndBindUi( this ) );

        this.setPixelSize( 800, 580 );
        this.phenotypeEnrichmentGrid.setHeight( 580 );
        this.phenotypeEnrichmentGrid.setWidth( 800 );
    }

    public void showPhenotypeEnrichment( final Collection<Long> subjectIds ) {
        this.show();

        Scheduler.get().scheduleDeferred( new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {

                phenotypeEnrichmentGrid.loadPhenotypeEnrichment( subjectIds );
            }
        } );
    }

    public void setHeadingText( String title ) {
        this.phenotypeEnrichmentGrid.setHeadingText( title );
    }

    public void addToToolbar( Widget toolbarWidget ) {
        this.phenotypeEnrichmentGrid.addWidgetToToolbar( toolbarWidget );
    }
}
