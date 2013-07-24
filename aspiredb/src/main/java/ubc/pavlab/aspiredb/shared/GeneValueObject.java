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

import ubc.pavlab.aspiredb.client.view.suggestBox.Displayable;

/**
 * TODO Document Me
 * 
 * @author Paul
 * @version $Id: GeneValueObject.java,v 1.10 2013/06/11 22:30:57 anton Exp $
 */
public class GeneValueObject implements Displayable, GwtSerializable {

    private static final long serialVersionUID = -7411514301896256147L;

    private String key;
    private String symbol;
    private String name;
    private String taxon;
    private String ensemblId;
    private String linkToGemma;
    private String geneBioType;
    private GenomicRange genomicRange;
    
    
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol( String symbol ) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public GeneValueObject() {
    }

    public String getGeneBioType() {
		return geneBioType;
	}

	public void setGeneBioType(String geneBioType) {
		this.geneBioType = geneBioType;
	}

	public GeneValueObject( String ensemblId, String symbol, String geneName, String gene_biotype, String taxon ) {
		this.ensemblId = ensemblId;		
        this.symbol = symbol;
        this.name = geneName;
        this.geneBioType = gene_biotype;
        this.taxon = taxon;
        this.key = symbol + ":" + taxon;
    }

    public String getKey() {
        return key;
    }

    public void setKey( String key ) {
        this.key = key;
    }

    public String getEnsemblId() {
        return ensemblId;
    }

    public void setEnsemblId( String ensemblId ) {
        this.ensemblId = ensemblId;
    }

    public String getLinkToGemma() {
        return linkToGemma;
    }

    public void setLinkToGemma( String linkToGemma ) {
        this.linkToGemma = linkToGemma;
    }

    public String getTaxon() {
        return taxon;
    }

    public void setTaxon( String taxon ) {
        this.taxon = taxon;
    }

	public GenomicRange getGenomicRange() {
		return this.genomicRange;
	}

	public void setGenomicRange(GenomicRange genomicRange) {
		this.genomicRange = genomicRange;
	}

	@Override
	public String getLabel() {
        return symbol.equals("") ? ensemblId : symbol;
	}

	@Override
	public String getHtmlLabel() {
		return "<b>" + getLabel() + "</b>: " + name;
	}

	@Override
	public String getTooltip() {
		return getLabel() + ": " + name + " - " + this.genomicRange.toString();
	}
}
