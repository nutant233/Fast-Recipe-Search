This mod dramatically increases recipe lookup speed. Its core mechanism builds a tree index based on the hash codes of recipe ingredients, where ingredients serve as branches and recipes as nodes. When searching, it first extracts the ingredients from the container and starts matching from the root node; if a certain key does not match, it prunes that branch directly and no longer searches it, until a fully matching recipe is found. This greatly reduces the number of recipes that need to be checked. Compared to the vanilla method of comparing recipes one by one, it speeds up by more than 10 times, 和 the performance advantage becomes more significant as the total number of recipes increases.

In addition, the mod's core library provides a high-performance Recipe Tree API that other mods can call. Because it does not depend on MC code, even non-MC projects can use this functionality.

## Optimization scope
Old versions only optimized the server side; the new version optimizes both the client and the server.

## Compatibility
This mod is compatible with all mods that use the vanilla recipe manager for recipe searching. It is completely incompatible with Recipe Essentials (or FastSuite) because they optimize the same module. According to my test results, installing this mod alone achieves the best effect — it provides better optimization performance while ensuring compatibility.

Recipe Essentials relies on a caching mechanism (which often performs poorly), while FastSuite uses parallel processing (distributing the performance cost across multiple threads and potentially causing compatibility issues).

Regarding Client Crafting, there is no conflict; that mod allows the client to search recipes locally first without modifying the recipe search mechanism, and it can be used alongside this mod.

## Configuration
Because some mods implement recipe-related interfaces in a non-standard way — for example, they implement the getIngredients method of the Recipe interface, so ingredients can be extracted and thus are optimized by this mod, but the corresponding Container interface does not implement the getItem method, making it impossible to search for that mod's recipes.

To address this issue, this mod by default only optimizes vanilla recipe types (crafting table, furnace, etc.). If you need better optimization, you can try turning off the "optimize_only_vanilla" option.


## Performance Analysis

### Comparison with Vanilla

#### This Mod
The performance improvements of this mod over vanilla are substantial, with particularly dramatic gains in modpack environments:

In vanilla:
- Recipe searches are 8.36x to 85.95x faster
- Most basic recipes see improvements between 8-40x
- Failed matches are handled 28.11x faster

In ATM9 (heavy modpack):
- The improvements become even more dramatic, ranging from 133.19x to 6942.47x faster
- Basic recipes like sticks and crafting table see improvements of over 5000x
- Even complex recipes like black shulker boxes are found 1742.03x faster

#### FastSuite
FastSuite shows more modest improvements compared to vanilla:

In vanilla:
- Multi-threading is actually slower in most cases
- Single-threaded performance shows improvements of 1.13x to 3.96x
- The parallel processing overhead often outweighs the benefits

In ATM9:
- Multi-threading shows better results, with improvements of 8.72x to 16.17x
- However, these improvements are still significantly lower than This Mod's performance gains
- The performance gap between single and multi-threaded operations is more pronounced in modpack environments

## Test Result

### This mod vanilla

[19:27:59] [Server thread/INFO] [fastrecipesearch/]: [Fast Test] - Took an average of 9130.97 ns to find the recipe for acacia planks
[19:28:00] [Server thread/INFO] [fastrecipesearch/]: [Default Test] - Took an average of 85468.055 ns to find the recipe for acacia planks
**Gap: 8.36x faster**

[19:28:00] [Server thread/INFO] [fastrecipesearch/]: [Fast Test] - Took an average of 1149.77 ns to find the recipe for sticks
[19:28:01] [Server thread/INFO] [fastrecipesearch/]: [Default Test] - Took an average of 98866.69 ns to find the recipe for sticks
**Gap: 85.95x faster**

[19:28:01] [Server thread/INFO] [fastrecipesearch/]: [Fast Test] - Took an average of 989.36 ns to find the recipe for crafting table
[19:28:02] [Server thread/INFO] [fastrecipesearch/]: [Default Test] - Took an average of 39704.348 ns to find the recipe for crafting table
**Gap: 40.14x faster**

[19:28:02] [Server thread/INFO] [fastrecipesearch/]: [Fast Test] - Took an average of 7168.7905 ns to find the recipe for black shulker box
[19:28:03] [Server thread/INFO] [fastrecipesearch/]: [Default Test] - Took an average of 99736.78 ns to find the recipe for black shulker box
**Gap: 13.91x faster**

[19:28:03] [Server thread/INFO] [fastrecipesearch/]: [Fast Test] - Took an average of 3845.24 ns to find the recipe for failed match
[19:28:04] [Server thread/INFO] [fastrecipesearch/]: [Default Test] - Took an average of 108064.26 ns to find the recipe for failed match
**Gap: 28.11x faster**

### FastSuite vanilla

[19:40:58] [Server thread/INFO] [fastsuite/]: [Multithreaded Test] - Took an average of 89221.78 ns to find the recipe for acacia planks
[19:40:59] [Server thread/INFO] [fastsuite/]: [Singlethreaded Test] - Took an average of 22518.31 ns to find the recipe for acacia planks
**Gap: Single-threaded 3.96x faster**

[19:40:59] [Server thread/INFO] [fastsuite/]: [Multithreaded Test] - Took an average of 74224.68 ns to find the recipe for sticks
[19:41:00] [Server thread/INFO] [fastsuite/]: [Singlethreaded Test] - Took an average of 65598.14 ns to find the recipe for sticks
**Gap: Single-threaded 1.13x faster**

[19:41:01] [Server thread/INFO] [fastsuite/]: [Multithreaded Test] - Took an average of 59932.03 ns to find the recipe for crafting table
[19:41:01] [Server thread/INFO] [fastsuite/]: [Singlethreaded Test] - Took an average of 37442.07 ns to find the recipe for crafting table
**Gap: Single-threaded 1.60x faster**

[19:41:02] [Server thread/INFO] [fastsuite/]: [Multithreaded Test] - Took an average of 76964.96 ns to find the recipe for black shulker box
[19:41:03] [Server thread/INFO] [fastsuite/]: [Singlethreaded Test] - Took an average of 91723.984 ns to find the recipe for black shulker box
**Gap: Multi-threaded 1.19x faster**

[19:41:03] [Server thread/INFO] [fastsuite/]: [Multithreaded Test] - Took an average of 69536.41 ns to find the recipe for failed match
[19:41:04] [Server thread/INFO] [fastsuite/]: [Singlethreaded Test] - Took an average of 101547.414 ns to find the recipe for failed match
**Gap: Multi-threaded 1.46x faster**

### This mod ATM9

[20:05:20] [Server thread/INFO] [fastrecipesearch/]: [Fast Test] - Took an average of 36147.832 ns to find the recipe for acacia planks
[20:06:48] [Server thread/INFO] [fastrecipesearch/]: [Default Test] - Took an average of 8826844.0 ns to find the recipe for acacia planks
**Gap: 244.16x faster**

[20:06:48] [Server thread/INFO] [fastrecipesearch/]: [Fast Test] - Took an average of 2543.54 ns to find the recipe for sticks
[20:09:20] [Server thread/INFO] [fastrecipesearch/]: [Default Test] - Took an average of 1.5240665E7 ns to find the recipe for sticks
**Gap: 5992.52x faster**

[20:09:20] [Server thread/INFO] [fastrecipesearch/]: [Fast Test] - Took an average of 1175.53 ns to find the recipe for crafting table
[20:10:42] [Server thread/INFO] [fastrecipesearch/]: [Default Test] - Took an average of 8158714.5 ns to find the recipe for crafting table
**Gap: 6942.47x faster**

[20:10:42] [Server thread/INFO] [fastrecipesearch/]: [Fast Test] - Took an average of 8131.2 ns to find the recipe for black shulker box
[20:13:04] [Server thread/INFO] [fastrecipesearch/]: [Default Test] - Took an average of 1.4163565E7 ns to find the recipe for black shulker box
**Gap: 1742.03x faster**

[20:13:05] [Server thread/INFO] [fastrecipesearch/]: [Fast Test] - Took an average of 128783.62 ns to find the recipe for failed match
[20:15:57] [Server thread/INFO] [fastrecipesearch/]: [Default Test] - Took an average of 1.7153812E7 ns to find the recipe for failed match
**Gap: 133.19x faster**

### FastSuite ATM9

[20:21:46] [Server thread/INFO] [fastsuite/]: [Multithreaded Test] - Took an average of 1177099.6 ns to find the recipe for acacia planks
[20:24:20] [Server thread/INFO] [fastsuite/]: [Singlethreaded Test] - Took an average of 1.5478248E7 ns to find the recipe for acacia planks
**Gap: Multi-threaded 13.15x faster**

[20:24:35] [Server thread/INFO] [fastsuite/]: [Multithreaded Test] - Took an average of 1484677.5 ns to find the recipe for sticks
[20:27:05] [Server thread/INFO] [fastsuite/]: [Singlethreaded Test] - Took an average of 1.4976043E7 ns to find the recipe for sticks
**Gap: Multi-threaded 10.08x faster**

[20:27:13] [Server thread/INFO] [fastsuite/]: [Multithreaded Test] - Took an average of 817227.3 ns to find the recipe for crafting table
[20:29:25] [Server thread/INFO] [fastsuite/]: [Singlethreaded Test] - Took an average of 1.3213295E7 ns to find the recipe for crafting table
**Gap: Multi-threaded 16.17x faster**

[20:29:41] [Server thread/INFO] [fastsuite/]: [Multithreaded Test] - Took an average of 1527112.8 ns to find the recipe for black shulker box
[20:32:07] [Server thread/INFO] [fastsuite/]: [Singlethreaded Test] - Took an average of 1.4597164E7 ns to find the recipe for black shulker box
**Gap: Multi-threaded 9.56x faster**

[20:32:43] [Server thread/INFO] [fastsuite/]: [Multithreaded Test] - Took an average of 3679896.8 ns to find the recipe for failed match
[20:38:04] [Server thread/INFO] [fastsuite/]: [Singlethreaded Test] - Took an average of 3.2105598E7 ns to find the recipe for failed match
**Gap: Multi-threaded 8.72x faster**

