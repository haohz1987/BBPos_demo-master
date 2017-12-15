package com.bbpos.bbdevice.util.log;

public class ApplicationException extends RuntimeException
{
    private static final long serialVersionUID = -537542772102965055L;

    public ApplicationException()
    {
        super();
    }

    public ApplicationException(String message)
    {
        super(message);
    }

    public ApplicationException(String area, String message)
    {
        super(area + " : " + message);
    }

    public ApplicationException(String area, String message, Exception innerException)
    {
        super(area + " : " + message + " " + innerException.toString());
    }

}
