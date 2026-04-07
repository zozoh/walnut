package org.nutz.mvc.upload;


public class UploadUnsupportedFileNameException extends RuntimeException {

    public UploadUnsupportedFileNameException(FieldMeta meta) {
        super(String.format("Unsupport file name '%s' ", meta.getFileLocalPath()));
    }

}
