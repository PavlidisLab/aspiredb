package ubc.pavlab.aspiredb.shared.suggestions;

import java.io.Serializable;

import org.directwebremoting.annotations.DataTransferObject;

@DataTransferObject(javascript = "PhenotypeSuggestion")
public class PhenotypeSuggestion implements Serializable {

    private String name;
    private String uri;

    private boolean isOntologyTerm;
    private boolean existInDatabase;

    public PhenotypeSuggestion() {
    }

    public PhenotypeSuggestion( String name ) {
        this.name = name;
    }

    public PhenotypeSuggestion( String name, String uri ) {
        this.name = name;
        this.uri = uri;
        this.isOntologyTerm = true;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public boolean isExistInDatabase() {
        return existInDatabase;
    }

    public boolean isOntologyTerm() {
        return isOntologyTerm;
    }

    public void setExistInDatabase( boolean existInDatabase ) {
        this.existInDatabase = existInDatabase;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public void setOntologyTerm( boolean ontologyTerm ) {
        isOntologyTerm = ontologyTerm;
    }

    public void setUri( String uri ) {
        this.uri = uri;
    }
}
