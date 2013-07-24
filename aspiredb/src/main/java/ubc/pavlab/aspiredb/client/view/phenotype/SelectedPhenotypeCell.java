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
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import ubc.pavlab.aspiredb.shared.PhenotypeSummaryValueObject;
import ubc.pavlab.aspiredb.shared.PhenotypeValueObject;

/**
 * author: anton
 * date: 02/05/13
 */
public class SelectedPhenotypeCell extends AbstractCell<PhenotypeSummaryValueObject> {

    @Override
    public void render( Context context,
                        PhenotypeSummaryValueObject phenotype,
                        SafeHtmlBuilder sb )
    {
        if (phenotype == null) return;

        String style = "style='color: black'";

        PhenotypeValueObject phenotypeValueObject = phenotype.getSelectedPhenotype();
        if (phenotypeValueObject == null) return;
        String dbValue = phenotypeValueObject.getDbValue();
        String inferredValue = "";
        for (String value : phenotype.getInferredValueToSubjectSet().keySet()) {
            if ( phenotype
                    .getInferredValueToSubjectSet()
                    .get(value)
                    .contains(phenotypeValueObject.getSubjectId())) {
                inferredValue = value;
                break;
            }
        }

        String shownValue = inferredValue;
        String inferenceArrow = "";
        if (inferredValue.equals("0")) {
            style = "style='color: green'";
            shownValue = "Absent";
            inferenceArrow = " &uarr;";
        } else if (inferredValue.equals("1")) {
            style = "style='color: red'";
            shownValue = "Present";
            inferenceArrow = " &darr;";
        }
        sb.appendHtmlConstant("<span " + style + ">");
        sb.appendHtmlConstant(shownValue);
        if (!inferredValue.equals(dbValue)) {
            sb.appendHtmlConstant( inferenceArrow );
        }
        sb.appendHtmlConstant("</span>");
    }
}
