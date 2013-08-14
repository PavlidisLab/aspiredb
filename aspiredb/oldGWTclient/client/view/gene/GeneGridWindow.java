package ubc.pavlab.aspiredb.client.view.gene;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.toolbar.SeparatorToolItem;

import ubc.pavlab.aspiredb.shared.VariantValueObject;

import java.util.ArrayList;
import java.util.Collection;

public class GeneGridWindow extends Window {
	
    interface MyUIBinder extends UiBinder<Widget, GeneGridWindow> {}
    private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );

    @UiField
    GeneGrid geneGrid;
    
    public GeneGridWindow() {    
    	setWidget( uiBinder.createAndBindUi( this ) );
    	
    	this.setHeadingText("Genes hit by variant(s)");
        this.setPixelSize(800, 615);
    	this.geneGrid.setHeight(580);
    	this.geneGrid.setWidth(800);    	
    }
	
	public void showGenes(final Collection<VariantValueObject> variantCollection) {
        this.show();

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                Collection<Long> variantIds = new ArrayList<Long>();

                for (VariantValueObject vvo: variantCollection){
                    variantIds.add( vvo.getId() );
                }

                geneGrid.loadGenesForVariants( variantIds );
            }
        });
    }
    
	public void showGenesWithNeurocartaPhenotype(String phenotypeUri) {
		this.show();
		this.geneGrid.loadGenesWithNeurocartaPhenotype(phenotypeUri);
	}
	
	public void addToToolbar(Widget toolbarWidget) {
		this.geneGrid.addWidgetToToolbar(toolbarWidget);
	}
	
	public TextButton getViewEvidenceButton(){
	    return geneGrid.viewEvidenceButton;	    
	}
	
	public SeparatorToolItem getViewEvidenceSeparator(){
        return geneGrid.viewEvidenceSeparator;     
    }
}
