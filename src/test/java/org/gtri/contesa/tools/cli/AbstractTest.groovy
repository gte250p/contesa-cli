package org.gtri.contesa.tools.cli

import gtri.logging.Logger
import gtri.logging.LoggerFactory
import org.junit.After
import org.junit.Before

/**
 * Provides basic test infrastructure.
 * <br/><br/>
 * User: brad
 * Date: 10/31/13 9:45 AM
 */
abstract class AbstractTest {

    public static Logger logger = LoggerFactory.get(AbstractTest);


    @Before
    public void printStart(){
        logger.info("================================ STARTING TEST =================================")
    }
    @After
    public void printStop(){
        logger.info("================================ STOPPING TEST =================================\n\n")
    }


}
