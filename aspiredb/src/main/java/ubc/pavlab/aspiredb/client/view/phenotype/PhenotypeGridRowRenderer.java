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

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import ubc.pavlab.aspiredb.shared.PhenotypeSummaryValueObject;
import ubc.pavlab.aspiredb.shared.PhenotypeValueObject;

import java.util.Set;

/**
 * author: anton
 * date: 02/05/13
 */
public class PhenotypeGridRowRenderer {

    /**
     * Render HTML to show expanded phenotype summary row.
     *
     * @param phenotypeSummary
     * @param sb
     */
    public static void renderExpandedRow(PhenotypeSummaryValueObject phenotypeSummary, SafeHtmlBuilder sb) {
        sb.appendHtmlConstant("<p><b>Includes:</b></p>");
        sb.appendHtmlConstant("<table>");

        for (PhenotypeSummaryValueObject descendantPhenotype : phenotypeSummary.getDescendantOntologyTermSummaries()) {
            sb.appendHtmlConstant("<tr>");
            sb.appendHtmlConstant("<td style='padding-right:3px; padding-left:5px; font-size: 8pt;'>" +
                    descendantPhenotype.getName() + "</td>");

            renderPhenotypeOfSelectedSubject(sb, descendantPhenotype, phenotypeSummary.getSelectedPhenotype());

            renderValuesAndCounts(sb, descendantPhenotype);

            sb.appendHtmlConstant("</tr>");
        }
        sb.appendHtmlConstant("</table>");
    }

    private static void renderValuesAndCounts(SafeHtmlBuilder sb, PhenotypeSummaryValueObject descendantPhenotype) {
        sb.appendHtmlConstant("<td style='padding-right:5px; padding-left:5px; font-size: 8pt;'>");
        Set<String> values = descendantPhenotype.getInferredValueToSubjectSet().keySet();
        for ( String value : values ) {
            String style = "style='color: black'";
            String shownValue = value;
            Integer count = descendantPhenotype.getInferredValueToSubjectSet().get( value ).size();

            if ( value.equals("0") ) {
                style = "style='color: green'";
                shownValue = "Absent";
            } else if ( value.equals("1") ) {
                style = "style='color: red'";
                shownValue = "Present";
            }
            sb.appendHtmlConstant("<span " + style + ">");
            sb.appendHtmlConstant(shownValue);
            sb.appendHtmlConstant("("+count+") </span>");
        }
        sb.appendHtmlConstant("</td>");
    }

    private static void renderPhenotypeOfSelectedSubject(SafeHtmlBuilder sb, PhenotypeSummaryValueObject descendantPhenotype, PhenotypeValueObject selectedPhenotype) {
        sb.appendHtmlConstant("<td style='padding-right:4px; padding-left:4px; font-size: 8pt;'>");
        if ( selectedPhenotype != null ) {

            PhenotypeValueObject phenotypeValueObject = selectedPhenotype.getDescendantPhenotypes()
                    .get(descendantPhenotype.getName());

            String inferredValueValue = null;
            String dbValue = null;

            if (phenotypeValueObject == null) {
                inferredValueValue = "Uknown";
            } else {
                dbValue = phenotypeValueObject.getDbValue();
                inferredValueValue = phenotypeValueObject.getInferredValue();
            }

            if ( dbValue != null ) {
                makeHTMLPhenotypeLabel(sb, dbValue, true);
            } else if (inferredValueValue != null ) {
                makeHTMLPhenotypeLabel(sb, inferredValueValue, false);
            }
        }
        sb.appendHtmlConstant("</td>");
    }

    private static void makeHTMLPhenotypeLabel(SafeHtmlBuilder sb, String value, boolean isDbValue) {
        String shownValue = value;
        String style = "style='color: black'";
        String inferenceArrow = "";
        if (value.equals("0")) {
            style = "style='color: green'";
            shownValue = "Absent";
            if (!isDbValue) {
                inferenceArrow = " &uarr;";
            }
        } else if (value.equals("1")) {
            style = "style='color: red'";
            shownValue = "Present";
            if (!isDbValue) {
                inferenceArrow = " &darr;";
            }
        } else {
            shownValue = value;
        }

        sb.appendHtmlConstant("<span " + style + ">")
                .appendHtmlConstant(shownValue)
                .appendHtmlConstant(inferenceArrow)
                .appendHtmlConstant("</span>");
    }
}