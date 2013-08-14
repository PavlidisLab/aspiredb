/*
 * The aspiredb project
 * 
 * Copyright (c) 2013 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubc.pavlab.aspiredb.client.view.gene;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Image;
import ubc.pavlab.aspiredb.client.resources.AspireImageResources;
import ubc.pavlab.aspiredb.client.util.GemmaURLUtils;
import ubc.pavlab.aspiredb.shared.GeneValueObject;

/**
 * @author anton
 */
public class GemmaButtonCell extends AbstractCell<GeneValueObject> {
//    public GemmaButtonCell() {
//        super("click");
//    }
//
//    @Override
//    public void onBrowserEvent(Context context, Element parent, GeneValueObject value, NativeEvent event, ValueUpdater<GeneValueObject> valueUpdater) {
//        super.onBrowserEvent(context, parent, value, event, valueUpdater);
//    }

    @Override
    public void render(Context context, final GeneValueObject value, SafeHtmlBuilder sb) {
        if (value.getGeneBioType().equals("protein_coding")) {
            String tooltip = "View gene in Gemma";
            String gemmaURL = GemmaURLUtils.makeGeneUrl(value.getSymbol());

            Image image = new Image(AspireImageResources.INSTANCE.gemmaLogo());
            image.setAltText(tooltip);
            image.setTitle(tooltip);

            //image.getElement().addClassName("gwt-Hyperlink");

            sb.appendHtmlConstant("<a href='" + gemmaURL + "' target='_blank' >"+ image +"</a>");
        }
    }
}
