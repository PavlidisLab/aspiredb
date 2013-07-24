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
package ubc.pavlab.aspiredb.shared;

import java.io.Serializable;

/**
 * Marker interface for classes/interfaces that are exposed to GWT front-end. That is return and parameter types of
 * remote service methods.
 *
 * The reason for this is that not everything that is Serializable (in Java sense) is actually serializable by GWT.
 * More info:
 * http://www.gwtapps.com/doc/html/com.google.gwt.doc.DeveloperGuide.RemoteProcedureCalls.SerializableTypes.html
 *
 * @author anton
 * @version $id$
 */
public interface GwtSerializable extends Serializable {
}
