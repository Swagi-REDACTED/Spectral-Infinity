package net.blupillcosby.spectralinfinity.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class ModConfig {
    public static final String MOD_ID = "spectral_infinity";
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static ModConfig INSTANCE;

    public MutableBoolean allArrows = new MutableBoolean(false);
    public Set<String> arrowWhitelist = new HashSet<>();

    public ModConfig() {
        arrowWhitelist.add("minecraft:spectral_arrow");
    }

    public static ModConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public static ModConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                return GSON.fromJson(Files.newBufferedReader(CONFIG_PATH), ModConfig.class);
            } catch (IOException e) {
                return new ModConfig();
            }
        }
        return new ModConfig();
    }

    public void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public void updateFromJson(String json) {
        ModConfig other = GSON.fromJson(json, ModConfig.class);
        if (other != null) {
            this.allArrows.setValue(other.allArrows.booleanValue());
            this.arrowWhitelist.clear();
            this.arrowWhitelist.addAll(other.arrowWhitelist);
        }
    }
}
