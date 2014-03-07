package org.gtri.contesa.tools.cli

import org.gtri.contesa.tools.reporting.ReportFormat

/**
 * A class to manage the options within the contesa cli.
 * User: brad
 * Date: 10/31/13 9:25 AM
 */
class ContesaOpts {

    public static ContesaOpts INSTANCE = null;

    /**
     * Follows the convention of ssh, where you can do multiple 'v' to get more verbosity.  Ie, -v < -vv < -vvv ...
     */
    public Integer verbosity = 0;

    /**
     * The location of the instance to validate on disk.
     */
    public String instancePath = null;
    public File instanceFile = null;

    /**
     * The output file to write result XML into.  May be null, in which case it will go to standard out.
     */
    public String outFilePath = null;
    public boolean hasOutFilePath() {
        return outFilePath != null && outFilePath.trim().length() > 0;
    }

    /**
     * The output file to write contesa logging into.  May be null, in which case it will go to standard out/err.  It
     * is recommended that you suppress logging when outputting a file to the console.
     */
    public String logFilePath = null;
    public boolean shouldOutputLog(){
        return (this.logFilePath && this.logFilePath.trim().length() > 0);
    }

    /**
     * The output file to write contesa status (event listener events) into.  May be null, in which case output is suppressed.
     */
    public String statusFilePath = null;
    public boolean shouldOutputStatusFile(){
        return (this.statusFilePath && this.statusFilePath.trim().length() > 0);
    }

    /**
     * The type of status lines to output.  Supported options are text|xml.
     */
    public String statusEntryFormat = "text";
    public LogEntryFormatter statusEntryFormatter = new LogEntryFormatterText();

    /**
     * Represents the reportFormats the user has expressed to be on the command line.  XML only by default.
     */
    public List<ReportFormat> reportFormats = [ReportFormat.XML];
    public Boolean hasCustomizedReportFormat = Boolean.FALSE;
    public Boolean xmlOnlyReport() {
        Boolean xmlOnlyReport = Boolean.TRUE;
        for( ReportFormat format : reportFormats )
            if( format != ReportFormat.XML )
                xmlOnlyReport = Boolean.FALSE;
        return xmlOnlyReport;
    }


    public ContesaOpts(String[] args){
        args.each { arg ->
            if( arg.startsWith("-v") ){
                // Count how many v's there are...
                arg.chars.each { c ->
                    if( c == 'v' ){
                        verbosity++;
                        if( verbosity > 4 ){
                            throw new Exception("Only 4 levels of verbose are supported.")
                        }
                    }
                }

            }else if( arg.startsWith("-o=") || arg.toLowerCase().startsWith("--out-file=") ){
                outFilePath = parseArgValue(arg);

            }else if( arg.startsWith("-l=") || arg.toLowerCase().startsWith("--log=") ){
                logFilePath = parseArgValue(arg);

            }else if( arg.startsWith("-s=") || arg.toLowerCase().startsWith("--status=") ){
                statusFilePath = parseArgValue(arg);

            }else if( arg.startsWith("-sf=") || arg.toLowerCase().startsWith("--status-format=") ){
                statusEntryFormat = parseArgValue(arg)?.toLowerCase();
                if( !statusEntryFormat.equals("text") && !statusEntryFormat.equals("xml") ){
                    throw new Exception("Unsupported status file format: "+statusEntryFormat+".  Expected 'text' or 'xml'.");
                }
                if( statusEntryFormat.equalsIgnoreCase("xml") ){
                    statusEntryFormatter = new LogEntryFormatterXml();
                }else if( statusEntryFormat.equalsIgnoreCase("text") ){
                    statusEntryFormatter = new LogEntryFormatterText();
                }
            }else if( arg.startsWith("-rf=") || arg.toLowerCase().startsWith("--report-format=") ){
                if( !hasCustomizedReportFormat ){
                    hasCustomizedReportFormat = Boolean.TRUE
                    reportFormats = []
                }

                String reportFormat = parseArgValue(arg)?.toUpperCase();
                if( reportFormat.equalsIgnoreCase("XML") ){
                    reportFormats.add(ReportFormat.XML)
                }else if( reportFormat.equalsIgnoreCase("HTML") ){
                    reportFormats.add(ReportFormat.HTML)
                }else if( reportFormat.equalsIgnoreCase("EXCEL") ){
                    reportFormats.add(ReportFormat.EXCEL)
                }else if( reportFormat.equalsIgnoreCase("ALL") ){
                    reportFormats.add(ReportFormat.XML)
                    reportFormats.add(ReportFormat.HTML)
                    reportFormats.add(ReportFormat.EXCEL)
                }else{
                    throw new Exception("Unable to determine type of report to generate! ${reportFormat} is not supported.")
                }

            }else if( arg.startsWith("-") ){
                throw new Exception("Unrecognized option: ${arg}");

            }else{
                if( !instancePath ){
                    instancePath = arg;
                    instanceFile = resolvePath(instancePath);
                }else
                    throw new Exception("Cannot contain more than one file path to validate.  The system saw ${instancePath} first.")
            }
        }

        if( hasCustomizedReportFormat && !outFilePath ){
            throw new Exception("Using customized report formats requires that you set an output file (do nothing to spit XML to command line).")
        }else if( hasCustomizedReportFormat && outFilePath && verbosity < 1 ){
            verbosity = 1; // This way there is at least some command line output to stdout.
        }

        INSTANCE = this;
    }//end public constructor

    protected File resolvePath(String path){
        return resolvePath(path, false);
    }

    protected File resolvePath(String path, Boolean isDirectory){
        File file = new File(path);
        if( !file.exists() )
            throw new FileNotFoundException("Could not locate file: $path");
        if( !isDirectory && file.isDirectory() )
            throw new Exception("Expecting a file, but found a directory at: $path")
        return file;
    }

    protected String parseArgValue( String arg ){
        def index1 = arg.indexOf('=') + 1
        def rest = arg.substring(index1);
        if( rest.trim().length() == 0 )
            throw new Exception("Argument '$arg' requires a value.")
        return rest.trim();
    }//end parseArgValue()


}//end ContesaOpts
