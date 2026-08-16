package com.dinob.throwerlist;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThrowerListMod implements ModInitializer {
    public static final String MOD_ID = "throwerlist";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("ThrowerList initialized!");
    }
}