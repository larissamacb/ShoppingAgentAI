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
public class FirefighterAgent extends BaseAgent {

    @Belief(dynamic=true)
    protected List<ISpaceObject> incendios = new ArrayList<>();

    @Goal(unique=true)
    public static class Extinguish {

        protected ISpaceObject targetFire;
        protected double priority;

        public Extinguish(ISpaceObject targetFire, double priority) {
            this.targetFire = targetFire;
            this.priority = priority;
        }

        @GoalCreationCondition(rawevents=@RawEvent(value=ChangeEvent.FACTADDED, second="incendios"))
        public static Extinguish checkCreate(FirefighterAgent agent, ISpaceObject newFire) {
            IVector2 firePos = (IVector2) newFire.getProperty(Space2D.PROPERTY_POSITION);
            Integer intensity = (Integer) newFire.getProperty("intensidade");
            if (firePos == null || intensity == null) return null;

            IVector1 distanceVec = (IVector1)agent.getPosition().getDistance(firePos);
            double distance = distanceVec.getAsDouble();

            if (distance < 1) distance = 1;

            double priority = intensity / distance;

            System.out.println("Bombeiro " + agent.getAgent().getId().getName() + " calculou prioridade " + String.format("%.2f", priority) + " para o incêndio em " + firePos);
            return new Extinguish(newFire, priority);
        }

        @GoalTargetCondition
        public static boolean checkTarget(Extinguish currentGoal, Extinguish newGoal) {
            if (newGoal.priority > currentGoal.priority) {
                System.out.println("!!! MUDANÇA DE FOCO: Novo incêndio é mais prioritário (" + String.format("%.2f", newGoal.priority) + " > " + String.format("%.2f", currentGoal.priority) + ")");
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return targetFire != null ? targetFire.hashCode() : 0;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Extinguish that = (Extinguish) obj;
            return SUtil.equals(this.targetFire, that.targetFire);
        }
    }
}