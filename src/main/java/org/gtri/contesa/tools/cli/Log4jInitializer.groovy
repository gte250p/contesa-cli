package org.gtri.contesa.tools.cli

import gtri.logging.Logger
import gtri.logging.LoggerFactory
import gtri.logging.log4j.JANSIPatternLayout
import org.apache.commons.io.IOUtils
import org.apache.log4j.FileAppender
import org.apache.log4j.Level
import org.apache.log4j.LogManager
import org.apache.log4j.Priority
import org.apache.log4j.PropertyConfigurator
import org.apache.log4j.xml.DOMConfigurator

/**
 * Initializes log4j based on verbosity and logging file options found in {@link ContesaOpts} class.
 * <br/>
 * User: brad
 * Date: 10/31/13 11:17 AM
 */
class Log4jInitializer {

    static Logger logger = LoggerFactory.get(Log4jInitializer)

    static void initialize() {
        initialize(ContesaOpts.INSTANCE)
    }//end initlaize()


    static void initialize(ContesaOpts opts){
        if( opts.verbosity > 0 ){
            String log4jFilename = opts.verbosity + ".log4j.xml";
            File tempLog4j = File.createTempFile("log4j-", ".xml");
            FileOutputStream fOut = new FileOutputStream(tempLog4j)
            IOUtils.copy(getClasspathResourceInputStream("logging/$log4jFilename"), fOut);
            fOut.flush(); fOut.close();
//            System.out.println("Configuring log4j logger with ${tempLog4j.canonicalPath}..."); System.out.flush();
            LogManager.resetConfiguration();
            DOMConfigurator.configure(tempLog4j.canonicalPath);
        }
        if( opts.logFilePath && opts.logFilePath.trim().length() > 0 ){
            updateOutputAppender(opts.logFilePath);
        }
        logger.debug("Successfully configured log4j.")
    }//end initialize()

    static void updateOutputAppender( String filePath ){
        // For non-error messages, they should all go to filePath instead of stdout
        // For error messages, they should go to stderr and filePath

        File file = new File(filePath);
        if( file.exists() )
            file.delete();
        else if( file.parentFile && !file.parentFile.exists() ){
            if( !file.parentFile.mkdirs() )
                throw new Exception("Could not create directory for logs: @|yellow $file.parentFile|@")
        }

        org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
        rootLogger.removeAppender("stdout");
        JANSIPatternLayout layout = new JANSIPatternLayout("%d [%5p] %c - %m%n"); // TODO Put this on command line?
        FileAppender fileAppender = new FileAppender(layout, file.canonicalPath, false /* don't append, overwrite */);
                // true /* buffer */, 2048 /* buffer size */);
        fileAppender.setThreshold(Level.ALL);
        rootLogger.addAppender(fileAppender);

    }//end updateAppender()


    private static InputStream getClasspathResourceInputStream(String name){
        InputStream resource = Log4jInitializer.classLoader.getResourceAsStream(name);
        if( !resource ){
            resource = Log4jInitializer.classLoader.getResourceAsStream("/"+name);
        }
        if( !resource ){
            resource = ClassLoader.getSystemResourceAsStream(name);
        }
        if( !resource ){
            resource = ClassLoader.getSystemResourceAsStream("/"+name);
        }
        if( !resource ){
            throw new Exception("Cannot find classpath resource: "+name);
        }
        return resource;
    }//end getClasspathResourceInputStream()



}//end Log4jInitializer()
