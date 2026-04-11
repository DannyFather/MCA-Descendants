package net.dannyfather.mca_descendants;

import net.dannyfather.mca_descendants.network.ClientInteractionManager;
import net.dannyfather.mca_descendants.network.ClientInteractionManagerImpl;

public abstract class ClientProxyAbstractImpl extends ClientProxy.Impl {

    private final ClientInteractionManager networkHandler = new ClientInteractionManagerImpl();

    @Override
    public final ClientInteractionManager getNetworkHandler() {
        return networkHandler;
    }
}