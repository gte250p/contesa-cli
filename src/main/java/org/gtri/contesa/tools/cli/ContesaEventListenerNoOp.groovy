package org.gtri.contesa.tools.cli

import org.gtri.contesa.rules.BusinessRule
import org.gtri.contesa.rules.RuleContext
import org.gtri.contesa.rules.RuleResult
import org.gtri.contesa.tools.model.ValidationObject
import org.gtri.contesa.tools.services.ValidationObjectProvider

/**
 * Discards all listener events.  For use when no output is specified.
 */
class ContesaEventListenerNoOp implements ContesaEventListener {

    @Override
    void initStart() {}

    @Override
    void initComplete(long startupTime) {}

    @Override
    public void ruleContextLoaded( RuleContext context ){}

    @Override
    public void validationObjectProvider(ValidationObjectProvider provider, String mime, File file){}

    @Override
    public void validationObject( ValidationObject validationObject, int voCount){}

    @Override
    public void matchedContext( RuleContext context, ValidationObject obj ){}
    @Override
    public void unmatchedContext( RuleContext context, ValidationObject obj ){}

    @Override
    public void executionStart(){}
    @Override
    public void startContext(RuleContext context){}
    @Override
    public void startRule(RuleContext context, BusinessRule rule){}
    @Override
    public void ruleResult(RuleContext context, BusinessRule rule, RuleResult result){}
    @Override
    public void stopRule(RuleContext context, BusinessRule rule){}
    @Override
    public void executionUpdate(int lastExecutedRuleIndex, int totalRuleCount, String overallStatus){}
    @Override
    public void stopContext(RuleContext context){}
    @Override
    public void executionStop(List<RuleResult> results){}

    @Override
    public void reportGenerationStart(){}
    @Override
    public void reportGenerationStop(){}

    @Override
    public void validationComplete(){}


}//end ContesaEventListenerNoOp()
