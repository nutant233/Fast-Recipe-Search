package fast.fastrecipesearch;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Set;

/**
 * Resolves the configured optimize mode into the concrete set of recipe types
 * that take the fast path. Unlike Config, this class runs at recipe load time,
 * when the registries are already populated, so it may reference Minecraft classes.
 */
public final class RecipeTypeFilter {

    private RecipeTypeFilter() {
    }

    /**
     * @return recipe types that take the fast path, or null when all types are optimized
     */
    public static Set<RecipeType<?>> optimizedTypes() {
        return switch (Config.optimizeMode) {
            case ALL -> null;
            case VANILLA -> Fastrecipesearch.VANILLA_TYPES;
            case WHITELIST -> resolve(Config.optimizeWhitelist);
            case BLACKLIST -> resolveComplement(Config.optimizeBlacklist);
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
                    Config.LOGGER.warn("Unknown recipe type '{}' in config, ignoring", id);
                }
            } else {
                Config.LOGGER.warn("Invalid recipe type id '{}' in config, ignoring", id);
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
}
