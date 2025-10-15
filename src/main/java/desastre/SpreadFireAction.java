package desastre;

import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.ISpaceAction;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.Grid2D;
import jadex.extension.envsupport.environment.space2d.Space2D;
import jadex.extension.envsupport.math.IVector2;
import jadex.extension.envsupport.math.Vector2Int;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SpreadFireAction implements ISpaceAction {

    private Random random = new Random();

    public Object perform(Map<String, Object> parameters, IEnvironmentSpace space) {
        Grid2D grid = (Grid2D) space;
        ISpaceObject[] fires = grid.getSpaceObjectsByType("incendio");

        if (fires == null || fires.length == 0) {
            return null;
        }

        IVector2 dimension = (IVector2)grid.getProperty("dimension");
        int width = dimension.getXAsInteger();
        int height = dimension.getYAsInteger();

        List<ISpaceObject> newFires = new ArrayList<>();

        for (ISpaceObject fire : fires) {
            IVector2 firePos = (IVector2) fire.getProperty(Space2D.PROPERTY_POSITION);
            Integer intensity = (Integer) fire.getProperty("intensidade");

            if (intensity == null || intensity <= 0 || firePos == null) {
                continue;
            }

            if (random.nextDouble() < intensity / 200.0) {
                List<IVector2> neighborCandidates = new ArrayList<>();
                neighborCandidates.add(firePos.add(new Vector2Int(0, 1)));
                neighborCandidates.add(firePos.add(new Vector2Int(0, -1)));
                neighborCandidates.add(firePos.add(new Vector2Int(1, 0)));
                neighborCandidates.add(firePos.add(new Vector2Int(-1, 0)));

                List<IVector2> validNeighbors = new ArrayList<>();
                for(IVector2 pos : neighborCandidates) {
                    boolean isInArea = pos.getXAsInteger() >= 0 && pos.getXAsInteger() < width
                            && pos.getYAsInteger() >= 0 && pos.getYAsInteger() < height;
                    if(isInArea) {
                        validNeighbors.add(pos);
                    }
                }

                if (!validNeighbors.isEmpty()) {
                    IVector2 spreadPos = validNeighbors.get(random.nextInt(validNeighbors.size()));

                    Object[] allObjects = grid.getSpaceObjects();
                    boolean alreadyBurning = false;
                    if (allObjects != null) {
                        alreadyBurning = Arrays.stream(allObjects)
                                .anyMatch(obj -> {
                                    ISpaceObject spaceObj = (ISpaceObject)obj;
                                    return "incendio".equals(spaceObj.getProperty("type")) && spreadPos.equals(spaceObj.getProperty(Space2D.PROPERTY_POSITION));
                                });
                    }

                    if (!alreadyBurning) {
                        Map<String, Object> props = new HashMap<>();
                        props.put("type", "incendio");
                        props.put(Space2D.PROPERTY_POSITION, spreadPos);
                        props.put("intensidade", intensity / 2);
                        ISpaceObject newFire = space.createSpaceObject("incendio", props, null);
                        if(newFire != null) {
                            newFires.add(newFire);
                        }
                    }
                }
            }
        }

        if (!newFires.isEmpty()) {
            System.out.println("Fogo se espalhou para " + newFires.size() + " novas localizações.");
        }

        return null;
    }

    public Object getId() {
        return "espalhar_fogo";
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