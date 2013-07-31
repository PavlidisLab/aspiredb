package ubc.pavlab.aspiredb.server.valueobjects;

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



/**
 * author: anton
 * date: 11/04/13
 */
public class TestValueObject  {
    private static final long serialVersionUID = 5912945308104924604L;

    private String name;
    private Long id;
    private String colour;

    public TestValueObject() {
    }

    public TestValueObject(String name) {
        this.name = name;
        this.colour = "E6E6FA"; // TODO: define default
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
}