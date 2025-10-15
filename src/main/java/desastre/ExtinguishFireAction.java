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
import java.util.Set;
import java.util.Optional;

public class ExtinguishFireAction implements ISpaceAction {

    public Object perform(Map<String, Object> parameters, IEnvironmentSpace space) {
        Grid2D grid = (Grid2D) space;

        IComponentDescription actorDesc = (IComponentDescription) parameters.get(ISpaceAction.ACTOR_ID);
        if (actorDesc == null) return null;

        ISpaceObject agentAvatar = grid.getAvatar(actorDesc);
        if (agentAvatar == null) return null;

        IVector2 agentPos = (IVector2) agentAvatar.getProperty(Space2D.PROPERTY_POSITION);

        ISpaceObject[] fireArray = grid.getSpaceObjectsByType("incendio");
        if(fireArray == null) return null;

        Optional<ISpaceObject> fireOptional = Arrays.stream(fireArray)
                .filter(fire -> agentPos.equals(fire.getProperty(Space2D.PROPERTY_POSITION)))
                .findFirst();

        if (fireOptional.isPresent()) {
            ISpaceObject fire = fireOptional.get();
            Integer currentIntensity = (Integer) fire.getProperty("intensidade");

            if (currentIntensity != null && currentIntensity > 0) {
                int reductionAmount = 25;
                int newIntensity = currentIntensity - reductionAmount;

                if (newIntensity > 0) {
                    fire.setProperty("intensidade", newIntensity);
                    System.out.println("Bombeiro " + actorDesc.getName() + " reduziu a intensidade do fogo em " + agentPos + " para " + newIntensity);
                } else {
                    space.destroySpaceObject(fire.getId());
                    System.out.println("Bombeiro " + actorDesc.getName() + " APAGOU o fogo em " + agentPos);
                }
            }
        } else {
            System.out.println("Bombeiro " + actorDesc.getName() + " tentou apagar fogo em " + agentPos + ", mas não havia nenhum.");
        }

        return null;
    }

    public Object getId() {
        return "apagar_incendio";
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