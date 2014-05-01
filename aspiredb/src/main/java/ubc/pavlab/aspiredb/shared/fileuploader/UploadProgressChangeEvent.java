package ubc.pavlab.aspiredb.shared.fileuploader;

import java.io.Serializable;

public final class UploadProgressChangeEvent implements FileEvent, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1906882050263042109L;
    private String filename;
    private Integer percentage;

    public UploadProgressChangeEvent() {
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename( final String filename ) {
        this.filename = filename;
    }

    public Integer getPercentage() {
        return percentage;
    }

    public void setPercentage( final Integer percentage ) {
        this.percentage = percentage;
    }

    @Override
    public String toString() {
        return filename + " - " + percentage;
    }
}