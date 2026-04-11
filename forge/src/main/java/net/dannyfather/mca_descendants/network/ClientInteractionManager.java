package net.dannyfather.mca_descendants.network;

import net.dannyfather.mca_descendants.network.s2c.OpenGuiRequest;
import net.dannyfather.mca_descendants.network.s2c.getDescendantResponse;

public interface ClientInteractionManager {
    void handleGuiRequest(OpenGuiRequest message);

    void handleDescendantDataResponse(getDescendantResponse message);
}