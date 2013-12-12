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
package ubc.pavlab.aspiredb.server.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ubc.pavlab.aspiredb.server.model.Project;
import ubc.pavlab.aspiredb.server.model.Subject;
import ubc.pavlab.aspiredb.server.model.Variant2SpecialVariantInfo;

import java.util.ArrayList;
import java.util.Collection;

@Repository("variant2SpecialVariantInfoDao")
public class Variant2SpecialVariantInfoDaoImpl extends DaoBaseImpl<Variant2SpecialVariantInfo> implements Variant2SpecialVariantInfoDao{
    
    protected static Log log = LogFactory.getLog( ProjectDaoImpl.class );
    
    @Autowired
    public Variant2SpecialVariantInfoDaoImpl( SessionFactory sessionFactory ) {
        super( Variant2SpecialVariantInfo.class );
        super.setSessionFactory( sessionFactory );
    }
    
    @Override
    @Transactional(readOnly = true)
    public Collection<Variant2SpecialVariantInfo> loadByVariantId( Long id , Collection<Long> overlapProjectIds) {
        
        if (id == null){
            return new ArrayList<Variant2SpecialVariantInfo>();
        }
        
        String[] paramNames = { "id", "overlapProjectIds" };
        Object[] objectValues = { id, overlapProjectIds };
        
        return this.getHibernateTemplate().findByNamedParam(
                "from Variant2SpecialVariantInfo where variantId = :id and overlapProjectId in (:overlapProjectIds)", paramNames, objectValues );
        
    }
    
    @Override
    @Transactional(readOnly = true)
    public Collection<Variant2SpecialVariantInfo> loadByVariantIdAndOverlap( Long id , Integer overlap, Integer operator, Collection<Long> overlapProjectIds) {
        
        if (id == null){
            return new ArrayList<Variant2SpecialVariantInfo>();
            
        }
        
        String[] paramNames = { "id", "overlap", "overlapProjectIds" };
        Object[] objectValues = { id, overlap, overlapProjectIds };
        
        String greaterThan = "from Variant2SpecialVariantInfo where variantId = :id and overlap > :overlap and overlapProjectId in (:overlapProjectIds)";
        String lessThan = "from Variant2SpecialVariantInfo where variantId = :id and overlap < :overlap and overlapProjectId in (:overlapProjectIds)";
        String equal = "from Variant2SpecialVariantInfo where variantId = :id and overlap = :overlap and overlapProjectId in (:overlapProjectIds)";
        
        String queryString = "";
        
        if (operator >0){
            queryString = greaterThan;
        }else if(operator<0){
            queryString = lessThan;
        }else{
            queryString = equal;
        }
        
        return this.getHibernateTemplate().findByNamedParam(
                queryString, paramNames, objectValues );
        
    }
    
}