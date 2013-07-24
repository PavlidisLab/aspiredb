package ubc.pavlab.aspiredb.client.view.variant.ideogram;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Window;
import ubc.pavlab.aspiredb.client.events.GenomeRegionSelectionEvent;
import ubc.pavlab.aspiredb.client.handlers.GenomeRegionSelectionHandler;
import ubc.pavlab.aspiredb.client.handlers.HasGenomeRegionSelectionHandler;
import ubc.pavlab.aspiredb.client.service.ChromosomeService;
import ubc.pavlab.aspiredb.client.service.ChromosomeServiceAsync;
import ubc.pavlab.aspiredb.shared.ChromosomeBand;
import ubc.pavlab.aspiredb.shared.ChromosomeValueObject;
import ubc.pavlab.aspiredb.shared.GenomicRange;
import ubc.pavlab.aspiredb.shared.VariantValueObject;
import ubc.pavlab.aspiredb.shared.query.CNVTypeProperty;
import ubc.pavlab.aspiredb.shared.query.CharacteristicProperty;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.query.VariantTypeProperty;

import java.util.*;

public class IdeogramPanel extends Composite implements HasGenomeRegionSelectionHandler {

    private Property displayedProperty;
    private List<VariantValueObject> variants;
    private ColourLegend colourLegend;


    public void setVariants(List<VariantValueObject> variants) {
        this.variants = variants;
    }

    public List<VariantValueObject> getVariants() {
        return variants;
    }

    public void showColourLegend() {
        colourLegend.show();
    }

    public void hideColourLegend() {
        colourLegend.hide();
    }

    interface MyUIBinder extends UiBinder<Widget, IdeogramPanel> {}
    private static MyUIBinder uiBinder = GWT.create( MyUIBinder.class );

    @UiField(provided=true)
    Canvas canvasBox;

    @UiField(provided=true)
    Canvas overlayCanvasBox;

    Context2d ctx;
    Context2d ctxOverlay;

    private double zoom = 1;

    public void zoom( double newZoom ) {
        this.zoom = newZoom;
        this.width = (int) (850 * zoom);
        this.height = (int) (boxHeight * zoom);
        redraw();
    }

	int width;
	int height;

    int boxHeight = 700;

    int displayScaleFactor;// = (int) (500000 / zoom); // bases per pixel
    boolean doneDrawing = false;

    private Map<String, ChromosomeValueObject> chromosomeValueObjects = new HashMap<String, ChromosomeValueObject>();
    private Map<String, ChromosomeIdeogram> chromosomes = new HashMap<String, ChromosomeIdeogram>();
    ChromosomeIdeogram previousChromosome = null;

    private final ChromosomeServiceAsync chromosomeService = GWT.create( ChromosomeService.class );

    public IdeogramPanel () {
        this.width = (int) (850 * zoom);
        this.height = (int) (boxHeight * zoom);

		canvasBox = Canvas.createIfSupported();
        overlayCanvasBox = Canvas.createIfSupported();

		initWidget( uiBinder.createAndBindUi( this ) );

        colourLegend = new ColourLegend();

        fetchChromosomeInfo();

        setDisplayedProperty(new VariantTypeProperty());
    }

    public void fetchChromosomeInfo() {
        chromosomeService.getChromosomes( new AsyncCallback<Map<String, ChromosomeValueObject>>() {
            @Override
            public void onFailure(Throwable caught) {
                // throw new ChromosomeServiceException;
            }

            @Override
            public void onSuccess(Map<String, ChromosomeValueObject> result) {
                chromosomeValueObjects.putAll(result);
            }
        });
    }

    private void initChromosomeIdeograms() {
        double longestChromosome = 250000000; // longest chromosome (# bases)
        displayScaleFactor = (int) ( longestChromosome  / ( height - 30 ) );

        final String[] chromosomeOrder = {"1","2","3","4","5","6","7","8","9","10","11","12",
                "13","14","15","16","17","18","19","20","21","22","X","Y"};

        int topY = 15;
        for (int index = 0; index < chromosomeOrder.length; index++) {
            String name = chromosomeOrder[index];
            ChromosomeValueObject chromosomeInfo = chromosomeValueObjects.get(name);
            Map<String, ChromosomeBand> bands = chromosomeInfo.getBands();
            int size = chromosomeInfo.getSize();
            int centromereLocation = chromosomeInfo.getCentromereLocation();
            int leftX = (int) (5 + index * 35 * zoom);
            ChromosomeIdeogram chromosomeIdeogram =
                    new ChromosomeIdeogram( name, size, centromereLocation, topY, leftX, displayScaleFactor, ctx, ctxOverlay, chromosomeInfo, zoom );
//                    chromosomeIdeogram.addGenomeRegionSelectionHandler(regionSelectionHandler);
            chromosomes.put(name, chromosomeIdeogram);
        }
    }

    private void initCanvasSize() {
        ctx = canvasBox.getContext2d();
        ctxOverlay = overlayCanvasBox.getContext2d();

        canvasBox.setHeight( height + "px" );
        canvasBox.setWidth( width + "px" );
        canvasBox.setCoordinateSpaceHeight( height );
        canvasBox.setCoordinateSpaceWidth( width );

        overlayCanvasBox.setHeight( height + "px" );
        overlayCanvasBox.setWidth( width + "px" );
        overlayCanvasBox.setCoordinateSpaceHeight( height );
        overlayCanvasBox.setCoordinateSpaceWidth( width );

        this.ctx.clearRect(0, 0, width, height);
        this.ctxOverlay.clearRect(0, 0, width, height);
    }


    @UiHandler("overlayCanvasBox")
    public void onMouseOut(MouseOutEvent event) {
        if (previousChromosome != null) {
            previousChromosome.clearCursor();
        }
        previousChromosome = null;
    }

    @UiHandler("overlayCanvasBox")
    public void onMouseMove(MouseMoveEvent event) {
        if (!doneDrawing) return;

        // Determine chromosome we are in.
        int x = event.getX();
        int y = event.getY();
        ChromosomeIdeogram chromosomeIdeogram = findChromosomeIdeogram( x, y );

        // If we moved from one chromosome to another, clear cursor from the previous one.
        if ( chromosomeIdeogram != null ) {
            if ( previousChromosome != null ) {
                if ( !previousChromosome.equals( chromosomeIdeogram ) ) {
                    previousChromosome.clearCursor();
                }
            }
            chromosomeIdeogram.drawCursor(y);
        } else {
            if (previousChromosome != null) {
                previousChromosome.clearCursor();
            }
        }
        previousChromosome = chromosomeIdeogram;
    }

    @UiHandler("overlayCanvasBox")
    public void onMouseDown(MouseDownEvent event) {
        if (!doneDrawing) return;

        // Determine chromosome
        int x = event.getX();
        int y = event.getY();
        ChromosomeIdeogram chromosomeIdeogram = findChromosomeIdeogram( x, y );
        if (chromosomeIdeogram != null) {
            for (ChromosomeIdeogram chromosome : chromosomes.values()) {
                chromosome.clearCursor();
            }

            // clear previous selection
            for (ChromosomeIdeogram chromosome : chromosomes.values()) {
                chromosome.clearSelection();
            }
            chromosomeIdeogram.startSelection(y);
        }
    }

    @UiHandler("overlayCanvasBox")
    public void onMouseUp(MouseUpEvent event) {
        if (!doneDrawing) return;

        // Determine chromosome
        int x = event.getX();
        int y = event.getY();
        ChromosomeIdeogram chromosomeIdeogram = findChromosomeIdeogram( x, y );
        if (chromosomeIdeogram != null) {
            chromosomeIdeogram.finishSelection(y);
            this.fireEvent( new GenomeRegionSelectionEvent(chromosomeIdeogram.getSelection()));
        }
    }

    private ChromosomeIdeogram findChromosomeIdeogram(int x, int y) {
        if (x < 5) return null;
        String name;
        int index = (int) ((x - 5) / (35 * zoom) + 1);
        if (index > 24) return null;

        if (index == 23 ) {
            name="X";
        } else if (index == 24) {
            name="Y";
        } else {
            name = String.valueOf(index);
        }

        ChromosomeIdeogram chromosomeIdeogram = chromosomes.get(name);
        int padding = 4;
        if (chromosomeIdeogram.getTopY() - padding > y) return null;
        if (chromosomeIdeogram.getTopY() + chromosomeIdeogram.getDisplaySize() + padding < y) return null;

        return chromosomeIdeogram;
    }

	private void drawVariants (List<VariantValueObject> variantValueObjects) {
        List<VariantValueObject> variants = new ArrayList<VariantValueObject>(variantValueObjects);
        sortVariantsBySize(variants);
		for (VariantValueObject variant : variants) {
			String chrName = variant.getGenomicRange().getChromosome();

            ChromosomeIdeogram chrIdeogram = this.chromosomes.get( chrName );
			chrIdeogram.drawVariant(variant, displayedProperty);
		}
	}

    private void sortVariantsBySize(List<VariantValueObject> variants) {
        Collections.sort(variants, new Comparator<VariantValueObject>() {
            @Override
            public int compare(VariantValueObject variant1, VariantValueObject variant2) {
                int size1 = variant1.getGenomicRange().getBaseEnd() - variant1.getGenomicRange().getBaseStart();
                int size2 = variant2.getGenomicRange().getBaseEnd() - variant2.getGenomicRange().getBaseStart();
                return size2 - size1;
            }
        });
    }

	private void drawVariants (Long subjectId, List<VariantValueObject> variantValueObjects) {
        List<VariantValueObject> variants = new ArrayList<VariantValueObject>(variantValueObjects);
        sortVariantsBySize(variants);

		for (VariantValueObject variant : variants) {
			String chrName = variant.getGenomicRange().getChromosome();

            ChromosomeIdeogram chrIdeogram = this.chromosomes.get( chrName );
            if (variant.getSubjectId().equals( subjectId )) {
                chrIdeogram.drawHighlightedVariant(variant, displayedProperty);
            } else {
                chrIdeogram.drawDimmedVariant(variant);
            }
		}
    }

    public void setDisplayedProperty(Property displayedProperty) {
        this.displayedProperty = displayedProperty;
        VariantLayer.resetDisplayProperty();
    }

    public Map<String,String> getColourLegend() {
        return VariantLayer.valueToColourMap;
    }

	public void redraw () {
        this.drawChromosomes();
        this.drawVariants( variants );
        colourLegend.update(VariantLayer.valueToColourMap, displayedProperty);
    }

    private void drawChromosomes() {
        doneDrawing = false;

        initCanvasSize();
        initChromosomeIdeograms();

        for (ChromosomeIdeogram chromosomeIdeogram : chromosomes.values()) {
            chromosomeIdeogram.drawChromosome();
        }
        doneDrawing = true;
    }

    public void highlightSubject(Long subjectId, List<VariantValueObject> variants) {
        doneDrawing = false;
        initCanvasSize();

        this.drawChromosomes();
		this.drawVariants( subjectId, variants );

        doneDrawing = true;
    }

	public void removeHighlight() {
		redraw ();
	}

    public GenomicRange getSelection() {
        for (ChromosomeIdeogram chromosomeIdeogram : chromosomes.values()) {
            if (chromosomeIdeogram.getSelection() != null) {
                return chromosomeIdeogram.getSelection();
            }
        }
        return null;
    }

    public void deselectAll() {
        for (ChromosomeIdeogram chromosomeIdeogram : chromosomes.values()) {
            chromosomeIdeogram.clearSelection();
        }
    }

    @Override
    public HandlerRegistration addGenomeRegionSelectionHandler(GenomeRegionSelectionHandler handler) {
        return this.addHandler ( handler, GenomeRegionSelectionEvent.TYPE );
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.boxHeight = height;
    }
}

