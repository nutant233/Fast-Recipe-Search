package fast.fastrecipesearch;

import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;

final class Client {

    Client() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, Client::loggingIn);
    }

    private static void loggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        Runtime runtime = Runtime.getRuntime();
        System.gc();
        Config.LOGGER.info("Current memory usage: {} MB", String.format("%.2f", (runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024.0)));
    }

}
