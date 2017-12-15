package com.bbpos.bbdevice.util.log;

public class ApplicationIOException extends ApplicationException
{
    private static final long serialVersionUID = -4875896103944285909L;

    public ApplicationIOException()
    {
        super();
    }

    public ApplicationIOException(String message)
    {
        super(message);
    }

    public ApplicationIOException(String area, String message)
    {
        super(area + " : " + message);
    }

    public ApplicationIOException(String area, String message, Exception innerException)
    {
        super(area + " : " + message + " " + innerException.toString());
    }
}

