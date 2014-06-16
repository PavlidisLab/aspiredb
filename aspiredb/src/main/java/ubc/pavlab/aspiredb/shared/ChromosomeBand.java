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
 * author: anton date: 19/02/13
 */
@DataTransferObject
public class ChromosomeBand implements Serializable {
    private int start;
    private int end;
    private String name;
    private String staining;

    public ChromosomeBand() {
    };

    public ChromosomeBand( int start, int end, String name, String staining ) {
        this.start = start;
        this.end = end;
        this.name = name;
        this.staining = staining;
    }

    public int getEnd() {
        return end;
    }

    public String getName() {
        return name;
    }

    public String getStaining() {
        return staining;
    }

    public int getStart() {
        return start;
    }
}
