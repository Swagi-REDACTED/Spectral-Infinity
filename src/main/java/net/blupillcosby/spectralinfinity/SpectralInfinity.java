package net.blupillcosby.spectralinfinity;

import net.blupillcosby.spectralinfinity.config.ModConfig;
import net.blupillcosby.spectralinfinity.network.ConfigSavePayload;
import net.blupillcosby.spectralinfinity.network.ConfigSyncPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.players.NameAndId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpectralInfinity implements ModInitializer {
    public static final String MOD_ID = "spectral_infinity";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Spectral Infinity initialized!");

        // Register payloads
        PayloadTypeRegistry.clientboundPlay().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ConfigSavePayload.TYPE, ConfigSavePayload.STREAM_CODEC);

        // Handle server-side save requests
        ServerPlayNetworking.registerGlobalReceiver(ConfigSavePayload.TYPE, (payload, context) -> {
            // Check if player is an operator
            if (context.server().getPlayerList().isOp(new NameAndId(context.player().getGameProfile()))) {
                LOGGER.info("Player {} (OP) requested config save.", context.player().getName().getString());
                ModConfig.get().updateFromJson(payload.configJson());
                ModConfig.get().save();

                // Sync new config to ALL players
                String json = ModConfig.get().toJson();
                for (var player : context.server().getPlayerList().getPlayers()) {
                    ServerPlayNetworking.send(player, new ConfigSyncPayload(json));
                }
            } else {
                LOGGER.warn("Player {} (NOT OP) tried to save config! Ignoring.", context.player().getName().getString());
                // Optionally send back the correct config to the offending player
                ServerPlayNetworking.send(context.player(), new ConfigSyncPayload(ModConfig.get().toJson()));
            }
        });

        // Sync config on player join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            sender.sendPacket(new ConfigSyncPayload(ModConfig.get().toJson()));
        });
    }
}
