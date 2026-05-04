package fast.fastrecipesearch;

import net.neoforged.fml.loading.FMLLoader;
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
    public static final boolean optimize_only_vanilla;

    private static final File configFile = new File(FMLLoader.getCurrent().getGameDir().toFile(), "config/fast_recipe_search.properties");

    static {
        boolean enable;
        boolean vanilla;
        File configDir = configFile.getParentFile();
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        Properties props = new Properties();
        if (configFile.exists()) {
            try (InputStream in = new FileInputStream(configFile)) {
                props.load(in);
                enable = props.getProperty("enable").equalsIgnoreCase("true");
                vanilla = props.getProperty("optimize_only_vanilla").equalsIgnoreCase("true");
            } catch (Throwable e) {
                enable = true;
                vanilla = true;
                set(props);
            }
        } else {
            enable = true;
            vanilla = true;
            set(props);
        }
        isEnable = enable;
        optimize_only_vanilla = vanilla;
    }

    private static void set(Properties props) {
        props.setProperty("enable", "true");
        props.setProperty("optimize_only_vanilla", "true");
        try (OutputStream out = new FileOutputStream(configFile)) {
            String comments = """
                    # Mod Optimization Configuration
                    # enable: Master switch for this mod's optimizations
                    #   When enabled, activates optimization features
                    #   When disabled, mod functions as a library without game modifications
                    # optimize_only_vanilla: Only optimize vanilla recipes
                    #   When enabled, only vanilla recipes are optimized
                    #   When disabled, all recipes are optimized""";
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
