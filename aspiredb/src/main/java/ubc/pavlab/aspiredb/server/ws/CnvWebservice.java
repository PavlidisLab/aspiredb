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
package ubc.pavlab.aspiredb.server.ws;

import javax.ws.rs.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ubc.pavlab.aspiredb.server.dao.VariantDao;

/**
 * TODO Document Me
 * 
 * @author ??
 * @version $Id: CnvWebservice.java,v 1.12 2013/06/11 22:30:50 anton Exp $
 */
@Service
@Path("/cnv")
public class CnvWebservice {

    @Autowired
    VariantDao variantDao;

    /**
     * Find matching CNVs and return a BED file
     * 
     * @param chromosome
     * @param start
     * @param end
     * @param servletResponse
     * @return
     */
    // @GET
    // @Path("/showCnv/chr/{chromosome}/start/{start}/end/{end}")
    // @Produces(MediaType.TEXT_PLAIN)
    // public String showCnv( @PathParam("chromosome") String chromosome, @PathParam("start") int start,
    // @PathParam("end") int end, @Context HttpServletResponse servletResponse ) {
    //
    // // Get CNVs from db.
    // Collection<Variant> cnvs = variantDao.findByGenomicLocation( new GenomicRange( chromosome, start, end ) );
    //
    // if ( cnvs.isEmpty() ) {
    // // FIXME what do we do here?
    // throw new IllegalArgumentException( "No variants in the region described" );
    // }
    //
    // // Make and return file.
    // return CnvToBed.create( cnvs, chromosome, start, end );
    // }

    // @GET
    // @Path("/showAllCnv/chr/{chromosome}/start/{start}/end/{end}")
    // @Produces(MediaType.TEXT_PLAIN)
    // public String showAllCnv( @PathParam("chromosome") String chromosome, @PathParam("start") int start,
    // @PathParam("end") int end, @Context HttpServletResponse servletResponse ) {
    //
    // // Get CNVs from db.
    // Collection<Variant> cnvs = variantDao.getAllOnChromosome( chromosome );
    //
    // if ( cnvs.isEmpty() ) {
    // // FIXME what do we do here?
    // throw new IllegalArgumentException( "No variants in the region described" );
    // }
    //
    // // Make and return file.
    // return CnvToBed.create( cnvs, chromosome, start, end );
    // }

}
