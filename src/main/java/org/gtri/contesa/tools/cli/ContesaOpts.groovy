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


    public ContesaOpts(String[] args){
        args.each { arg ->
            if( arg.startsWith("-v") ){
                // Count how many v's there are...
                arg.chars.each { c ->
                    if( c == 'v' ){
                        verbosity++;
                    }
                }
            }else if( arg.equalsIgnoreCase("-ne") || arg.equalsIgnoreCase("--no-error-on-no-match") ){
                errorOnNoMatch = Boolean.FALSE;
            }else if( arg.startsWith("-") ){
                throw new Exception("Unrecognized option: ${arg}");
            }else{
                if( !instancePath )
                    instancePath = arg;
                else
                    throw new Exception("Cannot contain more than one file path to validate.  The system saw ${instancePath} first.")
            }
        }
        INSTANCE = this;
    }


}//end ContesaOpts