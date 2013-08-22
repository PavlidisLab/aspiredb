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
package ubc.pavlab.aspiredb.shared;

import org.directwebremoting.annotations.DataTransferObject;

import java.util.Collection;

/**
 * Neurocarta phenotype value object
 * 
 * @author frances
 * @version $Id: NeurocartaPhenotypeValueObject.java,v 1.6 2013/06/11 22:30:57 anton Exp $
 */
@DataTransferObject(javascript = "NeurocartaPhenotypeValueObject")
public class NeurocartaPhenotypeValueObject extends PhenotypeValueObject implements Displayable {

	private static final long serialVersionUID = -7292996170070245025L;

    private Collection<GeneValueObject> genes;

    private int geneCount;
	
	public NeurocartaPhenotypeValueObject() {}

	public int getGeneCount() {
		return this.geneCount;
	}

	public void setGeneCount(int geneCount) {
		this.geneCount = geneCount;
	}

    public Collection<GeneValueObject> getGenes() {
        return genes;
    }

    public void setGenes(Collection<GeneValueObject> genes) {
        this.genes = genes;
    }

	@Override
	public String getLabel() {
		return this.getName();
	}

	@Override
	public String getHtmlLabel() {
		return this.getName();
	}

	@Override
	public String getTooltip() {
		return this.getName() + " - " + this.geneCount + " genes";
	}
}
