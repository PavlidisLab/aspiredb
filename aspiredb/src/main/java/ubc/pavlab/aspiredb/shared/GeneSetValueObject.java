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
 * author: gaya date: 06/05/14
 */
@DataTransferObject(javascript = "GeneSetValueObject")
public class GeneSetValueObject implements Displayable, Serializable {

   
    /**
     * auto generated serializable id
     */
    private static final long serialVersionUID = 3637627637659836236L;
    
    
    private String name;
    private Long id;
    private Serializable object;


    public GeneSetValueObject() {
    }

    public GeneSetValueObject( String name ) {
        this.name = name;
        
    }

    public GeneSetValueObject( String name, Serializable object ) {
        this.name = name;
        this.setObject( object );
    }

    public GeneSetValueObject( Long id, String name ) {
        this.id = id;
        this.name = name;
  
    }

   
    @Override
    public String getLabel() {
        return name;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }

   
    public void setName( String name ) {
        this.name = name;
    }

    
    @Override
    public String toString() {
        return this.getName();
    }

    
    public String getGeneSet() {
        return name;
    }

    @Override
    public String getHtmlLabel() {
        return name;
    }

    @Override
    public String getTooltip() {
        return "";
    }

    public Serializable getObject() {
        return object;
    }

    public void setObject( Serializable object ) {
        this.object = object;
    }
}
