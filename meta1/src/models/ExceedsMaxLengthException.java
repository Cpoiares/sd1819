package models;

public class ExceedsMaxLengthException extends Exception {
    private static final long serialVersionUID = 1L;

    public ExceedsMaxLengthException(String e) {
        super(e);
    }
}
