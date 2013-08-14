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

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import ubc.pavlab.aspiredb.shared.query.Property;
import ubc.pavlab.aspiredb.shared.query.RestrictionFilterConfig;
import ubc.pavlab.aspiredb.shared.query.VariantFilterConfig;
import ubc.pavlab.aspiredb.shared.query.restriction.Conjunction;
import ubc.pavlab.aspiredb.shared.query.restriction.Disjunction;
import ubc.pavlab.aspiredb.shared.query.restriction.RestrictionExpression;
import ubc.pavlab.aspiredb.shared.query.restriction.VariantTypeRestriction;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class VariantFilterWidget extends FilterWidget {
	private static final String WIDGET_TITLE = "Variant Filter";
	private static final String QUERIES_PANEL_TITLE = "Variant Location:";
	private static final String WIDGET_BACKGROUND_COLOR = "#FFFFD0";

	private VariantPropertyQueryWidget cnvQueryWidget;
	private VariantPropertyQueryWidget indelQueryWidget;
	private VariantPropertyQueryWidget snvQueryWidget;
	
	private Collection<Property> variantLocationProperties;
	
	public VariantFilterWidget(Map<String, Collection<Property>> variantProperties, Collection<Property> variantLocationProperties) {
		this.variantLocationProperties = variantLocationProperties;
		
		this.queriesPanelTitleHTML.setHTML(QUERIES_PANEL_TITLE);
		
		cnvQueryWidget = new VariantPropertyQueryWidget("CNV", variantProperties.get("CNV"));
		indelQueryWidget = new VariantPropertyQueryWidget("Indel", variantProperties.get("Indel"));
		snvQueryWidget = new VariantPropertyQueryWidget("SNV", variantProperties.get("SNV"));
		
		
		this.widgetContainer.add(new HTML("<hr />"));
		this.widgetContainer.add(new Label("Variant Characteristics:"));
		this.widgetContainer.add(cnvQueryWidget);
		this.widgetContainer.add(indelQueryWidget);
		this.widgetContainer.add(snvQueryWidget);
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
	public RestrictionFilterConfig getFilterConfig() {
        VariantFilterConfig config = new VariantFilterConfig();

        Conjunction conjunction = new Conjunction();

        Conjunction locationConjunction = new Conjunction();
        Iterator<Widget> iterator = this.queriesPanel.iterator();
        while ( iterator.hasNext() ) {
            QueryWidget queryWidget = (QueryWidget) iterator.next();
            locationConjunction.add( queryWidget.getRestrictionExpression() );
        }
        conjunction.add(locationConjunction);

		if (this.cnvQueryWidget.isSelected() || this.indelQueryWidget.isSelected() || this.snvQueryWidget.isSelected()) {
			Disjunction disjunction = new Disjunction();
			if (this.cnvQueryWidget.isSelected()) {
				disjunction.add(this.cnvQueryWidget.getRestrictionExpression());
			}
			if (this.indelQueryWidget.isSelected()) {
				disjunction.add(this.indelQueryWidget.getRestrictionExpression());
			}
			if (this.snvQueryWidget.isSelected()) {
				disjunction.add(this.snvQueryWidget.getRestrictionExpression());
			}
			
			conjunction.add(disjunction);
		}
        
        config.setRestriction( conjunction );
        return config;
	}
	
	@Override
	public void setFilterConfig(RestrictionFilterConfig filterConfig) {
		Conjunction conjunction = (Conjunction)filterConfig.getRestriction();
		for (final RestrictionExpression restriction: conjunction.getRestrictions()) {
			if (restriction instanceof Conjunction) {
				Conjunction locationConjunction = (Conjunction)restriction;
				
				for (final RestrictionExpression locationRestriction: locationConjunction.getRestrictions()) {
					QueryWidget queryWidget = super.addNewRow();
		        	queryWidget.setRestrictionExpression(locationRestriction);
					
				}
			} else if (restriction instanceof Disjunction) {
				for (final RestrictionExpression propertyRestriction: ((Disjunction)restriction).getRestrictions()) {
					if (propertyRestriction instanceof Conjunction) {
						for (RestrictionExpression conRestriction: ((Conjunction)propertyRestriction).getRestrictions()) {
							if (conRestriction instanceof VariantTypeRestriction) {
								switch (((VariantTypeRestriction)conRestriction).getType()) {
									case CNV:
										cnvQueryWidget.setRestrictionExpression(propertyRestriction);
										break;
									case INDEL:
										indelQueryWidget.setRestrictionExpression(propertyRestriction);
										break;
									case SNV:
										snvQueryWidget.setRestrictionExpression(propertyRestriction);
										break;
								}
								break;
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected QueryWidget createQueryWidget() {
		return new ContainerQueryWidget() {
			@Override
			protected QueryWidget createQueryWidget() {
				return new VariantLocationPropertyQueryWidget(variantLocationProperties);
			}
			
		};
	}

	@Override
	public QueryWidget addNewRow() {
		ContainerQueryWidget queryWidget = (ContainerQueryWidget)super.addNewRow();
		
		queryWidget.addNewRow();
		
		return queryWidget;
	}
}
