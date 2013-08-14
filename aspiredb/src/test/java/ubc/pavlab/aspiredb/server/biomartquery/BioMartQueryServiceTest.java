package ubc.pavlab.aspiredb.server.biomartquery;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ubc.pavlab.aspiredb.server.BaseSpringContextTest;
import ubc.pavlab.aspiredb.server.exceptions.BioMartServiceException;
import ubc.pavlab.aspiredb.shared.GeneValueObject;


public class BioMartQueryServiceTest extends BaseSpringContextTest {
    @Autowired
    private BioMartQueryService bioMartQueryService;
    
    /**
     *  More of integration test (actually calls BioMart). 
     *  We check that we get some results (i.e. not an empty list).
     * 
     * @throws BioMartServiceException 
     */
    @Test
    public void testFetchGenesByGenomicLocation() throws BioMartServiceException {
        Collection<GeneValueObject> geneValueObjects = this.bioMartQueryService.fetchGenesByLocation( "7", 140000000L, 141000000L );
        
        assertNotNull (geneValueObjects);
        
        assertFalse (geneValueObjects.isEmpty());
                
        for (GeneValueObject geneValueObject : geneValueObjects) {
            assertNotNull( geneValueObject );
            assertNotNull( geneValueObject.getName() );
            assertNotNull( geneValueObject.getTaxon() );
        }
    }
    
}
