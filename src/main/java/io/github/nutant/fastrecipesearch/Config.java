package io.github.nutant.fastrecipesearch;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.neoforged.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

public class Config implements IMixinConfigPlugin {

    public static final String MODID = "fastrecipesearch";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static final boolean isEnable;
    public static final OptimizeMode optimizeTypeMode;
    public static final Set<String> optimizeTypeWhitelist;
    public static final Set<String> optimizeTypeBlacklist;
    public static final OptimizeMode recipeClassMode;
    public static final Set<String> recipeClassWhitelist;
    public static final Set<String> recipeClassBlacklist;

    public enum OptimizeMode {
        ALL,
        VANILLA,
        WHITELIST,
        BLACKLIST
    }

    private static final File configFile = new File(FMLLoader.getCurrent().getGameDir().toFile(), "config/fast_recipe_search.toml");
    private static final File legacyFile = new File(FMLLoader.getCurrent().getGameDir().toFile(), "config/fast_recipe_search.properties");

    static {
        File configDir = configFile.getParentFile();
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        boolean enable = true;
        OptimizeMode typeMode = OptimizeMode.VANILLA;
        Set<String> typeWhite = Set.of();
        Set<String> typeBlack = Set.of();
        OptimizeMode classMode = OptimizeMode.VANILLA;
        Set<String> classWhite = Set.of();
        Set<String> classBlack = Set.of();
        try (CommentedFileConfig toml = CommentedFileConfig.builder(configFile)
                .preserveInsertionOrder()
                .sync()
                .writingMode(WritingMode.REPLACE)
                .build()) {
            boolean migrated = false;
            if (configFile.exists()) {
                try {
                    toml.load();
                } catch (Throwable e) {
                    LOGGER.error("Failed to load config file {}, using defaults", configFile, e);
                }
            } else if (legacyFile.exists()) {
                migrated = migrateProperties(toml);
            }

            boolean changed = applyDefaults(toml);
            enable = toml.getOrElse("enable", true);
            typeMode = parseMode(toml.getOrElse("optimize_type.mode", "vanilla"));
            typeWhite = getList(toml, "optimize_type.whitelist", true);
            typeBlack = getList(toml, "optimize_type.blacklist", true);
            classMode = parseMode(toml.getOrElse("recipe_class.mode", "vanilla"));
            classWhite = getList(toml, "recipe_class.whitelist", false);
            classBlack = getList(toml, "recipe_class.blacklist", false);

            if (changed || migrated || !configFile.exists()) {
                toml.save();
            }
            if (migrated && configFile.exists() && !legacyFile.delete()) {
                LOGGER.warn("Migrated config but failed to delete {}", legacyFile);
            }
        } catch (Throwable e) {
            LOGGER.error("Failed to read config file {}, using defaults", configFile, e);
        }
        isEnable = enable;
        optimizeTypeMode = typeMode;
        optimizeTypeWhitelist = typeWhite;
        optimizeTypeBlacklist = typeBlack;
        recipeClassMode = classMode;
        recipeClassWhitelist = classWhite;
        recipeClassBlacklist = classBlack;
    }

    private static boolean applyDefaults(CommentedFileConfig toml) {
        boolean changed = false;
        changed |= setDefault(toml, "enable", true, """
                Master switch for this mod's optimizations.
                When disabled, the mod functions as a library without game modifications.""");
        changed |= setDefault(toml, "optimize_type.mode", "vanilla", """
                Scope of recipe types to optimize: all | vanilla | whitelist | blacklist
                all: Optimize all recipe types
                vanilla: Only optimize vanilla recipe types (crafting table, furnace, etc.)
                whitelist: Only optimize the recipe types listed in optimize_type.whitelist
                blacklist: Optimize all recipe types except those listed in optimize_type.blacklist""");
        changed |= setDefault(toml, "optimize_type.whitelist", List.of(), "Recipe type ids, e.g. \"minecraft:crafting\"");
        changed |= setDefault(toml, "optimize_type.blacklist", List.of(), "Recipe type ids, e.g. \"some_mod:custom_type\"");
        changed |= setDefault(toml, "recipe_class.mode", "vanilla", """
                Scope of recipe classes to index: all | vanilla | whitelist | blacklist
                Vanilla classes (net.minecraft.world.item.crafting.*) are never filtered unless blacklisted.""");
        changed |= setDefault(toml, "recipe_class.whitelist", List.of(), "Class names, or package prefixes ending with '.'");
        changed |= setDefault(toml, "recipe_class.blacklist", List.of(), "Class names, or package prefixes ending with '.'");
        return changed;
    }

    private static boolean setDefault(CommentedFileConfig toml, String path, Object value, String comment) {
        toml.setComment(path, " " + comment.replace("\n", "\n# "));
        if (toml.get(path) == null) {
            toml.set(path, value);
            return true;
        }
        return false;
    }

    private static boolean migrateProperties(CommentedFileConfig toml) {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(legacyFile)) {
            props.load(in);
        } catch (Throwable e) {
            LOGGER.error("Failed to migrate legacy config file {}, using defaults", legacyFile, e);
            return false;
        }
        LOGGER.info("Migrating {} to {}", legacyFile.getName(), configFile.getName());
        setIfPresent(toml, "enable", parseBool(props.getProperty("enable")));
        String typeMode = firstPresent(props, "optimize_type_mode", "optimize_mode");
        if (typeMode == null && props.getProperty("optimize_only_vanilla") != null) {
            typeMode = Boolean.parseBoolean(props.getProperty("optimize_only_vanilla")) ? "vanilla" : "all";
        }
        setIfPresent(toml, "optimize_type.mode", typeMode);
        setIfPresent(toml, "optimize_type.whitelist", splitList(firstPresent(props, "optimize_type_whitelist", "optimize_whitelist"), true));
        setIfPresent(toml, "optimize_type.blacklist", splitList(firstPresent(props, "optimize_type_blacklist", "optimize_blacklist"), true));
        setIfPresent(toml, "recipe_class.mode", props.getProperty("recipe_class_mode"));
        setIfPresent(toml, "recipe_class.whitelist", splitList(props.getProperty("recipe_class_whitelist"), false));
        setIfPresent(toml, "recipe_class.blacklist", splitList(props.getProperty("recipe_class_blacklist"), false));
        return true;
    }

    private static void setIfPresent(CommentedFileConfig toml, String path, Object value) {
        if (value != null) {
            toml.set(path, value);
        }
    }

    private static Boolean parseBool(String s) {
        return s == null ? null : Boolean.parseBoolean(s);
    }

    private static String firstPresent(Properties props, String key, String fallbackKey) {
        String value = props.getProperty(key);
        return value != null ? value : props.getProperty(fallbackKey);
    }

    private static List<String> splitList(String s, boolean lowercase) {
        if (s == null) return null;
        return List.copyOf(parseList(s, lowercase));
    }

    private static Set<String> getList(CommentedConfig cfg, String path, boolean lowercase) {
        Object value = cfg.get(path);
        if (value instanceof Collection<?> collection) {
            Set<String> set = new HashSet<>();
            for (Object part : collection) {
                if (part == null) continue;
                String trimmed = part.toString().trim();
                if (lowercase) {
                    trimmed = trimmed.toLowerCase(Locale.ROOT);
                }
                if (!trimmed.isEmpty()) {
                    set.add(trimmed);
                }
            }
            return Set.copyOf(set);
        }
        if (value instanceof String s) {
            return parseList(s, lowercase);
        }
        return Set.of();
    }

    private static OptimizeMode parseMode(String s) {
        if (s == null) return OptimizeMode.VANILLA;
        return switch (s.trim().toLowerCase(Locale.ROOT)) {
            case "all" -> OptimizeMode.ALL;
            case "vanilla" -> OptimizeMode.VANILLA;
            case "whitelist" -> OptimizeMode.WHITELIST;
            case "blacklist" -> OptimizeMode.BLACKLIST;
            default -> OptimizeMode.VANILLA;
        };
    }

    private static Set<String> parseList(String s, boolean lowercase) {
        if (s == null || s.isBlank()) {
            return Set.of();
        }
        Set<String> set = new HashSet<>();
        for (String part : s.split(",")) {
            String trimmed = part.trim();
            if (lowercase) {
                trimmed = trimmed.toLowerCase(Locale.ROOT);
            }
            if (!trimmed.isEmpty()) {
                set.add(trimmed);
            }
        }
        return Set.copyOf(set);
    }

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return isEnable;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
