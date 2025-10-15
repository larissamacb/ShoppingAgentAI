package desastre;

import jadex.bdiv3.annotation.Goal;
import jadex.extension.envsupport.math.IVector2;

@Goal
public class GoGoal {

    protected IVector2 destination;

    public GoGoal(IVector2 destination) {
        this.destination = destination;
    }

    public IVector2 getDestination() {
        return destination;
    }
}