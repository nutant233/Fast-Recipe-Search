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

    public static final boolean ingredient_sync;
    public static final boolean ingredient_deduplicator;

    private static final File configFile = new File(FMLLoader.getGamePath().toFile(), "config/fast_recipe_search.properties");

    static {
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
                sync = Boolean.parseBoolean(props.getProperty("ingredient_sync", "false"));
                deduplicator = Boolean.parseBoolean(props.getProperty("ingredient_deduplicator", "false"));
            } catch (IOException e) {
                sync = false;
                deduplicator = false;
            }
        } else {
            sync = false;
            deduplicator = false;
            props.setProperty("ingredient_sync", "false");
            props.setProperty("ingredient_deduplicator", "false");
            try (OutputStream out = new FileOutputStream(configFile)) {
                props.store(out, null);
            } catch (IOException ignored) {
            }
        }
        ingredient_sync = sync;
        ingredient_deduplicator = deduplicator;
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
        if (ingredient_sync) return true;
        if (mixinClassName.equals("fast.fastrecipesearch.mixin.sync.IngredientMixin")) {
            LOGGER.info("ingredient_sync is false, disabling sync mixin");
            return false;
        } else if (mixinClassName.contains("fast.fastrecipesearch.mixin.deduplicator")) {
            LOGGER.info("ingredient_deduplicator is false, disabling deduplicator mixin");
            return false;
        }
        return true;
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
