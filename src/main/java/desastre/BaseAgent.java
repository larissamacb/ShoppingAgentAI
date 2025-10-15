package desastre;

import jadex.application.EnvironmentService;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bridge.IInternalAccess;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.Grid2D;
import jadex.extension.envsupport.math.IVector2;
import jadex.micro.annotation.Agent;
import jadex.bridge.service.annotation.OnStart;

@Agent
public class BaseAgent {

    @Agent
    protected IInternalAccess agent;

    protected Grid2D envGrid;
    protected ISpaceObject myself;

    @OnStart
    @SuppressWarnings("deprecation")
    public void init() {
        IEnvironmentSpace space = (IEnvironmentSpace)EnvironmentService.getSpace(agent, "cidade_space").get();

        this.envGrid = (Grid2D) space;

        this.myself = this.envGrid.getAvatar(agent.getDescription());

        System.out.println("BaseAgent " + agent.getId().getName() + " iniciado com sucesso!");

        agent.getFeature(IBDIAgentFeature.class).dispatchTopLevelGoal(new MonitorarAmbienteGoal());
    }

    @Goal(recur = true)
    public class MonitorarAmbienteGoal {}

    @Plan(trigger = @Trigger(goals = MonitorarAmbienteGoal.class))
    public void monitorarPlano() {
        System.out.println("Agente " + agent.getId().getName() + " está executando o plano de monitoramento.");
    }

    public Grid2D getEnvironment() {
        return envGrid;
    }

    public IVector2 getPosition() {
        return (IVector2) myself.getProperty("position");
    }

    public IInternalAccess getAgent() {
        return agent;
    }

    public ISpaceObject getMyself() {
        return myself;
    }
}