package ubc.pavlab.aspiredb.shared;

import java.io.Serializable;

import org.directwebremoting.annotations.DataTransferObject;

@DataTransferObject
public class OntologyTermValueObject implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -8947605139804095762L;

    private String name;
    private String uri;

    public OntologyTermValueObject() {
    }

    public OntologyTermValueObject( String name, String uri ) {
        super();
        this.name = name;
        this.uri = uri;
    }

    public String getKey() {
        return this.name;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public void setUri( String uri ) {
        this.uri = uri;
    }

}
