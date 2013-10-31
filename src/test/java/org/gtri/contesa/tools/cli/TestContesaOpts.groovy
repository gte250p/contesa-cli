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
class TestContesaOpts extends AbstractTest {


    private void assertDefaults(ContesaOpts opts){
        assertThat(opts, notNullValue())
        assertThat(opts.verbosity, notNullValue())
        assertThat(opts.verbosity, equalTo(0))
        assertThat(opts.errorOnNoMatch, notNullValue())
        assertThat(opts.errorOnNoMatch, equalTo(Boolean.TRUE))
        assertThat(opts.instancePath, notNullValue())
    }

    @Test
    public void testOptionParsingForFileOnly() {
        logger.info("Testing that we can parse file only arguments correctly...")
        def PATH1 = "/a/path/to/file"
        def args = [PATH1] as String[];
        ContesaOpts opts = new ContesaOpts(args);
        assertDefaults(opts);
        assertThat(opts.instancePath, equalTo(PATH1))
        logger.info("Successfully tested single file arg only case.")
    }//end testOptionParsingForFileOnly()


    @Test
    public void testVerbosityLevelsInArgs() {
        logger.info("Testing verbosity in arguments correctly...")

        def args = ["-v"] as String[];
        ContesaOpts opts = new ContesaOpts(args);
        assertThat(opts.verbosity, equalTo(1))

        args = ["-vv"] as String[];
        opts = new ContesaOpts(args);
        assertThat(opts.verbosity, equalTo(2))

        args = ["-vvv"] as String[];
        opts = new ContesaOpts(args);
        assertThat(opts.verbosity, equalTo(3))

        args = ["-vvvv"] as String[];
        opts = new ContesaOpts(args);
        assertThat(opts.verbosity, equalTo(4))

        logger.info("Successfully tested verbosity in arguments.")
    }//end testVerbosityLevelsInArgs()


}//end TestValidationCLI()