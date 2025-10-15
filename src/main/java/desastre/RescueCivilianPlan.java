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
public class RescueCivilianPlan {

    @PlanCapability
    protected ParamedicAgent agent;

    @PlanAPI
    protected IPlan rplan;

    @PlanReason
    protected ParamedicAgent.Rescue goal;

    @PlanBody
    public void body() {
        ISpaceObject targetCivilian = goal.targetCivilian;

        if (!agent.civis.contains(targetCivilian)) {
            return;
        }

        IVector2 civilianPosition = (IVector2) targetCivilian.getProperty(Space2D.PROPERTY_POSITION);
        System.out.println("Paramédico " + agent.getAgent().getId().getName() + " a caminho do civil em " + civilianPosition);
        rplan.dispatchSubgoal(new GoGoal(civilianPosition)).get();

        Map<String, Object> rescueParams = new HashMap<>();
        rescueParams.put(ISpaceAction.ACTOR_ID, agent.getAgent().getDescription());
        agent.getEnvironment().performSpaceAction("resgatar_civil", rescueParams);

        if (agent.getMyself().getProperty("carrying") == null) {
            System.out.println("Paramédico " + agent.getAgent().getId().getName() + " falhou em resgatar o civil.");
            return;
        }

        if (agent.hospitais.isEmpty()) {
            System.out.println("Paramédico não sabe onde fica o hospital!");
            return;
        }
        ISpaceObject hospital = agent.hospitais.get(0);
        IVector2 hospitalPosition = (IVector2) hospital.getProperty(Space2D.PROPERTY_POSITION);
        System.out.println("Paramédico " + agent.getAgent().getId().getName() + " levando civil para o hospital em " + hospitalPosition);
        rplan.dispatchSubgoal(new GoGoal(hospitalPosition)).get();

        Map<String, Object> dropOffParams = new HashMap<>();
        dropOffParams.put(ISpaceAction.ACTOR_ID, agent.getAgent().getDescription());
        agent.getEnvironment().performSpaceAction("deixar_civil", dropOffParams);
    }
}