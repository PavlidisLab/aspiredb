package ubc.pavlab.aspiredb.shared;

import com.sencha.gxt.data.shared.loader.PagingLoadConfigBean;
import org.directwebremoting.annotations.DataTransferObject;
import ubc.pavlab.aspiredb.shared.query.AspireDbFilterConfig;

import java.util.HashSet;
import java.util.Set;

@DataTransferObject(javascript = "AspireDbPagingLoadConfigBean")
public class AspireDbPagingLoadConfigBean extends PagingLoadConfigBean implements AspireDbPagingLoadConfig {

	private static final long serialVersionUID = 1795348301253031445L;
	
	private Set<AspireDbFilterConfig> filterConfigs = new HashSet<AspireDbFilterConfig>();
	  
	  @Override
	  public Set<AspireDbFilterConfig> getFilters() {
	    return filterConfigs;
	  }

	  @Override
	  public void setFilters(Set<AspireDbFilterConfig> filters) {
	    this.filterConfigs = filters;
	  }

}
