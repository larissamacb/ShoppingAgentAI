package desastre;

import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.GoalCreationCondition;
import jadex.bdiv3.annotation.GoalTargetCondition;
import jadex.bdiv3.annotation.RawEvent;
import jadex.bdiv3.runtime.ChangeEvent;
import jadex.commons.SUtil;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.Space2D;
import jadex.extension.envsupport.math.IVector1;
import jadex.extension.envsupport.math.IVector2;
import jadex.micro.annotation.Agent;

import java.util.ArrayList;
import java.util.List;

@Agent
public class ParamedicAgent extends BaseAgent {

    @Belief(dynamic=true)
    protected List<ISpaceObject> civis = new ArrayList<>();

    @Belief(dynamic=true)
    protected List<ISpaceObject> hospitais = new ArrayList<>();

    @Goal(unique=true)
    public static class Rescue {

        protected ISpaceObject targetCivilian;
        protected double priority;

        public Rescue(ISpaceObject targetCivilian, double priority) {
            this.targetCivilian = targetCivilian;
            this.priority = priority;
        }

        @GoalCreationCondition(rawevents=@RawEvent(value=ChangeEvent.FACTADDED, second="civis"))
        public static Rescue checkCreate(ParamedicAgent agent, ISpaceObject newCivilian) {
            IVector2 civilianPos = (IVector2) newCivilian.getProperty(Space2D.PROPERTY_POSITION);
            if (civilianPos == null) return null;

            IVector1 distanceVec = (IVector1)agent.getPosition().getDistance(civilianPos);
            double distance = distanceVec.getAsDouble();

            if (distance < 1) distance = 1;

            double priority = 1.0 / distance;

            System.out.println("Paramédico " + agent.getAgent().getId().getName() + " calculou prioridade " + String.format("%.2f", priority) + " para o civil em " + civilianPos);
            return new Rescue(newCivilian, priority);
        }

        @GoalTargetCondition
        public static boolean checkTarget(Rescue currentGoal, Rescue newGoal) {
            if (newGoal.priority > currentGoal.priority) {
                System.out.println("!!! PARAMÉDICO MUDA DE FOCO: Novo civil é mais prioritário (" + String.format("%.2f", newGoal.priority) + " > " + String.format("%.2f", currentGoal.priority) + ")");
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return targetCivilian != null ? targetCivilian.hashCode() : 0;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Rescue that = (Rescue) obj;
            return SUtil.equals(this.targetCivilian, that.targetCivilian);
        }
    }
}