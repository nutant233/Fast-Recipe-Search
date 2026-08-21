package fast.fastrecipesearch;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

public class Config implements IMixinConfigPlugin {

    public static final String MODID = "fastrecipesearch";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static final boolean isEnable;
    public static final OptimizeMode optimizeMode;
    public static final Set<String> optimizeWhitelist;
    public static final Set<String> optimizeBlacklist;

    public enum OptimizeMode {
        ALL,
        VANILLA,
        WHITELIST,
        BLACKLIST
    }

    private static final File configFile = new File(FMLLoader.getCurrent().getGameDir().toFile(), "config/fast_recipe_search.properties");

    static {
        File configDir = configFile.getParentFile();
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        Properties props = new Properties();
        if (configFile.exists()) {
            try (InputStream in = new FileInputStream(configFile)) {
                props.load(in);
            } catch (Throwable e) {
                LOGGER.error("Failed to load config file {}, using defaults", configFile, e);
            }
        }

        isEnable = getBool(props, "enable", true);
        optimizeWhitelist = parseList(props.getProperty("optimize_whitelist"));
        optimizeBlacklist = parseList(props.getProperty("optimize_blacklist"));
        String mode = props.getProperty("optimize_mode");
        if (mode == null) {
            // Backward compatibility: old configs only have optimize_only_vanilla
            optimizeMode = getBool(props, "optimize_only_vanilla", true) ? OptimizeMode.VANILLA : OptimizeMode.ALL;
        } else {
            optimizeMode = parseMode(mode);
        }

        // Always ensure the file contains the new options, migrating existing configs
        // without touching values the user has already set.
        setDefault(props);
    }

    private static boolean getBool(Properties props, String key, boolean defaultValue) {
        String value = props.getProperty(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    private static OptimizeMode parseMode(String s) {
        return switch (s.trim().toLowerCase(Locale.ROOT)) {
            case "all" -> OptimizeMode.ALL;
            case "vanilla" -> OptimizeMode.VANILLA;
            case "whitelist" -> OptimizeMode.WHITELIST;
            case "blacklist" -> OptimizeMode.BLACKLIST;
            default -> OptimizeMode.VANILLA;
        };
    }

    private static Set<String> parseList(String s) {
        if (s == null || s.isBlank()) {
            return Set.of();
        }
        Set<String> set = new HashSet<>();
        for (String part : s.split(",")) {
            String trimmed = part.trim().toLowerCase(Locale.ROOT);
            if (!trimmed.isEmpty()) {
                set.add(trimmed);
            }
        }
        return Set.copyOf(set);
    }

    /**
     * Resolves the configured optimize mode into the set of recipe types that take the fast path.
     * Returns null when all recipe types are optimized.
     */
    public static Set<RecipeType<?>> resolveMode() {
        return switch (optimizeMode) {
            case ALL -> null;
            case VANILLA -> Fastrecipesearch.VANILLA_TYPES;
            case WHITELIST -> resolve(optimizeWhitelist);
            case BLACKLIST -> resolveComplement(optimizeBlacklist);
        };
    }

    private static Set<RecipeType<?>> resolve(Set<String> ids) {
        var set = new ReferenceOpenHashSet<RecipeType<?>>();
        for (String id : ids) {
            var key = Identifier.tryParse(id);
            if (key != null) {
                var type = BuiltInRegistries.RECIPE_TYPE.getValue(key);
                if (type != null) {
                    set.add(type);
                } else {
                    LOGGER.warn("Unknown recipe type '{}' in config, ignoring", id);
                }
            } else {
                LOGGER.warn("Invalid recipe type id '{}' in config, ignoring", id);
            }
        }
        return set;
    }

    /** Blacklist mode: optimize every registered recipe type except the listed ones. */
    private static Set<RecipeType<?>> resolveComplement(Set<String> blacklist) {
        var excluded = resolve(blacklist);
        var set = new ReferenceOpenHashSet<RecipeType<?>>();
        for (var key : BuiltInRegistries.RECIPE_TYPE.keySet()) {
            var type = BuiltInRegistries.RECIPE_TYPE.getValue(key);
            if (type != null && !excluded.contains(type)) {
                set.add(type);
            }
        }
        return set;
    }

    private static void setDefault(Properties props) {
        boolean changed = false;
        if (props.getProperty("enable") == null) {
            props.setProperty("enable", "true");
            changed = true;
        }
        if (props.getProperty("optimize_mode") == null) {
            // Preserve the intent of a legacy optimize_only_vanilla when migrating an old config
            props.setProperty("optimize_mode", optimizeMode.name().toLowerCase(Locale.ROOT));
            changed = true;
        }
        if (props.getProperty("optimize_whitelist") == null) {
            props.setProperty("optimize_whitelist", "");
            changed = true;
        }
        if (props.getProperty("optimize_blacklist") == null) {
            props.setProperty("optimize_blacklist", "");
            changed = true;
        }
        if (!changed) {
            return;
        }
        try (OutputStream out = new FileOutputStream(configFile)) {
            String comments = """
                    # Mod Optimization Configuration
                    # enable: Master switch for this mod's optimizations
                    #   When enabled, activates optimization features
                    #   When disabled, mod functions as a library without game modifications
                    # optimize_mode: Scope of recipe types to optimize
                    #   all: Optimize all recipe types
                    #   vanilla: Only optimize vanilla recipe types (crafting table, furnace, etc.)
                    #   whitelist: Only optimize the recipe types listed in optimize_whitelist
                    #   blacklist: Optimize all recipe types except those listed in optimize_blacklist
                    # optimize_whitelist: Comma-separated recipe type ids, e.g. minecraft:crafting,minecraft:smelting
                    # optimize_blacklist: Comma-separated recipe type ids, e.g. some_mod:custom_type""";
            props.store(out, comments);
        } catch (IOException e) {
            LOGGER.error("Failed to write default config file {}", configFile, e);
        }
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
