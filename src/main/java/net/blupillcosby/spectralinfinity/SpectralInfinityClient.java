package net.blupillcosby.spectralinfinity;

import net.blupillcosby.spectralinfinity.config.ModConfig;
import net.blupillcosby.spectralinfinity.network.ConfigSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class SpectralInfinityClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Handle server-to-client config sync
        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                ModConfig.get().updateFromJson(payload.configJson());
                SpectralInfinity.LOGGER.info("Spectral Infinity config synced from server.");
            });
        });
    }
}
