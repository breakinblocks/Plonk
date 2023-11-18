package com.breakinblocks.plonk.client.gui;

import com.breakinblocks.plonk.PlonkConstants;
import com.breakinblocks.plonk.common.config.PlonkConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;

public class GuiPlonkConfig extends GuiConfig {
    public GuiPlonkConfig(GuiScreen parentScreen) {
        super(
                parentScreen,
                new ConfigElement(PlonkConfig.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
                PlonkConstants.MOD_ID,
                false,
                false,
                PlonkConstants.MOD_NAME
        );
    }
}
