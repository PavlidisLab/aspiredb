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

import org.directwebremoting.annotations.DataTransferObject;

/**
 * Labels are for tagging Subjects and Variants of interest with a name and color. author: anton date: 11/04/13
 */
@DataTransferObject(javascript = "LabelValueObject")
public class LabelValueObject implements Displayable, Serializable {

    private static final long serialVersionUID = 5912945308104924604L;

    private String name;
    private Long id;
    private String colour;
    private Boolean isShown;
    private String description;

    public LabelValueObject() {
    }

    public LabelValueObject( Long id, String name ) {
        this.id = id;
        this.name = name;
        this.colour = "E6E6FA"; // TODO: define default
    }

    public LabelValueObject( Long id, String name, String colour ) {
        this.id = id;
        this.name = name;
        this.colour = colour;
        this.isShown = false;
    }

    public LabelValueObject( Long id, String name, String colour, Boolean isShown, String description ) {
        this.id = id;
        this.name = name;
        this.colour = colour;
        this.isShown = isShown;
        this.description = description;
    }

    public LabelValueObject( String name ) {
        this.name = name;
        this.colour = "E6E6FA"; // TODO: define default
    }

    public LabelValueObject( String name, String colour ) {
        this.name = name;
        this.colour = colour;
    }

    public String getColour() {
        return colour;
    }

    @Override
    public String getHtmlLabel() {
        // String fontcolor = ( Integer.parseInt( this.colour, 16 ) > 0xffffff / 2 ) ? "black" : "white";
        String fontcolor = "white";
        String backgroundColor = this.colour;
        try {
            // is the color in hex?
            Integer.parseInt( this.colour, 16 );
            backgroundColor = "#" + backgroundColor;
        } catch ( NumberFormatException nfe ) {
            // nope
        }

        return "<font color=" + fontcolor + "><span style='background-color: " + backgroundColor + "'>&nbsp&nbsp"
                + this.name + "&nbsp&nbsp</span></font>&nbsp&nbsp&nbsp";

    }

    public Long getId() {
        return id;
    }

    public Boolean getIsShown() {
        return isShown;
    }

    @Override
    public String getLabel() {
        return name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getTooltip() {
        return name;
    }

    public void setColour( String colour ) {
        this.colour = colour;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public void setIsShown( Boolean isShown ) {
        this.isShown = isShown;
    }

    public void setName( String name ) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription( String description ) {
        this.description = description;
    }
}
