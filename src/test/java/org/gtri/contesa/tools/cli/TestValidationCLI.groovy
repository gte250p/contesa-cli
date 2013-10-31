package org.gtri.contesa.tools.cli

import org.junit.Test
import static org.hamcrest.Matchers.*
import static org.hamcrest.MatcherAssert.*

/**
 * Tests easy things WRT ValidationCLI Class.
 * <br/><br/>
 * User: brad
 * Date: 10/31/13 9:43 AM
 */
class TestValidationCLI extends AbstractTest {


    private void assertDefaults(ContesaOpts opts){
        assertThat(opts, notNullValue())
        assertThat(opts.VERBOSE_LEVEL, notNullValue())
        assertThat(opts.VERBOSE_LEVEL, equalTo(0))
        assertThat(opts.ERROR_ON_NO_MATCH, notNullValue())
        assertThat(opts.ERROR_ON_NO_MATCH, equalTo(Boolean.TRUE))
        assertThat(opts.INSTANCE_PATH, notNullValue())
    }

    @Test
    public void testOptionParsingForFileOnly() {
        logger.info("Testing that we can parse file only arguments correctly...")
        def PATH1 = "/a/path/to/file"
        def args = [PATH1] as String[];
        ContesaOpts opts = ValidationCLI.parseArgs(args);
        assertDefaults(opts);
        assertThat(opts.INSTANCE_PATH, equalTo(PATH1))
        logger.info("Successfully tested single file arg only case.")
    }//end testOptionParsingForFileOnly()

}
