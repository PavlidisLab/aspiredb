package ubc.pavlab.aspiredb.shared;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface OntologyTermValueObjectProperties extends PropertyAccess<OntologyTermValueObject> {

    ModelKeyProvider<OntologyTermValueObject> key();

    ValueProvider<OntologyTermValueObject, String> name();
    ValueProvider<OntologyTermValueObject, String> uri();

}
