package me.andrew.gravitychanger.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(
        name ="gravitychanger"
)
public class GravityChangerConfig implements ConfigData {
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Excluded
    private boolean client;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Excluded
    private boolean server;

    @ConfigEntry.Gui.Tooltip(
            count = 2
    )
    public boolean resetGravityOnDimensionChange = false;

    @ConfigEntry.Gui.Tooltip(
            count = 2
    )
    public boolean resetGravityOnRespawn = false;

    @ConfigEntry.Gui.Tooltip(
            count = 2
    )
    public boolean voidDamageAboveWorld = true;
}
