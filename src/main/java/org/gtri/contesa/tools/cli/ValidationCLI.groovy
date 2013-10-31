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


    public static void main(String[] args){
        try{
            CONTESA_OPTIONS = new ContesaOpts(args);
            validateOptions(CONTESA_OPTIONS);



            System.exit(0);
        }catch(Throwable t){
            logger.error(t.getMessage());
            System.exit(1);
        }
    }//end main()


    private static void validateOptions(ContesaOpts opts){
        if( opts.instancePath == null || opts.instancePath.trim().length() == 0 ){
            throw new Exception("Missing required instance file to validate.")
        }
    }//end opts()



}//end ValidationCLI