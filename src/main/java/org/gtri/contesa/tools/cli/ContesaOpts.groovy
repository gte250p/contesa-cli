package org.gtri.contesa.tools.cli

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
     * If true, the CLI should error for each ruleset present in contesa, but not matched by the given instance.
     */
    public Boolean errorOnNoMatch = Boolean.TRUE;

    /**
     * The location of the instance to validate on disk.
     */
    public String instancePath = null;
    public File instanceFile = null;

    /**
     * The output file to write result XML into.  May be null, in which case it will go to standard out.
     */
    public String outFilePath = null;

    /**
     * The output file to write contesa logging into.  May be null, in which case it will go to standard out/err.  It
     * is recommended that you suppress logging when outputting a file to the console.
     */
    public String logFilePath = null;

    /**
     * A List of context paths forced to execute via the '--force' parameter.
     */
    public List<String> forcedContextPaths = new ArrayList<String>();


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

            }else if( arg.equals("-ne") || arg.equalsIgnoreCase("--no-error-on-no-match") ){
                errorOnNoMatch = Boolean.FALSE;

            }else if( arg.startsWith("-f=") || arg.toLowerCase().startsWith("--force=") ){
                String value = parseArgValue(arg);
                value = URLDecoder.decode(value, "UTF-8");
                if( !forcedContextPaths.contains(value) )
                    forcedContextPaths.add(value);

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
