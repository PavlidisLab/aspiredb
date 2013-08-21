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

import org.directwebremoting.annotations.DataTransferObject;

import java.io.Serializable;

/**
 * author: anton
 * date: 11/04/13
 */
@DataTransferObject
public class LabelValueObject implements Displayable, Serializable {
    private static final long serialVersionUID = 5912945308104924604L;

    private String name;
    private Long id;
    private String colour;

    public LabelValueObject() {
    }

    public LabelValueObject(String name) {
        this.name = name;
        this.colour = "E6E6FA"; // TODO: define default
    }

    public LabelValueObject(String name, String colour) {
        this.name = name;
        this.colour = colour;
    }

    public LabelValueObject(Long id, String name) {
        this.id = id;
        this.name = name;
        this.colour = "E6E6FA"; // TODO: define default
    }

    public LabelValueObject(Long id, String name, String colour) {
        this.id = id;
        this.name = name;
        this.colour = colour;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getColour() {
        return colour;
    }

    public String toString() {
    	return this.getName();
    }

    @Override
    public String getLabel() {
        return name;
    }

    @Override
    public String getHtmlLabel() {
        return "";
    }

    @Override
    public String getTooltip() {
        return "";
    }
}
