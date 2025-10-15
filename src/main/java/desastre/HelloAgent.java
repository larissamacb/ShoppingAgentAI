package desastre;

import jadex.bdiv3.BDIAgentFactory;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;

@Agent(type = BDIAgentFactory.TYPE)
public class HelloAgent {
    @AgentBody
    public void body() {
        System.out.println("\n\n-----> FINALMENTE! O AGENTE FUNCIONOU! <-----\n\n");
    }
}