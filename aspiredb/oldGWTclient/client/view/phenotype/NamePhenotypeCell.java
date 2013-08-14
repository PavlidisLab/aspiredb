package ubc.pavlab.aspiredb.client.view.phenotype;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import ubc.pavlab.aspiredb.client.util.GemmaURLUtils;
import ubc.pavlab.aspiredb.client.view.gene.GeneGridWindow;
import ubc.pavlab.aspiredb.server.util.PhenotypeUtil;
import ubc.pavlab.aspiredb.shared.PhenotypeSummaryValueObject;

public class NamePhenotypeCell extends AbstractCell<PhenotypeSummaryValueObject> {
    public NamePhenotypeCell() {
        super("click");
    }

    @Override
    public void onBrowserEvent(Cell.Context context,
                               Element parent,
                               final PhenotypeSummaryValueObject value,
                               NativeEvent event,
                               ValueUpdater<PhenotypeSummaryValueObject> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);

        // Handle the click event.
        if ("click".equals(event.getType())) {
            Element firstChildElement = parent.getFirstChildElement();

            // If the user clicks on the Neurocarta logo, open gene grid window to show genes.
            // Assume that Image for Neurocarta logo is the first HTML tag in the cell.
            if (value.isNeurocartaPhenotype() && firstChildElement != null && firstChildElement.isOrHasChild(Element.as(event.getEventTarget()))) {
                
                GeneGridWindow geneGridWindow = new GeneGridWindow();
                
                TextButton viewEvidenceButton = geneGridWindow.getViewEvidenceButton();
                viewEvidenceButton.addSelectHandler(new SelectEvent.SelectHandler() {
                    public void onSelect(SelectEvent selectEvent) {
                        Window.open(GemmaURLUtils.makeNeurocartaPhenotypeUrl(value.getUri()), "_blank", "");
                    }
                });
                
                viewEvidenceButton.setVisible( true );
                geneGridWindow.getViewEvidenceSeparator().setVisible(true);                
                
                geneGridWindow.setHeadingText("Genes associated with " + value.getName() + " in Neurocarta");
                geneGridWindow.showGenesWithNeurocartaPhenotype(PhenotypeUtil.HUMAN_PHENOTYPE_URI_PREFIX + value.getUri());
            }
        }
    }

    @Override
    public void render(Cell.Context context, PhenotypeSummaryValueObject summary, SafeHtmlBuilder sb) {
        if (summary.isNeurocartaPhenotype()) {
            String tooltip = "View genes associated in Neurocarta";
            Image image = new Image("neurocarta.png");
            image.setAltText(tooltip);
            image.setTitle(tooltip);
            image.getElement().addClassName("gwt-Hyperlink");

            sb.appendHtmlConstant(summary.getName() + " " + image);
        } else {
            sb.appendHtmlConstant(summary.getName());
        }
    }
}