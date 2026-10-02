package net.pixeldreamstudios.combat_styles_content.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.pixeldreamstudios.combat_styles.platform.SpellPowerAccess;
import net.pixeldreamstudios.combat_styles_content.CombatStylesContent;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class EquipmentIndex {

    private static final String PATH =
            "/data/" + CombatStylesContent.MOD_ID + "/equipment_index.json";
    private static final String KEY_EQUIPMENT = "equipment";
    private static final String KEY_SPELL = "spell_power_equipment";

    private static List<String> cached;

    private EquipmentIndex() {
    }

    public static List<String> all() {
        if (cached == null) {
            cached = load();
        }
        return cached;
    }

    private static List<String> load() {
        JsonObject root = read();
        if (root == null) {
            return List.of();
        }
        List<String> ids = new ArrayList<>();
        collect(root, KEY_EQUIPMENT, ids);
        if (SpellPowerAccess.isLoaded()) {
            collect(root, KEY_SPELL, ids);
        }
        return List.copyOf(ids);
    }

    private static JsonObject read() {
        try (InputStream stream = EquipmentIndex.class.getResourceAsStream(PATH)) {
            if (stream == null) {
                CombatStylesContent.LOGGER.error("Missing {}", PATH);
                return null;
            }
            JsonElement parsed = JsonParser.parseReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8));
            return parsed.isJsonObject() ? parsed.getAsJsonObject() : null;
        } catch (IOException | RuntimeException exception) {
            CombatStylesContent.LOGGER.error("Unable to read {}", PATH, exception);
            return null;
        }
    }

    private static void collect(JsonObject root, String key, List<String> into) {
        if (!root.has(key) || !root.get(key).isJsonArray()) {
            return;
        }
        JsonArray array = root.getAsJsonArray(key);
        for (JsonElement element : array) {
            if (element.isJsonPrimitive()) {
                into.add(element.getAsString());
            }
        }
    }
}
