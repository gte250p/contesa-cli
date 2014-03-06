package org.gtri.contesa.tools.cli

import org.gtri.contesa.rules.BusinessRule
import org.gtri.contesa.rules.RuleContext
import org.gtri.contesa.rules.RuleResult
import org.gtri.contesa.tools.model.ValidationObject
import org.gtri.contesa.tools.services.ValidationObjectProvider

/**
 * Used to respond to ConTesA Events as execution occurs.
 * <br/><br/>
 * User: brad
 * Date: 10/31/13 1:54 PM
 */
public interface ContesaEventListener {

    public void initStart();
    public void initComplete(long startupTime);

    public void ruleContextLoaded( RuleContext context );

    public void validationObjectProvider(ValidationObjectProvider provider, String mime, File file);

    public void validationObject( ValidationObject validationObject, int voCount);

    public void matchedContext( RuleContext context, ValidationObject obj );
    public void unmatchedContext( RuleContext context, ValidationObject obj );

    public void executionStart();
    public void startContext(RuleContext context);
    public void startRule(RuleContext context, BusinessRule rule);
    public void ruleResult(RuleContext context, BusinessRule rule, RuleResult result);
    public void stopRule(RuleContext context, BusinessRule rule);
    public void executionUpdate(int lastExecutedRuleIndex, int totalRuleCount, String overallStatus);
    public void stopContext(RuleContext context);
    public void executionStop(List<RuleResult> results);

    public void reportGenerationStart();
    public void reportGenerationStop();

    public void validationComplete();

}//end ContesaEventListener
