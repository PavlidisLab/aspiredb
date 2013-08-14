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
package ubc.pavlab.aspiredb.client;

import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.uibinder.client.UiConstructor;
import com.sencha.gxt.cell.core.client.LabelProviderSafeHtmlRenderer;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.event.BeforeQueryEvent;
import com.sencha.gxt.widget.core.client.event.RefreshEvent;
import com.sencha.gxt.widget.core.client.form.ComboBox;

public class AutoSuggestComboBox<T> extends ComboBox<T> {
	private static class AutoSuggestComboBoxCell<T> extends ComboBoxCell<T> {
		public AutoSuggestComboBoxCell(ListStore<T> store, LabelProvider<? super T> labelProvider, final SafeHtmlRenderer<T> renderer) {
			super(store, labelProvider, renderer);
		}

		public String getLastQuery() {
			return this.lastQuery;
		}
	}

	@UiConstructor
	public AutoSuggestComboBox(ListStore<T> store, LabelProvider<? super T> labelProvider) {
		super(new AutoSuggestComboBoxCell<T>(store, labelProvider, new LabelProviderSafeHtmlRenderer<T>(labelProvider)));
		initializeComboBox();
	}

	public AutoSuggestComboBox(ListStore<T> store, LabelProvider<? super T> labelProvider, SafeHtmlRenderer<T> renderer) {
		super(new AutoSuggestComboBoxCell<T>(store, labelProvider, renderer));
		initializeComboBox();
	}

	private String getLastQuery() {
		ComboBoxCell<T> comboBoxCell = this.getCell();
		
		final String lastQuery;
		
		if (comboBoxCell instanceof AutoSuggestComboBoxCell) {
			lastQuery = ((AutoSuggestComboBoxCell<T>) comboBoxCell).getLastQuery(); 
		} else {
			lastQuery = null;
		}
		
		return lastQuery;
	}

	private void initializeComboBox() {
		this.addBeforeQueryHandler(new BeforeQueryEvent.BeforeQueryHandler<T>() {
			@Override
			public void onBeforeQuery(BeforeQueryEvent<T> event) {
				AutoSuggestComboBox<T> comboBox = (AutoSuggestComboBox<T>) event.getSource();

				if (event.getQuery().length() >= comboBox.getMinChars()) {
					comboBox.expand();

					if (!event.getQuery().equals(comboBox.getLastQuery())) {
						comboBox.getListView().mask("Searching...");
					}
				} else {
					comboBox.getListView().unmask();
					comboBox.collapse();
				}
			}
		});

        this.getListView().addRefreshHandler( new RefreshEvent.RefreshHandler() {
            @Override
            public void onRefresh(RefreshEvent event) {
            	// Make the scrollbar appear after refresh.
            	AutoSuggestComboBox.this.getListView().unmask();
            }
        });
	}
}
