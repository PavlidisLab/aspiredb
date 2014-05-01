package ubc.pavlab.aspiredb.server.dao;

import java.util.Collection;

public interface Page<T> extends Collection<T> {
    public int getTotalCount();
}
