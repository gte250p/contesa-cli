package org.gtri.contesa.tools.cli

import java.text.SimpleDateFormat

/**
 * Created by brad on 3/6/14.
 */
class LogEntryFormatterText implements LogEntryFormatter {

    protected SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public LogEntryFormatterText(){}
    public LogEntryFormatterText(String datePattern){
        dateFormatter = new SimpleDateFormat(datePattern);
    }

    @Override
    String format(LogEntry entry) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(sanitize(dateFormatter.format(entry.timestamp.time)));
        buffer.append("|")
        buffer.append(sanitize(entry.type))
        buffer.append("|")
        buffer.append(sanitize(entry.message))
        buffer.append("|")
        entry.data?.keySet()?.each {String key ->
            buffer.append(sanitize(key))
            buffer.append("=")
            buffer.append( sanitize(entry.data.get(key).toString()) )
            buffer.append("|");
        }
        return buffer.toString();
    }

    protected String sanitize(String str){
        String sanitized = str?.trim();
        if( sanitized && sanitized.length() > 0 ){
            sanitized = sanitized.replace("|", "\\|"); // Escape out any pipes with back-slash pipe.
        }else{
            sanitized = "";
        }
        return sanitized;
    }

}
