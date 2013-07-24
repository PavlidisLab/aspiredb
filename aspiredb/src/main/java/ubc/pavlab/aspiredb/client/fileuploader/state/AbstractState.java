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
package ubc.pavlab.aspiredb.client.fileuploader.state;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class AbstractState implements State {

  private transient PropertyChangeSupport changes = new PropertyChangeSupport(this);

  @Override
  public final void addPropertyChangeListener(final PropertyChangeListener l) {
    changes.addPropertyChangeListener(l);
  }

  @Override
  public final void addPropertyChangeListener(final String propertyName,
          final PropertyChangeListener l) {
    changes.addPropertyChangeListener(propertyName, l);
  }

  @Override
  public final void firePropertyChange(final String propertyName,
          final Object oldValue, final Object newValue) {
    changes.firePropertyChange(propertyName, oldValue, newValue);
  }

  @Override
  public final void removePropertyChangeListener(final PropertyChangeListener l) {
    changes.removePropertyChangeListener(l);
  }

  @Override
  public final void removePropertyChangeListener(final String propertyName,
          final PropertyChangeListener l) {
    changes.removePropertyChangeListener(propertyName, l);
  }
}