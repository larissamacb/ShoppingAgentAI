package desastre;

import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.PlanAPI;
import jadex.bdiv3.annotation.PlanBody;
import jadex.bdiv3.annotation.PlanCapability;
import jadex.bdiv3.annotation.PlanReason;
import jadex.bdiv3.runtime.IPlan;
import jadex.extension.envsupport.environment.ISpaceAction;

import java.util.HashMap;
import java.util.Map;

@Plan
public class GoPlanEnv {

    @PlanCapability
    protected BaseAgent agent;

    @PlanAPI
    protected IPlan rplan;

    @PlanReason
    protected GoGoal goal;

    @PlanBody
    public void body() {
        while (!agent.getPosition().equals(goal.getDestination())) {
            Map<String, Object> params = new HashMap<>();
            params.put("destination", goal.getDestination());
            params.put(ISpaceAction.ACTOR_ID, agent.getAgent().getDescription());

            agent.getEnvironment().performSpaceAction("go", params);

            rplan.waitFor(100).get();
        }
    }
}