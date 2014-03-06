package org.gtri.contesa.tools.cli

import java.text.SimpleDateFormat

/**
 * Created by brad on 3/6/14.
 */
class LogEntryFormatterXml implements LogEntryFormatter {

    protected SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public LogEntryFormatterXml(){}
    public LogEntryFormatterXml(String datePattern){
        dateFormatter = new SimpleDateFormat(datePattern);
    }

    @Override
    String format(LogEntry entry) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<log-entry timestamp=\"");
        buffer.append(sanitize(dateFormatter.format(entry.timestamp.time)));
        buffer.append("\">")
        buffer.append("<type>")
        buffer.append(sanitize(entry.type))
        buffer.append("</type><message>")
        buffer.append(sanitize(entry.message))
        buffer.append("</message><data>")
        entry.data?.keySet()?.each {String key ->
            buffer.append("<item key=\">")
            buffer.append(sanitize(key))
            buffer.append("\">")
            buffer.append( sanitize(entry.data.get(key).toString()) )
            buffer.append("</item>");
        }
        buffer.append("</data></log-entry>")
        return buffer.toString();
    }

    protected String sanitize(String str){
        String sanitized = str?.trim();
        if( sanitized && sanitized.length() > 0 ){
            sanitized = sanitized.replace("<", "&lt;");
            sanitized = sanitized.replace(">", "&gt;");
            sanitized = sanitized.replace("&", "&amp;");
        }else{
            sanitized = "";
        }
        return sanitized;
    }

}
