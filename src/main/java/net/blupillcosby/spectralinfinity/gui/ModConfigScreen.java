package net.blupillcosby.spectralinfinity.gui;

import io.github.cottonmc.cotton.gui.GuiDescription;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import net.blupillcosby.spectralinfinity.config.ModConfig;
import net.minecraft.client.gui.screens.Screen;

import net.blupillcosby.spectralinfinity.network.ConfigSavePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public class ModConfigScreen extends CottonClientScreen {
    public ModConfigScreen(Screen parent) {
        super(new ModConfigGui());
    }

    @Override
    public void removed() {
        super.removed();
        ModConfig.get().save();

        // If connected to a server, request to save the config on the server as well
        if (Minecraft.getInstance().getConnection() != null) {
            ClientPlayNetworking.send(new ConfigSavePayload(ModConfig.get().toJson()));
        }
    }
}
