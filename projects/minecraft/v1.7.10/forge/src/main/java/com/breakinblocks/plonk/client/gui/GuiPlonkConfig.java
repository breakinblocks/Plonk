package com.breakinblocks.plonk.client.gui;

import com.breakinblocks.plonk.PlonkConstants;
import com.breakinblocks.plonk.common.config.PlonkConfig;
import cpw.mods.fml.client.config.GuiConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

public class GuiPlonkConfig extends GuiConfig {
    public GuiPlonkConfig(GuiScreen parentScreen) {
        super(
                parentScreen,
                new ConfigElement<>(PlonkConfig.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
                PlonkConstants.MOD_ID,
                false,
                false,
                PlonkConstants.MOD_NAME
        );
    }
}
