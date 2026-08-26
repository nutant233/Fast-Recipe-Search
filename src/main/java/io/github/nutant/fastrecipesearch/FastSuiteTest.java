package io.github.nutant.fastrecipesearch;


import dev.shadowsoffire.fastsuite.FastSuite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.registries.ForgeRegistries;

// Code from https://github.com/Shadows-of-Fire/FastSuite/blob/1.20/src/main/java/dev/shadowsoffire/fastsuite/FastSuite.java
public class FastSuiteTest {

    // When testing FastSuite, it is necessary to disable ServerResourcesMixin
    public static boolean TEST_SELF = true;

    FastSuiteTest() {
        if (TEST_SELF) {
            MinecraftForge.EVENT_BUS.addListener(this::test);
        } else {
            FastSuite.DEBUG = true;
        }
    }

    private static class TestMenu extends AbstractContainerMenu {

        protected TestMenu() {
            super(null, -1);
        }

        @Override
        public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player pPlayer) {
            return true;
        }

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void test(ServerStartedEvent e) {
        Config.LOGGER.info("FastSuite Debug Recipe Counts:");
        for (RecipeType type : ForgeRegistries.RECIPE_TYPES.getValues()) {
            Config.LOGGER.info("{}: {}", ForgeRegistries.RECIPE_TYPES.getKey(type), e.getServer().getRecipeManager().getAllRecipesFor(type).size());
        }

        Config.LOGGER.info("Initiating FastSuite Tests...");
        RecipeManager mgr = (RecipeManager) e.getServer().getRecipeManager();
        CraftingContainer inv = new TransientCraftingContainer(new TestMenu(), 2, 2);
        Level world = e.getServer().getLevel(Level.OVERWORLD);
        inv.setItem(0, new ItemStack(Items.ACACIA_LOG));

        CraftingContainer inv2 = new TransientCraftingContainer(new TestMenu(), 2, 2);
        inv2.setItem(0, new ItemStack(Items.BIRCH_PLANKS));
        inv2.setItem(2, new ItemStack(Items.BIRCH_PLANKS));

        CraftingContainer inv3 = new TransientCraftingContainer(new TestMenu(), 2, 2);
        for (int i = 0; i < 4; i++)
            inv3.setItem(i, new ItemStack(Items.OAK_PLANKS));

        CraftingContainer inv4 = new TransientCraftingContainer(new TestMenu(), 2, 2);
        inv4.setItem(0, new ItemStack(Items.SHULKER_BOX));
        inv4.setItem(3, new ItemStack(Items.BLACK_DYE));

        CraftingContainer inv5 = new TransientCraftingContainer(new TestMenu(), 3, 3);
        inv5.setItem(0, new ItemStack(Items.STICK));
        inv5.setItem(1, new ItemStack(Items.STICKY_PISTON));
        inv5.setItem(2, new ItemStack(Items.ACACIA_FENCE));
        inv5.setItem(3, new ItemStack(Items.ACACIA_LEAVES));
        inv5.setItem(4, new ItemStack(Items.APPLE));
        inv5.setItem(5, new ItemStack(Items.BEEHIVE));
        inv5.setItem(6, new ItemStack(Items.BEE_NEST));
        inv5.setItem(7, new ItemStack(Items.BLACK_DYE));
        inv5.setItem(8, new ItemStack(Items.SHULKER_BOX));

        CraftingContainer[] arr = {inv, inv2, inv3, inv4, inv5};
        String[] names = {"acacia planks", "sticks", "crafting table", "black shulker box", "failed match"};

        for (int testCase = 0; testCase < names.length; testCase++) {
            this.testFast(mgr, world, arr[testCase], names[testCase]);
            this.testDefault(mgr, world, arr[testCase], names[testCase]);
        }
    }

    private void testFast(RecipeManager mgr, Level level, CraftingContainer input, String recipeName) {
        long time, time2;
        long deltaSum = 0;
        int iterations = 10000;
        for (int i = 0; i < iterations; i++) {
            time = System.nanoTime();
            mgr.getRecipeFor(RecipeType.CRAFTING, input, level);
            time2 = System.nanoTime();
            deltaSum += time2 - time;
        }
        Config.LOGGER.info("[Fast Test] - Took an average of {} ns to find the recipe for {}", deltaSum / (float) iterations, recipeName);
    }

    private void testDefault(RecipeManager mgr, Level level, CraftingContainer input, String recipeName) {
        long time, time2;
        long deltaSum = 0;
        int iterations = 10000;
        for (int i = 0; i < iterations; i++) {
            time = System.nanoTime();
            mgr.super_getRecipeFor(RecipeType.CRAFTING, input, level);
            time2 = System.nanoTime();
            deltaSum += time2 - time;
        }
        Config.LOGGER.info("[Default Test] - Took an average of {} ns to find the recipe for {}", deltaSum / (float) iterations, recipeName);
    }

}
