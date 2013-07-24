package ubc.pavlab.aspiredb.shared;

import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;

import java.util.Set;

public interface AspireDbPagingLoadConfig extends PagingLoadConfig {
	
	  Set<AspireDbFilterConfig> getFilters();

	  void setFilters(Set<AspireDbFilterConfig> filters);

}
