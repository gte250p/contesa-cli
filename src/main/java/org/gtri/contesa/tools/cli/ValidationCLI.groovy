package org.gtri.contesa.tools.cli

import gtri.logging.Logger
import gtri.logging.LoggerFactory

/**
 * Entry point class for command line validation.
 * <br/><br/>
 * User: brad
 * Date: 10/29/13 4:42 PM
 */
class ValidationCLI {

    public static final Logger logger = LoggerFactory.get(ValidationCLI);

    public static ContesaOpts CONTESA_OPTIONS = null;


    public static ContesaOpts parseArgs( String[] args ) {
        def opts = new ContesaOpts()
        args.each { arg ->
            if( arg.startsWith("-v") ){
                // Count how many v's there are...
                arg.chars.each { c ->
                    if( c == 'v' ){
                        opts.VERBOSE_LEVEL++;
                    }
                }
            }

            if( arg.equalsIgnoreCase("-ne") || arg.equalsIgnoreCase("--no-error-on-no-match") ){
                opts.ERROR_ON_NO_MATCH = Boolean.FALSE;
            }

            if( arg.startsWith("-") ){
                throw new Exception("Unrecognized option: ${arg}");
            }else{
                if( !opts.INSTANCE_PATH )
                    opts.INSTANCE_PATH = arg;
                else
                    throw new Exception("Cannot contain more than one file path to validate.  The system saw ${CONTESA_OPTIONS.INSTANCE_PATH} first.")
            }
        }
        return opts;
    }//end parseArgs()

    public static void main(String[] args){
        try{
            CONTESA_OPTIONS = parseArgs(args);



            System.exit(0);
        }catch(Throwable t){
            logger.error(t.getMessage());
            System.exit(1);
        }
    }//end main()



}//end ValidationCLI