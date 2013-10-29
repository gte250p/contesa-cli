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


    static CliBuilder buildCliBuilder() {
        CliBuilder cli = new CliBuilder();



        return cli;
    }//end buildCliBuilder()

    public static void main(String[] args){
        logger.info("Starting application...");
    }//end main()

}//end ValidationCLI