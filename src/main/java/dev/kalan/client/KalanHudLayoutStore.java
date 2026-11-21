package dev.kalan.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class KalanHudLayoutStore {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Type LIST_TYPE =
            new TypeToken<ArrayList<KalanHudLayoutEntry>>() {}.getType();

    private static final Path FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("kalan-hud-layout.json");

    private KalanHudLayoutStore() {
    }

    public static void save() {
        List<KalanHudLayoutEntry> entries = new ArrayList<>();

        for (KalanHudElement element : KalanHudRegistry.getAll()) {
            entries.add(new KalanHudLayoutEntry(element));
        }

        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(entries));
        } catch (IOException e) {
            KalanCore.LOGGER.error("Failed to save HUD layout", e);
        }
    }

    public static void load() {
        if (!Files.exists(FILE)) {
            return;
        }

        try {
            String json = Files.readString(FILE);
            List<KalanHudLayoutEntry> entries = GSON.fromJson(json, LIST_TYPE);

            if (entries == null) {
                return;
            }

            for (KalanHudLayoutEntry entry : entries) {
                if (entry == null || entry.id == null) {
                    continue;
                }

                KalanHudElement element = KalanHudRegistry.find(entry.id);

                if (element != null) {
                    entry.applyTo(element);
                }
            }
        } catch (Exception e) {
            KalanCore.LOGGER.error("Failed to load HUD layout", e);
        }
    }
}
