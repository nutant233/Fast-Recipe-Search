## ATM 10 Test

[18Dec2025 12:55:04.510] [Server thread/INFO] [com.mojang.text2speech.Narrator/]: [Fast Test] - Took an average of 135018.2 ns to find the recipe for torch

[18Dec2025 12:55:18.528] [Server thread/INFO] [com.mojang.text2speech.Narrator/]: [Default Test] - Took an average of 1.4016372E7 ns to find the recipe for torch

[18Dec2025 12:55:18.551] [Server thread/INFO] [com.mojang.text2speech.Narrator/]: [Fast Test] - Took an average of 22548.8 ns to find the recipe for workbench

[18Dec2025 12:55:19.809] [Server thread/INFO] [com.mojang.text2speech.Narrator/]: [Default Test] - Took an average of 1257699.5 ns to find the recipe for workbench

[18Dec2025 12:55:19.855] [Server thread/INFO] [com.mojang.text2speech.Narrator/]: [Fast Test] - Took an average of 46480.9 ns to find the recipe for chest

[18Dec2025 12:55:20.145] [Server thread/INFO] [com.mojang.text2speech.Narrator/]: [Default Test] - Took an average of 290051.72 ns to find the recipe for chest

[18Dec2025 12:55:20.154] [Server thread/INFO] [com.mojang.text2speech.Narrator/]: [Fast Test] - Took an average of 8439.9 ns to find the recipe for furnace

[18Dec2025 12:55:20.476] [Server thread/INFO] [com.mojang.text2speech.Narrator/]: [Default Test] - Took an average of 321852.9 ns to find the recipe for furnace

[18Dec2025 12:55:20.617] [Server thread/INFO] [com.mojang.text2speech.Narrator/]: [Fast Test] - Took an average of 140890.3 ns to find the recipe for bed

[18Dec2025 12:55:27.591] [Server thread/INFO] [com.mojang.text2speech.Narrator/]: [Default Test] - Took an average of 6972892.0 ns to find the recipe for bed

[18Dec2025 12:55:27.621] [Server thread/INFO] [com.mojang.text2speech.Narrator/]: [Fast Test] - Took an average of 29388.1 ns to find the recipe for golden apple

[18Dec2025 12:55:30.810] [Server thread/INFO] [com.mojang.text2speech.Narrator/]: [Default Test] - Took an average of 3187964.5 ns to find the recipe for golden apple

[18Dec2025 12:55:30.950] [Server thread/INFO] [com.mojang.text2speech.Narrator/]: [Fast Test] - Took an average of 139620.98 ns to find the recipe for arrow

[18Dec2025 12:55:38.518] [Server thread/INFO] [com.mojang.text2speech.Narrator/]: [Default Test] - Took an average of 7566936.5 ns to find the recipe for arrow

[18Dec2025 12:55:38.926] [Server thread/INFO] [com.mojang.text2speech.Narrator/]: [Fast Test] - Took an average of 408603.4 ns to find the recipe for painting

[18Dec2025 12:55:48.666] [Server thread/INFO] [com.mojang.text2speech.Narrator/]: [Default Test] - Took an average of 9739167.0 ns to find the recipe for painting

[18Dec2025 12:55:48.709] [Server thread/INFO] [com.mojang.text2speech.Narrator/]: [Fast Test] - Took an average of 42464.9 ns to find the recipe for bookshelf

[18Dec2025 12:56:01.420] [Server thread/INFO] [com.mojang.text2speech.Narrator/]: [Default Test] - Took an average of 1.2710804E7 ns to find the recipe for bookshelf

[18Dec2025 12:56:01.547] [Server thread/INFO] [com.mojang.text2speech.Narrator/]: [Fast Test] - Took an average of 126764.8 ns to find the recipe for enchanting table

[18Dec2025 12:56:20.741] [Server thread/INFO] [com.mojang.text2speech.Narrator/]: [Default Test] - Took an average of 1.9193496E7 ns to find the recipe for enchanting table

[18Dec2025 12:56:20.912] [Server thread/INFO] [com.mojang.text2speech.Narrator/]: [Fast Test] - Took an average of 171027.6 ns to find the recipe for failed

<img width="2560" height="1528" alt="image" src="https://github.com/user-attachments/assets/5f08efb8-7194-4d04-b225-05092dc6f0f3" />

## Test code:
package fast.fastrecipesearch;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import static com.mojang.text2speech.Narrator.LOGGER;

public class Test {

    static {
        ServerLifecycleEvents.SERVER_STARTED.register(Test::test);
    }

    private static class TestHandler extends ScreenHandler {

        protected TestHandler() {
            super(null, -1);
        }

        @Override
        public ItemStack quickMove(PlayerEntity player, int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }
    }

    public static void test(MinecraftServer server) {
        if (server == null) return;
        LOGGER.info("Initiating Tests...");
        RecipeManager mgr = (RecipeManager) server.getRecipeManager();
        World world = server.getWorld(World.OVERWORLD);
        while (true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            CraftingInventory inv1 = new CraftingInventory(new TestHandler(), 2, 2);
            inv1.setStack(0, new ItemStack(Items.STICK));
            inv1.setStack(1, new ItemStack(Items.COAL));

            CraftingInventory inv2 = new CraftingInventory(new TestHandler(), 2, 2);
            for (int i = 0; i < 4; i++) inv2.setStack(i, new ItemStack(Items.OAK_PLANKS));

            CraftingInventory inv3 = new CraftingInventory(new TestHandler(), 3, 3);
            for (int i = 0; i < 9; i++) {
                if (i != 4) {
                    inv3.setStack(i, new ItemStack(Items.OAK_PLANKS));
                }
            }

            CraftingInventory inv4 = new CraftingInventory(new TestHandler(), 3, 3);
            for (int i = 0; i < 9; i++) {
                if (i != 4) {
                    inv4.setStack(i, new ItemStack(Items.COBBLESTONE));
                }
            }

            CraftingInventory inv5 = new CraftingInventory(new TestHandler(), 3, 3);
            for (int i = 0; i < 3; i++) {
                inv5.setStack(i, new ItemStack(Items.OAK_PLANKS));
            }
            for (int i = 3; i < 6; i++) {
                inv5.setStack(i, new ItemStack(Items.WHITE_WOOL));
            }

            CraftingInventory inv6 = new CraftingInventory(new TestHandler(), 3, 3);
            inv6.setStack(4, new ItemStack(Items.APPLE));
            for (int i = 0; i < 9; i++) {
                if (i != 4) {
                    inv6.setStack(i, new ItemStack(Items.GOLD_INGOT));
                }
            }

            CraftingInventory inv7 = new CraftingInventory(new TestHandler(), 3, 3);
            inv7.setStack(0, new ItemStack(Items.FLINT));
            inv7.setStack(1, new ItemStack(Items.STICK));
            inv7.setStack(2, new ItemStack(Items.FEATHER));

            CraftingInventory inv8 = new CraftingInventory(new TestHandler(), 3, 3);

            inv8.setStack(0, new ItemStack(Items.STICK));
            inv8.setStack(1, new ItemStack(Items.STICK));
            inv8.setStack(2, new ItemStack(Items.STICK));
            inv8.setStack(3, new ItemStack(Items.STICK));
            inv8.setStack(4, new ItemStack(Items.WHITE_WOOL));
            inv8.setStack(5, new ItemStack(Items.STICK));
            inv8.setStack(6, new ItemStack(Items.STICK));
            inv8.setStack(7, new ItemStack(Items.STICK));
            inv8.setStack(8, new ItemStack(Items.STICK));

            CraftingInventory inv9 = new CraftingInventory(new TestHandler(), 3, 3);
            for (int i = 0; i < 3; i++) {
                inv9.setStack(i, new ItemStack(Items.OAK_PLANKS));
                inv9.setStack(i + 6, new ItemStack(Items.OAK_PLANKS));
            }
            for (int i = 3; i < 6; i++) {
                inv9.setStack(i, new ItemStack(Items.BOOK));
            }

            CraftingInventory inv10 = new CraftingInventory(new TestHandler(), 3, 3);
            inv10.setStack(0, new ItemStack(Items.BOOK));
            inv10.setStack(1, new ItemStack(Items.DIAMOND));
            inv10.setStack(2, new ItemStack(Items.BOOK));
            inv10.setStack(3, new ItemStack(Items.OBSIDIAN));
            inv10.setStack(4, new ItemStack(Items.OBSIDIAN));
            inv10.setStack(5, new ItemStack(Items.OBSIDIAN));
            inv10.setStack(6, new ItemStack(Items.OBSIDIAN));
            inv10.setStack(7, new ItemStack(Items.OBSIDIAN));
            inv10.setStack(8, new ItemStack(Items.OBSIDIAN));

            CraftingInventory inv11 = new CraftingInventory(new TestHandler(), 3, 3);
            inv11.setStack(0, new ItemStack(Items.STICK));
            inv11.setStack(1, new ItemStack(Items.IRON_INGOT));
            inv11.setStack(2, new ItemStack(Items.REDSTONE));
            inv11.setStack(3, new ItemStack(Items.OAK_PLANKS));
            inv11.setStack(4, new ItemStack(Items.OBSIDIAN));
            inv11.setStack(5, new ItemStack(Items.BOOK));
            inv11.setStack(6, new ItemStack(Items.FLINT));
            inv11.setStack(7, new ItemStack(Items.COBBLESTONE));
            inv11.setStack(8, new ItemStack(Items.COAL));

            CraftingInventory[] arr = {inv1, inv2, inv3, inv4, inv5, inv6, inv7, inv8, inv9, inv10, inv11};
            String[] names = {"torch", "workbench", "chest", "furnace", "bed", "golden apple", "arrow", "painting", "bookshelf", "enchanting table", "failed"};

            for (int testCase = 0; testCase < names.length; testCase++) {
                testFast(mgr, world, arr[testCase].createRecipeInput(), names[testCase]);
                testDefault(mgr, world, arr[testCase].createRecipeInput(), names[testCase]);
            }

            LOGGER.info("Starting Furnace Tests...");

            SingleStackRecipeInput furnaceInv1 = new SingleStackRecipeInput(new ItemStack(Items.IRON_ORE));
            SingleStackRecipeInput furnaceInv2 = new SingleStackRecipeInput(new ItemStack(Items.GOLD_ORE));
            SingleStackRecipeInput furnaceInv3 = new SingleStackRecipeInput(new ItemStack(Items.STONE));
            SingleStackRecipeInput furnaceInv4 = new SingleStackRecipeInput(new ItemStack(Items.SAND));
            SingleStackRecipeInput furnaceInv5 = new SingleStackRecipeInput(new ItemStack(Items.RAW_COPPER));
            SingleStackRecipeInput furnaceInv6 = new SingleStackRecipeInput(new ItemStack(Items.POTATO));
            SingleStackRecipeInput furnaceInv7 = new SingleStackRecipeInput(new ItemStack(Items.BEEF));
            SingleStackRecipeInput furnaceInv8 = new SingleStackRecipeInput(new ItemStack(Items.CLAY_BALL));
            SingleStackRecipeInput furnaceInv9 = new SingleStackRecipeInput(new ItemStack(Items.CACTUS));
            SingleStackRecipeInput furnaceInv10 = new SingleStackRecipeInput(new ItemStack(Items.DRAGON_EGG));

            SingleStackRecipeInput[] furnaceArrs = {furnaceInv1, furnaceInv2, furnaceInv3, furnaceInv4, furnaceInv5, furnaceInv6, furnaceInv7, furnaceInv8, furnaceInv9, furnaceInv10};
            String[] furnaceNames = {"iron ingot", "gold ingot", "smooth stone", "glass", "copper ingot", "baked potato", "steak", "brick", "green dye", "failed"};

            for (int i = 0; i < furnaceArrs.length; i++) {
                testFurnaceFast(mgr, world, furnaceArrs[i], furnaceNames[i]);
                testFurnaceDefault(mgr, world, furnaceArrs[i], furnaceNames[i]);
            }
        }
    }

    private static void testFast(RecipeManager mgr, World level, CraftingRecipeInput input, String recipeName) {
        long time, time2;
        long deltaSum = 0;
        int iterations = 10000;
        for (int i = 0; i < iterations; i++) {
            time = System.nanoTime();
            mgr.getFirstMatch(RecipeType.CRAFTING, input, level);
            time2 = System.nanoTime();
            deltaSum += time2 - time;
        }
        LOGGER.info("[Fast Test] - Took an average of {} ns to find the recipe for {}", deltaSum / (float) iterations, recipeName);
    }

    private static void testDefault(RecipeManager mgr, World level, CraftingRecipeInput input, String recipeName) {
        long time, time2;
        long deltaSum = 0;
        int iterations = 10000;
        for (int i = 0; i < iterations; i++) {
            time = System.nanoTime();
            mgr.super_getFirstMatch(RecipeType.CRAFTING, input, level);
            time2 = System.nanoTime();
            deltaSum += time2 - time;
        }
        LOGGER.info("[Default Test] - Took an average of {} ns to find the recipe for {}", deltaSum / (float) iterations, recipeName);
    }

    private static void testFurnaceFast(RecipeManager mgr, World level, SingleStackRecipeInput input, String recipeName) {
        long time, time2;
        long deltaSum = 0;
        int iterations = 10000;
        for (int i = 0; i < iterations; i++) {
            time = System.nanoTime();
            mgr.getFirstMatch(RecipeType.SMELTING, input, level);
            time2 = System.nanoTime();
            deltaSum += time2 - time;
        }
        LOGGER.info("[Furnace Fast Test] - Took an average of {} ns to find the recipe for {}", deltaSum / (float) iterations, recipeName);
    }

    private static void testFurnaceDefault(RecipeManager mgr, World level, SingleStackRecipeInput input, String recipeName) {
        long time, time2;
        long deltaSum = 0;
        int iterations = 10000;
        for (int i = 0; i < iterations; i++) {
            time = System.nanoTime();
            mgr.super_getFirstMatch(RecipeType.SMELTING, input, level);
            time2 = System.nanoTime();
            deltaSum += time2 - time;
        }
        LOGGER.info("[Furnace Default Test] - Took an average of {} ns to find the recipe for {}", deltaSum / (float) iterations, recipeName);
    }
}

