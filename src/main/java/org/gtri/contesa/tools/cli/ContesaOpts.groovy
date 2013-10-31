package org.gtri.contesa.tools.cli

/**
 * A class to manage the options within the contesa cli.
 * User: brad
 * Date: 10/31/13 9:25 AM
 */
class ContesaOpts {

    /**
     * Follows the convention of ssh, where you can do multiple 'v' to get more verbosity.  Ie, -v < -vv < -vvv ...
     */
    public static Integer VERBOSE_LEVEL = 0;

    /**
     * If true, the CLI should error for each ruleset present in contesa, but not matched by the given instance.
     */
    public static Boolean ERROR_ON_NO_MATCH = Boolean.TRUE;

    /**
     * The location of the instance to validate on disk.
     */
    public static String INSTANCE_PATH = null;



}//end ContesaOpts