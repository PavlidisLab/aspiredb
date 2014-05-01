package ubc.pavlab.aspiredb.server.dao;

import java.util.ArrayList;
import java.util.Collection;

public class PageBean<T> extends ArrayList<T> implements Page<T> {
    private static final long serialVersionUID = 2564380069117030730L;

    private int totalCount;

    public PageBean( Collection<T> data, int count ) {
        super( data );
        this.totalCount = count;
    }

    @Override
    public int getTotalCount() {
        return totalCount;
    }

}
