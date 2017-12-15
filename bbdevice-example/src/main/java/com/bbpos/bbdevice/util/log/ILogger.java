package com.bbpos.bbdevice.util.log;

import java.util.Date;

public interface ILogger
{
    void log(int level, Date date, String area, String message);
}
