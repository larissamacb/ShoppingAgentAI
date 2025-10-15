package desastre;

import jadex.bridge.service.types.cms.IComponentDescription;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.ISpaceAction;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.Grid2D;
import jadex.extension.envsupport.environment.space2d.Space2D;
import jadex.extension.envsupport.math.IVector2;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RescueCivilianAction implements ISpaceAction {

    public Object perform(Map<String, Object> parameters, IEnvironmentSpace space) {
        Grid2D grid = (Grid2D) space;
        IComponentDescription actorDesc = (IComponentDescription) parameters.get(ISpaceAction.ACTOR_ID);
        if (actorDesc == null) return null;

        ISpaceObject agentAvatar = grid.getAvatar(actorDesc);
        if (agentAvatar == null) return null;

        IVector2 agentPos = (IVector2) agentAvatar.getProperty(Space2D.PROPERTY_POSITION);

        ISpaceObject[] civilArray = grid.getSpaceObjectsByType("civil");
        if(civilArray == null) return null;

        Optional<ISpaceObject> civilianOptional = Arrays.stream(civilArray)
                .filter(civilian -> agentPos.equals(civilian.getProperty(Space2D.PROPERTY_POSITION)))
                .findFirst();

        if (civilianOptional.isPresent()) {
            if (agentAvatar.getProperty("carrying") == null) {
                ISpaceObject civilian = civilianOptional.get();

                agentAvatar.setProperty("carrying", civilian);
                space.destroySpaceObject(civilian.getId());

                System.out.println("Paramédico " + actorDesc.getName() + " resgatou um civil em " + agentPos);
            }
        }
        return null;
    }

    public Object getId() {
        return "resgatar_civil";
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