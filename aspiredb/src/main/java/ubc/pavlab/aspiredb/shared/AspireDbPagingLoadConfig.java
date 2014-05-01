package ubc.pavlab.aspiredb.shared;

import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import org.directwebremoting.annotations.DataTransferObject;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;

import java.util.Set;

@DataTransferObject(javascript = "AspireDbPagingLoadConfig")
public interface AspireDbPagingLoadConfig extends PagingLoadConfig {
	
	  Set<AspireDbFilterConfig> getFilters();

	  void setFilters(Set<AspireDbFilterConfig> filters);

}
