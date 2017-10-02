package ubc.pavlab.aspiredb.server.gemma;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.service.QueryServiceTest;
import ubc.pavlab.aspiredb.shared.GeneValueObject;
import ubc.pavlab.aspiredb.shared.NeurocartaPhenotypeValueObject;

import java.util.Collection;

import static org.junit.Assert.assertFalse;

/**
 * Created by mjacobson on 02/10/17.
 */
public class NeurocartaQueryServiceTest extends BaseSpringContextTest {

    private static Log log = LogFactory.getLog( NeurocartaQueryServiceTest.class.getName() );

    @Autowired
    private NeurocartaQueryService neurocartaQueryService;

    /**
     * Tests Gemma candidate genes endpoint
     *
     * @throws Exception
     */
    @Test
    public void testCandidateGenesEndpoint() throws Exception {
        Collection<GeneValueObject> res = neurocartaQueryService.fetchGenesAssociatedWithPhenotype( "http://purl.obolibrary.org/obo/DOID_11934" );
        assertFalse( res.isEmpty() );
    }

    /**
     * Tests Gemma load phenotypes endpoint
     *
     * @throws Exception
     */
    @Test
    public void testLoadPhenotypesEndpoint() throws Exception {
        Collection<NeurocartaPhenotypeValueObject> res = neurocartaQueryService.fetchAllPhenotypes();
        // Also, don't just load the 3 root terms
        assertFalse( res.size() <= 3 );
    }
}
