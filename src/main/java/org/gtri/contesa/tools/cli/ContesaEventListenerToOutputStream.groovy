package org.gtri.contesa.tools.cli

import org.apache.commons.io.IOUtils
import org.gtri.contesa.rules.BusinessRule
import org.gtri.contesa.rules.RuleContext
import org.gtri.contesa.rules.RuleResult
import org.gtri.contesa.tools.model.ValidationObject
import org.gtri.contesa.tools.services.ValidationObjectProvider

/**
 * Immediately writes everything to the outputStream specified, or to /dev/null if not specified.
 */
class ContesaEventListenerToOutputStream implements ContesaEventListener {

    protected OutputStream outputStream;
    protected LogEntryFormatter formatter;

    private ContesaEventListenerToOutputStream(){}
    public ContesaEventListenerToOutputStream(OutputStream out, LogEntryFormatter formatter){
        this.outputStream = out;
        this.formatter = formatter;
    }
    //==================================================================================================================
    //  Implementation of event listeners.
    //==================================================================================================================
    @Override
    void initStart() {
        LogEntry le = new LogEntry();
        le.type = "INIT_START";
        this.write(le);
    }

    @Override
    void initComplete(long startupTime) {
        LogEntry le = new LogEntry();
        le.type = "INIT_COMPLETE";
        le.addData("time", ""+startupTime);
        this.write(le);
    }

    @Override
    public void ruleContextLoaded( RuleContext context ){
        LogEntry le = new LogEntry();
        le.type = "LOADED_RULE_CONTEXT";
        le.addData("name", context.name);
        le.addData("rule-count", context.getRules()?.size())
        this.write(le);
    }

    @Override
    public void validationObjectProvider(ValidationObjectProvider provider, String mime, File file){
        LogEntry le = new LogEntry();
        le.type = "VALIDATION_OBJECT_PROVIDER";
        le.addData("class", provider.class.name)
        le.addData("mime", mime)
        le.addData("filename", file.name)
        this.write(le);
    }

    @Override
    public void validationObject( ValidationObject validationObject, int voCount){
        LogEntry le = new LogEntry();
        le.type = "VALIDATION_OBJECT";
        le.addData("class", validationObject.class.name)
        le.addData("name", validationObject.name)
        le.addData("voCount", voCount)
        this.write(le);
    }

    @Override
    public void matchedContext( RuleContext context, ValidationObject obj ){
        LogEntry le = new LogEntry();
        le.type = "MATCHED_CONTEXT";
        le.addData("context", context.name)
        this.write(le);
    }
    @Override
    public void unmatchedContext( RuleContext context, ValidationObject obj ){
        LogEntry le = new LogEntry();
        le.type = "UNMATCHED_CONTEXT";
        le.addData("context", context.name)
        this.write(le);
    }

    @Override
    public void executionStart(){
        LogEntry le = new LogEntry();
        le.type = "EXECUTION_START";
        this.write(le);
    }
    @Override
    public void startContext(RuleContext context){
        LogEntry le = new LogEntry();
        le.type = "CONTEXT_START";
        le.addData("context", context.name);
        this.write(le);
    }
    @Override
    public void startRule(RuleContext context, BusinessRule rule){
        LogEntry le = new LogEntry();
        le.type = "RULE_START";
        le.addData("context", context.name);
        le.addData("rule", rule.name);
        this.write(le);
    }
    @Override
    public void ruleResult(RuleContext context, BusinessRule rule, RuleResult result){
        LogEntry le = new LogEntry();
        le.type = "RULE_RESULT";
        le.addData("context", context.name);
        le.addData("rule", rule.name);
        le.addData("status", result.status.toString());
        this.write(le);
    }
    @Override
    public void stopRule(RuleContext context, BusinessRule rule){
        LogEntry le = new LogEntry();
        le.type = "RULE_STOP";
        le.addData("context", context.name);
        le.addData("rule", rule.name);
        this.write(le);
    }
    @Override
    public void executionUpdate(int lastExecutedRuleIndex, int totalRuleCount, String overallStatus){
        LogEntry le = new LogEntry();
        le.type = "EXECUTION_UPDATE";
        le.addData("lastExecutedRuleIndex", lastExecutedRuleIndex);
        le.addData("totalRuleCount", totalRuleCount);
        le.addData("overallStatus", overallStatus);
        le.addData("percentComplete", (int) (((double) lastExecutedRuleIndex/ (double)totalRuleCount) * 100.0d)  );
        this.write(le);
    }
    @Override
    public void stopContext(RuleContext context){
        LogEntry le = new LogEntry();
        le.type = "CONTEXT_STOP";
        le.addData("context", context.name);
        this.write(le);
    }
    @Override
    public void executionStop(List<RuleResult> results){
        LogEntry le = new LogEntry();
        le.type = "EXECUTION_STOP";
        le.addData("resultCount", results.size());
        this.write(le);
    }

    @Override
    public void reportGenerationStart(){
        LogEntry le = new LogEntry();
        le.type = "REPORT_START";
        this.write(le);
    }
    @Override
    public void reportGenerationStop(){
        LogEntry le = new LogEntry();
        le.type = "REPORT_STOP";
        this.write(le);
    }

    @Override
    public void validationComplete(){
        LogEntry le = new LogEntry();
        le.type = "VALIDATION_STOP";
        this.write(le);
        this.outputStream.flush();
        this.outputStream.close();
    }


    //==================================================================================================================
    //  Helper Methods
    //==================================================================================================================
    private void write(LogEntry le){
        IOUtils.copy(new StringReader(this.formatter.format(le)+"\n"), this.outputStream);
        outputStream.flush()
    }

}//end ContesaEventListenerToLogFile()
