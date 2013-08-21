/*
 * The aspiredb project
 * 
 * Copyright (c) 2012 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package ubc.pavlab.aspiredb.client.view.filter;

import com.google.gwt.user.client.ui.Widget;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.query.RestrictionFilterConfig;
import ubc.pavlab.aspiredb.shared.query.SubjectFilterConfig;
import ubc.pavlab.aspiredb.shared.query.restriction.Conjunction;
import ubc.pavlab.aspiredb.shared.query.restriction.RestrictionExpression;

import java.util.Collection;
import java.util.Iterator;

/**
 * Subject Filter Widget
 * 
 * @author frances
 * @version $Id: SubjectFilterWidget.java,v 1.16 2013/06/28 18:06:00 frances Exp $
 */
public class SubjectFilterWidget extends FilterWidget {
	private static final String WIDGET_TITLE = "Subject Filter";
	private static final String WIDGET_BACKGROUND_COLOR = "#FFE5B4";

    private Collection<Property> properties;

    public SubjectFilterWidget(Collection<Property> properties) {
    	this.properties = properties;
    }
    
	@Override
	protected String getWidgetTitle() {
		return WIDGET_TITLE;
	}

	@Override
	protected String getWidgetBackgroundColor() {
		return WIDGET_BACKGROUND_COLOR;
	}

	@Override
	protected QueryWidget createQueryWidget() {
		return new ContainerQueryWidget() {
			@Override
			protected QueryWidget createQueryWidget() {
				return new PropertyQueryWidget(properties) {
					@Override
					public SuggestionComboBox getSuggestionComboBox(Property property) {
						return new SubjectComboBox(property, true);
					}
				};
			}
			
		};
	}
	
	@Override
	public QueryWidget addNewRow() {
		ContainerQueryWidget queryWidget = (ContainerQueryWidget)super.addNewRow();
		
		queryWidget.addNewRow();
		
		return queryWidget;
	}

    @Override
    public RestrictionFilterConfig getFilterConfig() {
        SubjectFilterConfig config = new SubjectFilterConfig();
        Conjunction conjunction = new Conjunction();
        Iterator<Widget> iterator = this.queriesPanel.iterator();
        while ( iterator.hasNext() ) {
            QueryWidget queryWidget = (QueryWidget) iterator.next();
            conjunction.add( queryWidget.getRestrictionExpression() );
        }
        config.setRestriction( conjunction );
        return config;
    }

	@Override
	public void setFilterConfig(RestrictionFilterConfig filterConfig) {
		Conjunction conjunction = (Conjunction)filterConfig.getRestriction();
		for (final RestrictionExpression restriction: conjunction.getRestrictions()) {
			QueryWidget queryWidget = super.addNewRow();
        	queryWidget.setRestrictionExpression(restriction);
		}
	}
}
