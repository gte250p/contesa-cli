package org.gtri.contesa.tools.cli

import org.junit.Assert
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

    static String POM_XML = "pom.xml"
    static String ASSEMBLY_XML = "./src/main/assembly/dist.xml"

    private void assertDefaults(ContesaOpts opts){
        assertValues(opts, 0, null);
    }

    private void assertDefaults(ContesaOpts opts, String path){
        assertValues(opts, 0, path);
    }

    private void assertValues(ContesaOpts opts, Integer verbosity, String instancePath){
        assertThat(opts, notNullValue())
        assertThat(opts.verbosity, notNullValue())
        assertThat(opts.verbosity, equalTo(verbosity))
        if( instancePath ){
            assertThat(opts.instancePath, notNullValue())
            assertThat(opts.instancePath, equalTo(instancePath))
        }else{
            assertThat(opts.instancePath, nullValue())
        }
    }

    @Test
    public void testOptionParsingForFileOnly() {
        logger.info("Testing that we can parse file only arguments correctly...")
        def args = [POM_XML] as String[];
        ContesaOpts opts = new ContesaOpts(args);
        assertDefaults(opts, POM_XML);
        logger.info("Successfully tested single file arg only case.")
    }//end testOptionParsingForFileOnly()


    @Test
    public void testVerbosityLevelsInArgs() {
        logger.info("Testing verbosity in arguments correctly...")

        def args = ["-v"] as String[];
        ContesaOpts opts = new ContesaOpts(args);
        assertValues(opts, 1, null);

        args = ["-vv"] as String[];
        opts = new ContesaOpts(args);
        assertValues(opts, 2, null);

        args = ["-vvv"] as String[];
        opts = new ContesaOpts(args);
        assertValues(opts, 3, null);

        args = ["-vvvv"] as String[];
        opts = new ContesaOpts(args);
        assertValues(opts, 4, null);

        args = ["-vvvvv"] as String[];
        try{
            opts = new ContesaOpts(args);
            Assert.fail("Expecting error when verbose level is above 4, but no error occurred!")
        }catch(Exception e){
            assertThat(e.message, equalTo("Only 4 levels of verbose are supported."))
        }

        logger.info("Successfully tested verbosity in arguments.")
    }//end testVerbosityLevelsInArgs()

    @Test
    public void testOnly1InstanceArgumentAllowed() {
        logger.info("Testing that only 1 instance path argument is allowed...")

        def args = [POM_XML] as String[];
        ContesaOpts opts = new ContesaOpts(args);
        assertDefaults(opts, POM_XML);

        try{
            args = [POM_XML, ASSEMBLY_XML] as String[];
            opts = new ContesaOpts(args)
            Assert.fail("Expecting error on 2 instance paths, but that did not occur.")
        }catch(Exception e){
            assertThat(e.message, startsWith("Cannot contain more than one file path to validate"))
        }

        logger.info("Successfully verified only 1 instance path is allowed.")
    }//end testOnly1InstanceArgumentAllowed()


    @Test
    public void testSetLogFilePath() {
        logger.info("Testing that setting log file path works ok...");

        def args = ["-l=/path/1"] as String[];
        ContesaOpts opts = new ContesaOpts(args);
        assertDefaults(opts);
        assertThat(opts.logFilePath, notNullValue())
        assertThat(opts.logFilePath, equalTo("/path/1"))

        args = ["--log=/path/1"] as String[];
        opts = new ContesaOpts(args);
        assertDefaults(opts);
        assertThat(opts.logFilePath, notNullValue())
        assertThat(opts.logFilePath, equalTo("/path/1"))

        args = ["--LOG=/path/1"] as String[];
        opts = new ContesaOpts(args);
        assertDefaults(opts);
        assertThat(opts.logFilePath, notNullValue())
        assertThat(opts.logFilePath, equalTo("/path/1"))

        logger.info("Successfully Tested that setting log file path works ok");
    }//end testSetLogFilePath()

    @Test
    public void testSetOutFilePath() {
        logger.info("Testing that setting out file path works ok...");

        def args = ["-o=/path/1"] as String[];
        ContesaOpts opts = new ContesaOpts(args);
        assertDefaults(opts);
        assertThat(opts.outFilePath, notNullValue())
        assertThat(opts.outFilePath, equalTo("/path/1"))

        args = ["--out-file=/path/1"] as String[];
        opts = new ContesaOpts(args);
        assertDefaults(opts);
        assertThat(opts.outFilePath, notNullValue())
        assertThat(opts.outFilePath, equalTo("/path/1"))

        args = ["--OUT-FILE=/path/1"] as String[];
        opts = new ContesaOpts(args);
        assertDefaults(opts);
        assertThat(opts.outFilePath, notNullValue())
        assertThat(opts.outFilePath, equalTo("/path/1"))

        logger.info("Successfully tested that setting out file path works ok");
    }//end testSetLogFilePath()


    @Test
    public void testCommonOptionCombinations() {
        logger.info("Testing that common option combinations should work ok.");

        // Simple single path option (all defaults except path)
        def args = [ASSEMBLY_XML] as String[];
        ContesaOpts opts = new ContesaOpts(args);
        assertDefaults(opts, ASSEMBLY_XML)

        // Simple "verbose" option
        args = ["-v", POM_XML] as String[];
        opts = new ContesaOpts(args);
        assertValues(opts, 1, POM_XML);

        // All arguments
        args = ["-v", ASSEMBLY_XML] as String[];
        opts = new ContesaOpts(args);
        assertValues(opts, 1, ASSEMBLY_XML);

        logger.info("Common options combinations seems fine.")
    }//end testCommonOptionCombinations()



}//end TestValidationCLI()
