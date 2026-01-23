package fast.fastrecipesearch;

import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class Config implements IMixinConfigPlugin {

    public static final String MODID = "fastrecipesearch";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static final boolean isEnable;
    public static final boolean ingredient_sync;
    public static final boolean ingredient_deduplicator;

    private static final File configFile = new File(FMLLoader.getGamePath().toFile(), "config/fast_recipe_search.properties");

    static {
        boolean enable;
        boolean sync;
        boolean deduplicator;
        File configDir = configFile.getParentFile();
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        Properties props = new Properties();
        if (configFile.exists()) {
            try (InputStream in = new FileInputStream(configFile)) {
                props.load(in);
                enable = props.getProperty("enable").equalsIgnoreCase("true");
                sync = props.getProperty("ingredient_sync").equalsIgnoreCase("true");
                deduplicator = props.getProperty("ingredient_deduplicator").equalsIgnoreCase("true");
            } catch (Throwable e) {
                enable = false;
                sync = false;
                deduplicator = false;
                set(props);
            }
        } else {
            enable = false;
            sync = false;
            deduplicator = false;
            set(props);
        }
        isEnable = enable;
        ingredient_sync = sync;
        ingredient_deduplicator = deduplicator;
    }

    private static void set(Properties props) {
        props.setProperty("enable", "false");
        props.setProperty("ingredient_sync", "false");
        props.setProperty("ingredient_deduplicator", "false");
        try (OutputStream out = new FileOutputStream(configFile)) {
            String comments = """
                    # Mod Optimization Configuration
                    # enable: Master switch for this mod's optimizations
                    #   When enabled, activates optimization features
                    #   When disabled, mod functions as a library without game modifications
                    # ingredient_deduplicator: Removes duplicate objects to significantly reduce memory usage and slightly improve loading speed
                    #   Note: May be incompatible with some mods
                    # ingredient_sync: Optimizes synchronization to improve client-side search performance
                    #   Note: May be incompatible with some mods""";
            props.store(out, comments);
        } catch (IOException ignored) {
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
        if (isEnable) {
            if (!ingredient_sync && mixinClassName.equals("fast.fastrecipesearch.mixin.sync.IngredientMixin")) {
                LOGGER.info("ingredient_sync is false, disabling sync mixin");
                return false;
            }
            if (!ingredient_deduplicator && mixinClassName.contains("fast.fastrecipesearch.mixin.deduplicator")) {
                LOGGER.info("ingredient_deduplicator is false, disabling deduplicator mixin");
                return false;
            }
            return true;
        } else {
            return false;
        }
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
