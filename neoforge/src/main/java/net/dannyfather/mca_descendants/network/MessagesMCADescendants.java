package net.dannyfather.mca_descendants.network;

import net.dannyfather.mca_descendants.network.c2s.getDescendantsRequest;

public interface MessagesMCADescendants {
    static void register(ModNetwork.Registrar c) {
        c.register(getDescendantsRequest.TYPE, getDescendantsRequest.STREAM_CODEC, true);
    }
}
