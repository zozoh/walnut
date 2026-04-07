package org.nutz.mvc.upload;


public class UploadUnsupportedFileTypeException extends RuntimeException {

    public UploadUnsupportedFileTypeException(FieldMeta meta) {
        super(String.format("Unsupport file '%s' [%s] ",
                            meta.getFileLocalPath(),
                            meta.getContentType()));
    }

}
