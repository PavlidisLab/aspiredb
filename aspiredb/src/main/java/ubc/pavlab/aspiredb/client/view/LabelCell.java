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
package ubc.pavlab.aspiredb.client.view;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import ubc.pavlab.aspiredb.shared.LabelValueObject;

import java.util.Collection;
import java.util.Set;

/**
 * @author anton
 */
public class LabelCell extends AbstractCell<Collection<LabelValueObject>> {
    private final Set<Long> visibleLabels;

    public LabelCell (Set<Long> visibleLabels) {
        this.visibleLabels = visibleLabels;
    }

    @Override
    public void render(Context context, Collection<LabelValueObject> labels, SafeHtmlBuilder sb) {
        for (LabelValueObject label : labels) {
            if (this.visibleLabels.contains(label.getId())) {
                String color = label.getColour() == null ? "E6E6FA" : label.getColour();
                sb.appendHtmlConstant( "<span style='background-color: #"+color+"'>" );
                sb.appendHtmlConstant( "&nbsp" );
                sb.appendEscaped( label.getName() );
                sb.appendHtmlConstant( "&nbsp" );
                sb.appendHtmlConstant( "</span> &nbsp" );
            }
        }
    }
}

