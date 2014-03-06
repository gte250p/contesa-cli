package org.gtri.contesa.tools.cli

/**
 * Created by brad on 3/6/14.
 */
interface LogEntryFormatter {

    /**
     * Formats the given log entry to a string value for putting into a Log.
     */
    public String format( LogEntry entry );

}
