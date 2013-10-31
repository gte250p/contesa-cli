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



            System.exit(0);
        }catch(Throwable t){
            logger.error(t.getMessage());
            System.exit(1);
        }
    }//end main()



}//end ValidationCLI