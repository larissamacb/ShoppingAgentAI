package desastre;

import jadex.base.IPlatformConfiguration;
import jadex.base.PlatformConfigurationHandler;
import jadex.base.Starter;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.types.cms.CreationInfo;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando teste final...");
        IPlatformConfiguration config = PlatformConfigurationHandler.getDefault();
        config.setGui(true);
        IExternalAccess platform = Starter.createPlatform(config).get();
        CreationInfo ci = new CreationInfo();
        ci.setFilename("desastre/DisasterResponse.application.xml"); // Apontando para o novo arquivo
        platform.createComponent(ci).get();
    }
}