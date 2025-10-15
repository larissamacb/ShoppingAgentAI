package desastre;

import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.PlanAPI;
import jadex.bdiv3.annotation.PlanBody;
import jadex.bdiv3.annotation.PlanCapability;
import jadex.bdiv3.annotation.PlanReason;
import jadex.bdiv3.runtime.IPlan;
import jadex.extension.envsupport.environment.ISpaceAction;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.Space2D;
import jadex.extension.envsupport.math.IVector2;

import java.util.HashMap;
import java.util.Map;

@Plan
public class ExtinguishFirePlan {

    @PlanCapability
    protected FirefighterAgent agent;

    @PlanAPI
    protected IPlan rplan;

    @PlanReason
    protected FirefighterAgent.Extinguish goal;

    @PlanBody
    public void body() {
        ISpaceObject targetFire = goal.targetFire;

        while (agent.incendios.contains(targetFire)) {
            IVector2 firePosition = (IVector2) targetFire.getProperty(Space2D.PROPERTY_POSITION);

            rplan.dispatchSubgoal(new GoGoal(firePosition)).get();

            Map<String, Object> params = new HashMap<>();
            params.put(ISpaceAction.ACTOR_ID, agent.getAgent().getDescription());
            agent.getEnvironment().performSpaceAction("apagar_incendio", params);

            rplan.waitFor(500).get();
        }

        System.out.println("Plano para apagar fogo em " + targetFire.getProperty(Space2D.PROPERTY_POSITION) + " terminado.");
    }
}