package dev.kalan.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class KalanProfileManager {
    private static final Path PROFILE_DIR = FabricLoader.getInstance().getGameDir()
            .resolve("Kalan").resolve("config").resolve("cfg");
    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_-]{1,32}");
    private static boolean initialized;

    private KalanProfileManager() {}

    public static void initialize() {
        if (initialized) return;
        try {
            Files.createDirectories(PROFILE_DIR);
        } catch (IOException e) {
            KalanCore.LOGGER.error("Could not create Kalan profile directory", e);
        }
        ClientSendMessageEvents.ALLOW_CHAT.register(KalanProfileManager::onChatMessage);
        initialized = true;
    }


    public static String[] getSuggestions(String input) {
        String value = input == null ? "" : input.toLowerCase(Locale.ROOT);
        if (".".equals(value)) {
            return new String[]{
                    ".cfg — управление конфигами",
                    ".cfg save <название> — сохранить конфиг",
                    ".cfg load <название> — загрузить конфиг",
                    ".cfg list — показать список конфигов",
                    ".cfg delete <название> — удалить конфиг"
            };
        }
        if (".c".equals(value) || ".cf".equals(value) || ".cfg".equals(value)) {
            return new String[]{
                    ".cfg save <название>",
                    ".cfg load <название>",
                    ".cfg list",
                    ".cfg delete <название>"
            };
        }
        if (value.startsWith(".cfg ")) {
            String rest = value.substring(5).trim();
            if (rest.isEmpty()) {
                return new String[]{
                        "save <название> — сохранить",
                        "load <название> — загрузить",
                        "list — список конфигов",
                        "delete <название> — удалить"
                };
            }
            if ("save".startsWith(rest) || rest.startsWith("save ")) {
                return new String[]{"Использование: .cfg save <название>"};
            }
            if ("load".startsWith(rest) || rest.startsWith("load ")) {
                return new String[]{"Использование: .cfg load <название>"};
            }
            if ("list".startsWith(rest)) {
                return new String[]{"Использование: .cfg list"};
            }
            if ("delete".startsWith(rest) || rest.startsWith("delete ")) {
                return new String[]{"Использование: .cfg delete <название>"};
            }
        }
        return new String[0];
    }

    private static boolean onChatMessage(String message) {
        if (message == null) return true;
        String trimmed = message.trim();
        if (!trimmed.equalsIgnoreCase(".cfg") &&
                !trimmed.toLowerCase(Locale.ROOT).startsWith(".cfg ")) {
            return true;
        }

        handleCommand(trimmed);
        return false;
    }

    private static void handleCommand(String input) {
        String[] parts = input.split("\\s+", 3);
        if (parts.length == 1) {
            help();
            return;
        }

        String action = parts[1].toLowerCase(Locale.ROOT);
        String name = parts.length >= 3 ? parts[2].trim() : "";

        switch (action) {
            case "save" -> {
                if (!validName(name)) {
                    message("Использование: .cfg save название (латиница, цифры, _ или -, до 32 символов)");
                    return;
                }
                saveProfile(name);
            }
            case "load" -> {
                if (!validName(name)) {
                    message("Использование: .cfg load название");
                    return;
                }
                loadProfile(name);
            }
            case "list" -> listProfiles();
            case "delete" -> {
                if (!validName(name)) {
                    message("Использование: .cfg delete название");
                    return;
                }
                deleteProfile(name);
            }
            default -> help();
        }
    }

    private static boolean validName(String name) {
        return name != null && VALID_NAME.matcher(name).matches();
    }

    private static Path profilePath(String name) {
        return PROFILE_DIR.resolve(name + ".cfg").normalize();
    }

    private static void saveProfile(String name) {
        try {
            Files.createDirectories(PROFILE_DIR);
            JsonObject root = new JsonObject();
            root.addProperty("format", "KalanClientConfig");
            root.addProperty("version", 1);
            root.add("settings", JsonParser.parseString(KalanConfig.exportProfileSettings()));
            root.add("hudLayout", JsonParser.parseString(KalanHudLayoutStore.exportJson()));
            Files.writeString(profilePath(name), new com.google.gson.GsonBuilder()
                    .setPrettyPrinting().create().toJson(root));
            message("Конфиг §a" + name + "§f сохранён.");
        } catch (Exception e) {
            KalanCore.LOGGER.error("Failed to save Kalan profile: " + name, e);
            message("Не удалось сохранить конфиг. Проверь лог.");
        }
    }

    private static void loadProfile(String name) {
        Path file = profilePath(name);
        if (!Files.isRegularFile(file)) {
            message("Конфиг §c" + name + "§f не найден в Kalan/config/cfg.");
            return;
        }

        try {
            JsonObject root = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            if (!root.has("settings") || !root.get("settings").isJsonObject()) {
                message("Файл не похож на конфиг Kalan Client.");
                return;
            }
            if (!KalanConfig.importProfileSettings(root.get("settings").toString())) {
                message("Не удалось прочитать настройки конфига.");
                return;
            }

            if (root.has("hudLayout") && root.get("hudLayout").isJsonArray()) {
                KalanHudLayoutStore.importJson(root.get("hudLayout").toString());
            }

            KalanCustomSky.reloadFromConfig();
            KalanChinaHat.reloadFromConfig();
            message("Конфиг §a" + name + "§f загружен! Настройки применены.");
        } catch (Exception e) {
            KalanCore.LOGGER.error("Failed to load Kalan profile: " + name, e);
            message("Ошибка загрузки конфига. Файл повреждён или имеет неверный формат.");
        }
    }

    private static void listProfiles() {
        try (Stream<Path> files = Files.list(PROFILE_DIR)) {
            List<String> names = files
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(file -> file.toLowerCase(Locale.ROOT).endsWith(".cfg"))
                    .map(file -> file.substring(0, file.length() - 4))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();

            if (names.isEmpty()) {
                message("Пока нет конфигов. Создай первый: .cfg save название");
                return;
            }
            message("Доступные конфиги: §d" + String.join("§7, §d", names));
        } catch (IOException e) {
            KalanCore.LOGGER.error("Failed to list Kalan profiles", e);
            message("Не удалось прочитать папку конфигов.");
        }
    }

    private static void deleteProfile(String name) {
        try {
            if (Files.deleteIfExists(profilePath(name))) {
                message("Конфиг §c" + name + "§f удалён.");
            } else {
                message("Конфиг §c" + name + "§f не найден.");
            }
        } catch (IOException e) {
            KalanCore.LOGGER.error("Failed to delete Kalan profile: " + name, e);
            message("Не удалось удалить конфиг.");
        }
    }

    private static void help() {
        message("Команды: §d.cfg save название §7| §d.cfg load название §7| §d.cfg list §7| §d.cfg delete название");
    }

    private static void message(String text) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.inGameHud != null) {
            client.inGameHud.getChatHud().addMessage(Text.literal("§5[Kalan] §f" + text));
        }
    }
}
