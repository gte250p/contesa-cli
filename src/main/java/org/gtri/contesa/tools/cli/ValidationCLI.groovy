package org.gtri.contesa.tools.cli

import gtri.logging.Logger
import gtri.logging.LoggerFactory
import org.apache.commons.io.IOUtils
import org.gtri.contesa.ServiceManager
import org.gtri.contesa.rules.BusinessRule
import org.gtri.contesa.rules.RuleContext
import org.gtri.contesa.rules.RuleContextManager
import org.gtri.contesa.rules.RuleResult
import org.gtri.contesa.rules.RuleResultStatus
import org.gtri.contesa.tools.model.NextRuleExecutionResult
import org.gtri.contesa.tools.model.ValidationObject
import org.gtri.contesa.tools.model.ValidationStatus
import org.gtri.contesa.tools.reporting.Report
import org.gtri.contesa.tools.reporting.ReportFormat
import org.gtri.contesa.tools.reporting.ReportGenerator
import org.gtri.contesa.tools.reporting.model.ReportBusinessRule
import org.gtri.contesa.tools.reporting.model.ReportModel
import org.gtri.contesa.tools.reporting.model.ReportRuleContext
import org.gtri.contesa.tools.reporting.model.ReportRuleResult
import org.gtri.contesa.tools.services.ContesaAbstractionService
import org.gtri.contesa.tools.services.FileService
import org.gtri.contesa.tools.services.RuleExecutionStatus
import org.gtri.contesa.tools.services.ValidationObjectProvider
import org.gtri.contesa.xml.XMLDocument

/**
 * Entry point class for command line validation.
 * <br/><br/>
 * User: brad
 * Date: 10/29/13 4:42 PM
 */
class ValidationCLI {

    public static final Logger logger = LoggerFactory.get(ValidationCLI);

    public static ContesaOpts CONTESA_OPTIONS = null;

    public static ContesaEventListener eventListener = null;

    public static void main(String[] args){
        try{
            CONTESA_OPTIONS = new ContesaOpts(args);
            validateOptions(CONTESA_OPTIONS);
            Log4jInitializer.initialize(CONTESA_OPTIONS);
            setupEventListener();

            long startInit = System.currentTimeMillis();
            logger.debug("Initializing ConTesA Core...");
            eventListener.initStart();
            ServiceManager serviceManager = ServiceManager.getInstance();
            long stopInit = System.currentTimeMillis();
            logger.debug("ConTesA Core Initialization complete in @|green ${stopInit - startInit}|@ms.")
            eventListener.initComplete(stopInit-startInit);

            logger.debug("Validating loaded contexts...")
            RuleContextManager rcm = serviceManager.getBean(RuleContextManager.class);
            List<RuleContext> contexts = rcm.getManagedContexts();
            if( contexts.size() < 1 ){
                logger.warn("No rulesets found.")
                throw new Exception("No rulesets are loaded into ConTesA.  Please place the rule jars into the classpath.")
            }
            contexts.each {ctx ->
                logger.debug("Loaded RuleContext: @|cyan ${ctx.name}|@")
                eventListener.ruleContextLoaded(ctx);
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
            eventListener.validationObjectProvider(provider, mimeType, CONTESA_OPTIONS.instanceFile);

            logger.debug("Successfully resolved mime[@|cyan ${mimeType}|@] provider: @|green ${provider.class.name}|@")
            List<ValidationObject> validationObjects = provider.resolve(CONTESA_OPTIONS.instanceFile, CONTESA_OPTIONS.instanceFile.name, mimeType);
            if( validationObjects.size() > 1 )
                throw new Exception("This tool does not handle multiple validation objects.")
            else if( validationObjects.size() < 1 )
                throw new Exception("Could not parse ValidationObject from file: @|yellow ${CONTESA_OPTIONS.instanceFile}|@")

            if( validationObjects.size() > 1 ){
                logger.warn("This artifact contains @|cyan ${validationObjects.size()}|@ validation objects.  Only the @|yellow first|@ will be processed.")
            }
            ValidationObject validationObject = validationObjects.get(0);
            eventListener.validationObject(validationObject, validationObjects.size())

            logger.debug("Resolving @|cyan ContesaAbstractionService|@...")
            ContesaAbstractionService abstractionService = ServiceManager.getInstance().getBean(ContesaAbstractionService.class);
            if( abstractionService == null )
                throw new Exception("Could not load ConTesA correctly, missing @|yellow abstraction service|@.")


            logger.debug("Resolving contexts for @|cyan ${validationObject}|@...")
            List<RuleContext> unmatchedContexts = []
            List<RuleContext> matchedContexts = abstractionService.resolveContexts(validationObject);
            for( RuleContext context : contexts ){
                if( !matchedContexts.contains(context) ){
                    logger.debug("Did not match context: @|yellow ${context.name}|@")
                    unmatchedContexts.add(context);
                    eventListener.unmatchedContext(context, validationObject);
                }else{
                    eventListener.matchedContext(context, validationObject);
                }
            }

            int totalRuleCount = 0;
            contexts.each{ context ->
                totalRuleCount += context.rules?.size()
            }
            int currentRuleIndex = 0;
            RuleResultStatus overallStatus = RuleResultStatus.SUCCESS;

            List<ReportRuleContext> ranContexts = []
            List<ReportRuleResult> results = []
            eventListener.executionStart();
            XMLDocument document = validationObject.getDocument();
            for( RuleContext context : contexts ){
                eventListener.startContext(context);
                ReportRuleContext reportRuleContext = createReportRuleContext(context);
                ranContexts.add(reportRuleContext);
                for( BusinessRule rule : context.rules ){
                    logger.debug("Executing RuleContext[@|green ${context.name}|@] => BusinessRule[@|cyan ${rule.name}|@]...")
                    ReportBusinessRule reportBusinessRule = findReportBusinessRule(reportRuleContext, rule);
                    eventListener.startRule(context, rule);
                    def themResults = rule.execute(document);
                    eventListener.stopRule(context, rule);
                    List<ReportRuleResult> currentResults = []
                    themResults?.each{ result ->
                        def currentResult = createReportRuleResult(reportRuleContext, reportBusinessRule, result);
                        currentResults.add( currentResult );
                        eventListener.ruleResult(context, rule, result);
                        if( overallStatus.getOrdering() < result.getStatus().getOrdering() ){
                            overallStatus = result.getStatus();
                        }
                    }
                    results.addAll(currentResults);
                    currentRuleIndex++;
                    eventListener.executionUpdate(currentRuleIndex, totalRuleCount, overallStatus.toString());
                }
                eventListener.stopContext(context);
            }
            eventListener.executionStop(results)

            eventListener.reportGenerationStart()
            logger.debug("Generating ReportModel...")
            ReportModel reportModel = new ReportModel();
            reportModel.setOverallStatus(overallStatus.toString());
            reportModel.setMatchedContexts(ranContexts);
            reportModel.setRuleResults(results);
            reportModel.setRulesExecutedCount(currentRuleIndex);
            reportModel.setValidationObjectIdentifier(validationObject.getUniqueIdentifier());
            reportModel.setValidationObjectName(validationObject.getName())
            reportModel.setValidationObjectSize(CONTESA_OPTIONS.instanceFile.length()); // TODO how the hell do I set this?


            logger.debug("Finding report generator...")
            List<ReportGenerator> reportGeneratorsToExecute = []
            List<ReportGenerator> reportGenerators = serviceManager.getBeans(ReportGenerator.class);
            reportGenerators.each { generator ->
                if( CONTESA_OPTIONS.reportFormats.contains(generator.reportFormat) ){
                    reportGeneratorsToExecute.add( generator );
                }
            }
            if( reportGeneratorsToExecute.isEmpty() )
                throw new Exception("Unable to find report generators.  Conformance Report cannot be generated.")

            Map<ReportGenerator, Report> reports = [:]
            // Go ahead and execute the reports, storing the values in memory.
            reportGeneratorsToExecute.each{ reportGenerator ->
                logger.debug("Generatring report from ReportGenerator[@|cyan ${reportGenerator.class.name}|@]...")
                Report report = reportGenerator.generate(reportModel);
                reports.put(reportGenerator, report);
            }
            eventListener.reportGenerationStop()


            logger.debug("Writing report(s)..")
            if( CONTESA_OPTIONS.xmlOnlyReport() && !CONTESA_OPTIONS.hasOutFilePath() ){
                // If XML is the only report, and there is no log file, then we can output to stdout (but this is the only case allowing that).
                reports.keySet().each { key ->
                    Report report = reports.get(key);
                    System.out.println(new String(report.getInputStream().bytes));
                    System.out.flush();
                }
            }else{ // There must exist an out file path now (see CONTESA_OPTIONS processing logic - it excludes the other case).
                File outFile = new File(CONTESA_OPTIONS.outFilePath);
                if( outFile.exists() )
                    outFile.delete();

                File parentFile = outFile.parentFile;
                if( parentFile && !parentFile.exists() && !parentFile.mkdirs() ){
                    throw new Exception("Unable to create directory for report file(s): ${parentFile.canonicalPath}")
                }
                reports.keySet().each{ ReportGenerator generator ->
                    Report report = reports.get(generator);
                    logger.debug("Writing report: ${report.getName()}")
                    // TODO Does not handle multiple output files of same type!
                    File reportOutFile = new File(CONTESA_OPTIONS.outFilePath + "." + report.getFormat().toString().toLowerCase());
                    if( reportOutFile.exists() )
                        reportOutFile.delete()

                    FileOutputStream fOut = new FileOutputStream(reportOutFile, false);
                    IOUtils.copy(report.inputStream, fOut);
                    fOut.flush();
                    fOut.close();
                }

            }

            eventListener.validationComplete(); // Must be called last on event listener.
            logger.debug("Validation completed successfully.")
            System.exit(0);
        }catch(Throwable t){
            logger.error(t.getMessage());
            if( !CONTESA_OPTIONS || (CONTESA_OPTIONS && CONTESA_OPTIONS.verbosity >= 1) )
                t.printStackTrace(System.err);
            System.exit(1);
        }
    }//end main()

    private static ReportRuleResult createReportRuleResult(ReportRuleContext reportRuleContext, ReportBusinessRule reportBusinessRule, RuleResult result){
        def currentResult = new ReportRuleResult();
        currentResult.setBusinessRule(reportBusinessRule);
        currentResult.setRuleContext(reportRuleContext);
        currentResult.setCustomData(result.getCustomData());
        currentResult.setFile(result.getFile());
        currentResult.setLineNumber(result.getLineNumber());
        currentResult.setMessage(result.getMessage());
        currentResult.setStatus(result.getStatus().toString());
        currentResult.setUniqueXPath(result.getUniqueXPath());
        return currentResult;
    }

    private static ReportRuleContext createReportRuleContext(RuleContext ruleContext){
        ReportRuleContext reportRuleContext = new ReportRuleContext();
        reportRuleContext.description = ruleContext.description
        reportRuleContext.internalId = ruleContext.name.hashCode()
        reportRuleContext.name = ruleContext.name
        def rules = []
        ruleContext.rules.each{ BusinessRule rule ->
            ReportBusinessRule reportBusinessRule = createReportBusinessRule(rule);
            rules.add(reportBusinessRule);
        }
        reportRuleContext.rules = rules
        logger.debug("Successfully created ReportRuleContext[@|green ${reportRuleContext.name}|@] with @|green ${reportRuleContext.rules.size()}|@ rules")
        return reportRuleContext;
    }

    private static ReportBusinessRule createReportBusinessRule(BusinessRule rule){
        ReportBusinessRule reportBusinessRule = new ReportBusinessRule();
        reportBusinessRule.setDescription(rule.getDescription());
//        logger.debug("Creating rule: @|cyan ${rule.getIdentifier()}|@...")
        reportBusinessRule.setIdentifier(rule.getIdentifier());
        reportBusinessRule.setImplementationNote(rule.getImplementationDetails().getNote());
        reportBusinessRule.setImplementationStatus(rule.getImplementationDetails().getStatus().toString());
        reportBusinessRule.setName(rule.getName());
        return reportBusinessRule;
    }

    private static ReportBusinessRule findReportBusinessRule(ReportRuleContext context, BusinessRule rule){
        ReportBusinessRule reportBusinessRuleToReturn = null;
        logger.debug("Search for rule @|cyan ${rule.identifier}|@ in Context[@|green ${context?.name}|@] => @|green ${context?.rules.size()}|@ rules...")
        context?.rules.each{ ReportBusinessRule reportBusinessRule ->
            if( reportBusinessRule.identifier == rule.getIdentifier() )
                reportBusinessRuleToReturn = reportBusinessRule;
        }
        if( reportBusinessRuleToReturn == null ){
            logger.error("Could not find rule[@|red ${rule?.getIdentifier()}|@] on context: @|yellow ${context?.name}|@")
            throw new RuntimeException("Could not locate rule[${rule?.getIdentifier()}] in ${context?.rules.size()} rules for context[${context?.name}]");
        }
        return reportBusinessRuleToReturn;
    }

    private static void validateOptions(ContesaOpts opts){
        if( opts.instancePath == null || opts.instancePath.trim().length() == 0 ){
            throw new Exception("Missing required instance file to validate.")
        }
    }//end opts()

    private static void setupEventListener(){
        eventListener = new ContesaEventListenerNoOp();
        if( CONTESA_OPTIONS.shouldOutputStatusFile() ){
            logger.debug("Configuring status file[@|cyan ${CONTESA_OPTIONS.statusFilePath}|@]...")
            File logFile = new File(CONTESA_OPTIONS.statusFilePath);
            if( logFile.parentFile && !logFile.parentFile.exists() ){
                if( !logFile.parentFile.mkdirs() ){
                    logger.error("Cannot create log-file directory: @|red ${logFile.parentFile.path}|@")
                    throw new Exception("Cannot create log-file directory: ${logFile.parentFile.path}")
                }
            }
            FileOutputStream fos;
            if( logFile.exists() ){
                // TODO Do we just delete it?  Or should we append to it?
                logFile.delete();
            }
            fos = new FileOutputStream(logFile);
            eventListener = new ContesaEventListenerToOutputStream(fos, CONTESA_OPTIONS.statusEntryFormatter);
        }
    }

}//end ValidationCLI
