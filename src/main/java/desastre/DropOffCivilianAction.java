package desastre;

import jadex.bridge.IComponentIdentifier;
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

public class DropOffCivilianAction implements ISpaceAction {

    public Object perform(Map<String, Object> parameters, IEnvironmentSpace space) {
        Grid2D grid = (Grid2D) space;

        IComponentDescription actorDesc = (IComponentDescription) parameters.get(ISpaceAction.ACTOR_ID);
        if (actorDesc == null) return null;

        ISpaceObject agentAvatar = grid.getAvatar(actorDesc);
        if (agentAvatar == null) return null;

        IVector2 agentPos = (IVector2) agentAvatar.getProperty(Space2D.PROPERTY_POSITION);

        ISpaceObject[] hospitalArray = grid.getSpaceObjectsByType("hospital");
        if(hospitalArray == null) return null;

        Collection<ISpaceObject> allHospitals = Arrays.asList(hospitalArray);

        boolean isAtHospital = allHospitals.stream()
                .anyMatch(hospital -> agentPos.equals(hospital.getProperty(Space2D.PROPERTY_POSITION)));

        if (isAtHospital && agentAvatar.getProperty("carrying") != null) {
            agentAvatar.setProperty("carrying", null);
            System.out.println("Paramédico " + actorDesc.getName() + " deixou o civil em segurança no hospital em " + agentPos);
        }

        return null;
    }

    public Object getId() {
        return "deixar_civil";
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