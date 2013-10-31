package org.gtri.contesa.tools.cli

import gtri.logging.Logger
import gtri.logging.LoggerFactory
import org.apache.commons.io.IOUtils
import org.gtri.contesa.ServiceManager
import org.gtri.contesa.rules.RuleContext
import org.gtri.contesa.rules.RuleContextManager
import org.gtri.contesa.tools.model.NextRuleExecutionResult
import org.gtri.contesa.tools.model.ValidationObject
import org.gtri.contesa.tools.model.ValidationStatus
import org.gtri.contesa.tools.reporting.Report
import org.gtri.contesa.tools.reporting.ReportFormat
import org.gtri.contesa.tools.reporting.ReportGenerator
import org.gtri.contesa.tools.reporting.model.ReportModel
import org.gtri.contesa.tools.services.ContesaAbstractionService
import org.gtri.contesa.tools.services.FileService
import org.gtri.contesa.tools.services.RuleExecutionStatus
import org.gtri.contesa.tools.services.ValidationObjectProvider

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
            Log4jInitializer.initialize(CONTESA_OPTIONS);

            long startInit = System.currentTimeMillis();
            logger.debug("Initializing ConTesA System...");
            ServiceManager serviceManager = ServiceManager.getInstance();
            long stopInit = System.currentTimeMillis();
            logger.debug("Initialization complete in @|green ${stopInit - startInit}|@ms.")

            logger.debug("Validating loaded contexts...")
            RuleContextManager rcm = serviceManager.getBean(RuleContextManager.class);
            List<RuleContext> contexts = rcm.getManagedContexts();
            if( contexts.size() < 1 )
                throw new Exception("No rulesets are loaded into ConTesA.  Please place the rule jars into the classpath.")
            contexts.each {ctx ->
                logger.debug("Loaded RuleContext: @|cyan ${ctx.name}|@")
            }

            logger.debug("Parsing validation object...");
            FileService fileService = serviceManager.getBean(FileService)
            String mimeType = fileService.getMimeType(CONTESA_OPTIONS.instanceFile);
            logger.debug("Mime type parsed as: @|cyan ${mimeType}|@, resolving @|green ValidationObjectProvider|@...")
            List<ValidationObjectProvider> providers = serviceManager.getBeans(ValidationObjectProvider.class);
            ValidationObjectProvider provider = null;
            providers.each{ ValidationObjectProvider currentProvider ->
                if( !provider && currentProvider.accepts(mimeType) ){
                    provider = currentProvider;
                }
            }
            if( !provider )
                throw new Exception("Unknown MimeType[${mimeType}], ConTesA cannot validate this.")

            logger.debug("Successfully resolved mime[@|cyan ${mimeType}|@] provider: @|green ${provider.class.name}|@")
            List<ValidationObject> validationObjects = provider.resolve(CONTESA_OPTIONS.instanceFile, CONTESA_OPTIONS.instanceFile.name, mimeType);
            if( validationObjects.size() > 1 )
                throw new Exception("This tool does not handle multiple validation objects.")
            else if( validationObjects.size() < 1 )
                throw new Exception("Could not parse ValidationObject from file: @|yellow ${CONTESA_OPTIONS.instanceFile}|@")

            ValidationObject validationObject = validationObjects.get(0);

            logger.debug("Resolving @|cyan ContesaAbstractionService|@...")
            ContesaAbstractionService abstractionService = ServiceManager.getInstance().getBean(ContesaAbstractionService.class);
            if( abstractionService == null )
                throw new Exception("Could not load ConTesA correctly, missing @|yellow abstraction service|@.")

            logger.debug("Resolving contexts for @|cyan ${validationObject}|@...")
            List<RuleContext> unmatchedContexts = []
            List<RuleContext> matchedContexts = abstractionService.resolveContexts(validationObject);
            if( contexts.size() != matchedContexts.size() && CONTESA_OPTIONS.errorOnNoMatch ){
                logger.debug("Some contexts were not matched!");
                contexts.each{ context ->
                    if( !matchedContexts.contains(context) ){
                        unmatchedContexts.add(context);
                    }
                }

            }

            logger.debug("Executing rules...")
            while( !abstractionService.isFinishedExecuting(validationObject) ){
                NextRuleExecutionResult nextResult = abstractionService.executeNext(validationObject);
                if( nextResult ){
                    logger.debug("Successfully executed Rule[@|green ${nextResult.businessRule.name}|@] from Context[@|blue ${nextResult.ruleContext.name}|@].  Produced @|cyan ${nextResult.results.size()}|@ results.")
                    // TODO What do we do with this?
                }
            }

            logger.debug("Generating ReportModel...")
            ValidationStatus status = abstractionService.getValidationStatus(validationObject);
            ReportModel reportModel = status.getReportModel();

            // TODO Add in missing contexts as errors.


            logger.debug("Finding report generator...")
            List<ReportGenerator> reportGenerators = serviceManager.getBeans(ReportGenerator.class);
            ReportGenerator xmlReportGenerator = null;
            reportGenerators.each { generator ->
                if( !xmlReportGenerator && generator.reportFormat == ReportFormat.XML ){
                    xmlReportGenerator = generator;
                }
            }
            if( !xmlReportGenerator )
                throw new Exception("Unable to find XML ReportGenerator instance.  Conformance Report cannot be generated.")

            Report report = xmlReportGenerator.generate(reportModel);
            if( CONTESA_OPTIONS.outFilePath && CONTESA_OPTIONS.outFilePath.trim().length() > 0 ){
                FileOutputStream fOut = new FileOutputStream(CONTESA_OPTIONS.outFilePath, false);
                IOUtils.copy(report.inputStream, fOut);
                fOut.flush();
                fOut.close();
            }else{
                System.out.println(new String(report.getInputStream().bytes));
                System.out.flush();
            }

            logger.debug("Validation completed successfully.")
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