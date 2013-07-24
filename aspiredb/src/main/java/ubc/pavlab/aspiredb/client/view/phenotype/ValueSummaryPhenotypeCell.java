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

import java.util.Set;

/**
 * author: anton
 * date: 02/05/13
 */
public class ValueSummaryPhenotypeCell extends AbstractCell<PhenotypeSummaryValueObject> {
    @Override
    public void render(Context context, PhenotypeSummaryValueObject summary, SafeHtmlBuilder sb) {
        Set<String> values = summary.getInferredValueToSubjectSet().keySet();

        for (String value : values) {
            String color = "black";
            String shownValue = value;
            Integer count = summary.getInferredValueToSubjectSet().get(value).size();
            if (count == null || count == 0) continue;
            if (value.equals("0")) {
                if (summary.isInferredBinaryType()) {
                    color = "green";
                    shownValue = "Absent";
                }
            } else if (value.equals("1")) {
                if (summary.isInferredBinaryType()) {
                    color = "red";                    
                    shownValue = "Present";
                }
            } else if (value.equals("Unknown")) {
                color = "grey";
            }

            String style = "style = 'width: 80px; display: inline-block; color: " + color + ";'";
            sb.appendHtmlConstant("<span " + style + ">");
            sb.appendHtmlConstant(shownValue);
            sb.appendHtmlConstant("(" + count + ") </span>");
        }
    }
}
