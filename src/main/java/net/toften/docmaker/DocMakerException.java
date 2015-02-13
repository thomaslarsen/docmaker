package net.toften.docmaker;

public class DocMakerException extends Exception {

    private static final long serialVersionUID = -4305267497028543410L;

    public DocMakerException(final String string, final Exception e) {
        super(string, e);
    }

    public DocMakerException(final String string) {
        super(string);
    }

}
