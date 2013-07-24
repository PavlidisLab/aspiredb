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
package ubc.pavlab.aspiredb.client.view.phenotype;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.theme.base.client.grid.RowExpanderDefaultAppearance;
import com.sencha.gxt.widget.core.client.grid.RowExpander;
import ubc.pavlab.aspiredb.shared.PhenotypeSummaryValueObject;

/**
 * author: anton
 * date: 03/05/13
 */
public class PhenotypeGridRowExpander extends RowExpander<PhenotypeSummaryValueObject> {

    public PhenotypeGridRowExpander() {
        super( new IdentityValueProvider<PhenotypeSummaryValueObject>(),
               new AbstractCell<PhenotypeSummaryValueObject>() {
                    @Override
                    public void render(com.google.gwt.cell.client.Cell.Context context,
                                       PhenotypeSummaryValueObject value, SafeHtmlBuilder sb) {
                        PhenotypeGridRowRenderer.renderExpandedRow( value, sb );
                    }
               },
               new RowExpanderDefaultAppearance<PhenotypeSummaryValueObject>() {
               @Override
                    public void renderExpander(Cell.Context context, PhenotypeSummaryValueObject value, SafeHtmlBuilder sb) {
                        if (value.getDescendantOntologyTermSummaries().isEmpty()) {
                        } else {
                            super.renderExpander( context, value, sb );
                        }
                    }
               }
        );
    }
}
