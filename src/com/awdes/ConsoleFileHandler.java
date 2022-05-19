package com.awdes;

import java.util.logging.*;

public class ConsoleFileHandler extends ConsoleHandler
{
    private static Logger LOG = LOG = Logger.getLogger(ConsoleFileHandler.class.getName());;
    private FileHandler fileHandler;

    public ConsoleFileHandler()
    {
        try
        {
            fileHandler = new FileHandler();
        } catch (Exception ex)
        {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void close()
    {
        super.close();
        if (fileHandler != null)
        {
            fileHandler.close();
        }
    }

    @Override
    public void publish(LogRecord record)
    {
        super.publish(record);
        if (fileHandler != null)
        {
            fileHandler.publish(record);
        }
    }
}
