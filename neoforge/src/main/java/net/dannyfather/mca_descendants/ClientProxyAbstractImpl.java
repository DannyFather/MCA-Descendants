package net.dannyfather.mca_descendants;

import net.dannyfather.mca_descendants.network.ClientHandler;
import net.dannyfather.mca_descendants.network.ClientHandlerImpl;

public abstract class ClientProxyAbstractImpl extends ClientProxy.Impl {
    private final ClientHandler networkHandler = new ClientHandlerImpl();

    @Override
    public ClientHandler getNetworkHandler() {
        return networkHandler;
    }
}
