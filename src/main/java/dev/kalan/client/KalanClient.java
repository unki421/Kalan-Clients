
package dev.kalan.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KalanClient implements ClientModInitializer {

    public static final String MOD_ID = "kalan_client";
    public static final String MOD_NAME = "Kalan Client";

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitializeClient() {
        LOGGER.info("================================");
        LOGGER.info("       Kalan Client");
        LOGGER.info("       Minecraft 1.21.4");
        LOGGER.info("       Client initialized!");
        LOGGER.info("================================");

        KalanCore.initialize();
    }
}
