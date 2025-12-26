## Compatibility
This mod is compatible with any mod that uses the original recipe manager for searching recipes. It is completely incompatible with Recipe Essentials (or FastSuite), as they optimize the same part of the system. According to my test results, installing this mod is sufficient—it offers better optimization and compatibility. Recipe Essentials relies on caching (which generally performs poorly), while FastSuite uses parallel processing (distributing performance consumption across multiple threads and potentially introducing compatibility issues).
Regarding Client Crafting, there is no conflict, but it first searches on the client side and then waits for the server to send search results. Since this mod significantly optimizes search speed on the server side, Client Crafting becomes less meaningful. Additionally, this mod only optimizes the server-side portion, leaving the client-side recipe manager using the original method. This could result in a negative optimization state where the client is still searching while the server has already completed its search.

中文：本模组兼容所有使用原版配方管理器进行配方搜索的模组。它与Recipe Essentials（或FastSuite）完全无法兼容，因为它们优化的是同一模块。根据我的测试结果，安装本模组即可实现最优效果——它在提供更佳优化性能的同时保证了兼容性。Recipe Essentials依赖缓存机制（通常表现不佳），而FastSuite采用并行处理（将性能消耗分散到多个线程，且可能引发兼容性问题）。
关于Client Crafting，两者并无冲突，但客户端会先在本地搜索配方，随后仍需等待服务器返回搜索结果。由于本模组对服务端搜索速度进行了显著优化，客户端合成的实际意义便随之减弱。此外，本模组仅优化了服务端部分，客户端配方管理器仍采用原版搜索方式。这可能导致一种负优化状态：当客户端仍在进行本地搜索时，服务器早已完成搜索响应。

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
### Fabric:

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

### Neoforge:

public class Test {

    static {
        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, e -> test(e.getServer()));
    }

    private static class TestHandler extends AbstractContainerMenu {

        protected TestHandler() {
            super(null, -1);
        }


        @Override
        public net.minecraft.world.item.ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player p_38874_) {
            return true;
        }
    }

    private static class CraftingInventory extends TransientCraftingContainer {

        public CraftingInventory(AbstractContainerMenu p_287684_, int p_287629_, int p_287593_) {
            super(p_287684_, p_287629_, p_287593_);
        }

        public void setStack(int p_38941_, net.minecraft.world.item.ItemStack p_38942_) {
            this.setItem(p_38941_, p_38942_);
        }
    }

    public static void test(MinecraftServer server) {
        if (server == null) return;
        LOGGER.info("Initiating Tests...");
        RecipeManager mgr = (RecipeManager) server.getRecipeManager();
        var world = server.getLevel(Level.OVERWORLD);
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
                testFast(mgr, world, arr[testCase].asCraftInput(), names[testCase]);
                testDefault(mgr, world, arr[testCase].asCraftInput(), names[testCase]);
            }
        }
    }

    private static void testFast(RecipeManager mgr, Level level, CraftingInput input, String recipeName) {
        long time, time2;
        long deltaSum = 0;
        int iterations = 1000;
        for (int i = 0; i < iterations; i++) {
            time = System.nanoTime();
            mgr.getRecipeFor(RecipeType.CRAFTING, input, level);
            time2 = System.nanoTime();
            deltaSum += time2 - time;
        }
        LOGGER.info("[Fast Test] - Took an average of {} ns to find the recipe for {}", deltaSum / (float) iterations, recipeName);
    }

    private static void testDefault(RecipeManager mgr, Level level, CraftingInput input, String recipeName) {
        long time, time2;
        long deltaSum = 0;
        int iterations = 1000;
        for (int i = 0; i < iterations; i++) {
            time = System.nanoTime();
            mgr.super_getFirstMatch(RecipeType.CRAFTING, input, level);
            time2 = System.nanoTime();
            deltaSum += time2 - time;
        }
        LOGGER.info("[Default Test] - Took an average of {} ns to find the recipe for {}", deltaSum / (float) iterations, recipeName);
    }
}

}

