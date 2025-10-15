package desastre;

import jadex.bridge.service.types.cms.IComponentDescription;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.ISpaceAction;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.Grid2D;
import jadex.extension.envsupport.environment.space2d.Space2D;
import jadex.extension.envsupport.math.IVector2;
import jadex.extension.envsupport.math.Vector2Int;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class GoAction implements ISpaceAction {

    public Object perform(Map<String, Object> parameters, IEnvironmentSpace space) {
        Grid2D grid = (Grid2D) space;

        IComponentDescription actorDesc = (IComponentDescription) parameters.get(ISpaceAction.ACTOR_ID);
        if (actorDesc == null) return null;

        ISpaceObject agentAvatar = grid.getAvatar(actorDesc);

        if (agentAvatar != null) {
            IVector2 agentPos = (IVector2) agentAvatar.getProperty(Space2D.PROPERTY_POSITION);
            IVector2 destination = (IVector2) parameters.get("destination");

            if (destination != null && !agentPos.equals(destination)) {
                IVector2 fullDirection = destination.subtract(agentPos);

                int stepX = (int)Math.signum(fullDirection.getXAsInteger());
                int stepY = (int)Math.signum(fullDirection.getYAsInteger());
                IVector2 stepDirection = new Vector2Int(stepX, stepY);

                if(stepDirection.getXAsInteger() != 0 || stepDirection.getYAsInteger() != 0) {
                    IVector2 newPos = agentPos.add(stepDirection);

                    Object[] allObjects = grid.getSpaceObjects();
                    boolean isOccupied = false;
                    if (allObjects != null) {
                        isOccupied = Arrays.stream(allObjects)
                                .anyMatch(obj -> newPos.equals(((ISpaceObject)obj).getProperty(Space2D.PROPERTY_POSITION)));
                    }

                    if(!isOccupied) {
                        agentAvatar.setProperty(Space2D.PROPERTY_POSITION, newPos);
                    }
                }
            }
        }
        return null;
    }

    public Object getId() {
        return "go";
    }

    public Object getProperty(String name) {
        return null;
    }

    public void setProperty(String name, Object value) {
    }

    public Map<String, Object> getProperties() {
        return null;
    }

    public boolean hasProperty(String name) {
        return false;
    }

    public Set<String> getPropertyNames() {
        return null;
    }
}