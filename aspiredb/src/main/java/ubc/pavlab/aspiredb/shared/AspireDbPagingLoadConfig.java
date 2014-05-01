package ubc.pavlab.aspiredb.shared;

import java.util.Set;

import org.directwebremoting.annotations.DataTransferObject;

import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;

import com.sencha.gxt.data.shared.loader.PagingLoadConfig;

@DataTransferObject(javascript = "AspireDbPagingLoadConfig")
public interface AspireDbPagingLoadConfig extends PagingLoadConfig {

    Set<AspireDbFilterConfig> getFilters();

    void setFilters( Set<AspireDbFilterConfig> filters );

}
