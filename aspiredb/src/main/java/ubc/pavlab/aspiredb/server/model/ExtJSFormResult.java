package ubc.pavlab.aspiredb.server.model;

/**
 * A simple return message for Ext JS
 * 
 * @author Loiane Groner http://loiane.com http://loianegroner.com
 */
public class ExtJSFormResult {

    private boolean success;
    private String message;
    private Object data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess( boolean success ) {
        this.success = success;
    }

    public void setMessage( String message ) {
        this.message = message;
    }

    public void setData( Object data ) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "{\"success\":" + this.success + ", \"message\" : \"" + this.message + "\", \"data\" : "
                + this.data.toString() + " }";
    }
}