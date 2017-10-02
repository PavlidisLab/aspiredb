/*
 * The aspiredb project
 * 
 * Copyright (c) 2013 University of British Columbia
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
package ubc.pavlab.aspiredb.server.util;

import java.util.List;

import ubc.pavlab.aspiredb.shared.GeneValueObject;

/**
 * Gemma URL Utils
 * 
 * @version $Id: GemmaURLUtils.java,v 1.3 2013/07/05 00:02:36 frances Exp $
 */
public class GemmaURLUtils {

    private static final String GEMMA_URL = ConfigUtils.getGemmaBaseUrl();

    private static final String GEMMA_WEB_SERVICE_URL = GEMMA_URL + "/rest/v2";

    private static final String HELP_PAGE_URL = "http://aspiredb.sites.olt.ubc.ca/";

    public static String getHelpPageURL() {
        return HELP_PAGE_URL;
    }

    public static String makeGeneUrl( String geneSymbol ) {
        return GEMMA_URL + "/gene/showGene.html?name=" + geneSymbol + "&taxon=human";
    }

    public static String makeNeurocartaPhenotypeUrl( String phenotypeUri ) {
        return GEMMA_URL + "/phenotypes.html?phenotypeUrlId=" + phenotypeUri;
    }

    public static String makeViewGeneNetworkInGemmaURL( List<GeneValueObject> genes ) {

        String url = GEMMA_URL + "/home.html?taxon=1&geneList=";

        for ( GeneValueObject gvo : genes ) {

            url = url + gvo.getSymbol() + ",";

        }

        return url;

    }

    public static String makeWebServiceUrl( String path ) {
        return GEMMA_WEB_SERVICE_URL + path;
    }
}
