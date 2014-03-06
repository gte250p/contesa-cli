package org.gtri.contesa.tools.cli

/**
 * Created by brad on 3/6/14.
 */
class LogEntry {

    Calendar timestamp = Calendar.getInstance()
    String type
    String message
    Map<String, Object> data = [:]


    public void addData( String key, Object value ){
        this.data.put(key, value);
    }

}
