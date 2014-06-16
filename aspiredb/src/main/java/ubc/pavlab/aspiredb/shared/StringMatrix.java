package ubc.pavlab.aspiredb.shared;

import java.io.Serializable;
import java.util.Collection;

import org.directwebremoting.annotations.DataTransferObject;

@DataTransferObject(javascript = "StringMatrix")
public class StringMatrix<R, C> implements Serializable {

    private Collection<R> rowNames;
    private Collection<C> columnNames;
    private String[][] matrix;

    /**
     * 
     */
    private static final long serialVersionUID = 8061560995437516094L;

    public StringMatrix( int x, int y ) {
        matrix = new String[x][y];
    }

    public String get( int i, int j ) {
        return matrix[i][j];
    }

    public Collection<C> getColumnNames() {
        return columnNames;
    }

    public String[][] getMatrix() {
        return this.matrix;
    }

    public Collection<R> getRowNames() {
        return rowNames;
    }

    public String set( int i, int j, String value ) {
        return matrix[i][j] = value;
    }

    public void setColumnNames( Collection<C> columnNames ) {
        this.columnNames = columnNames;
    }

    public void setMatrix( String[][] matrix ) {
        this.matrix = matrix;
    }

    public void setRowNames( Collection<R> rowNames ) {
        this.rowNames = rowNames;
    }

}
