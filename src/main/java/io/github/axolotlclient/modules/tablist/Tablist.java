package io.github.axolotlclient.modules.tablist;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.Color;
import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.options.OptionCategory;
import io.github.axolotlclient.modules.AbstractModule;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

public class Tablist extends AbstractModule {

    @Getter
    private static final Tablist Instance = new Tablist();

    private final BooleanOption numericalPing = new BooleanOption("numericalPing", false);
    private final ColorOption pingColor0 = new ColorOption("pingColor0", Color.parse("#FF00FFFF"));
    private final ColorOption pingColor1 = new ColorOption("pingColor1", Color.parse("#FF00FF00"));
    private final ColorOption pingColor2 = new ColorOption("pingColor2", Color.parse("#FF008800"));
    private final ColorOption pingColor3 = new ColorOption("pingColor3", Color.parse("#FFFFFF00"));
    private final ColorOption pingColor4 = new ColorOption("pingColor4", Color.parse("#FFFF8800"));
    private final ColorOption pingColor5 = new ColorOption("pingColor5", Color.parse("#FFFF0000"));
    private final BooleanOption shadow = new BooleanOption("shadow", true);

    public final BooleanOption showPlayerHeads = new BooleanOption("showPlayerHeads", true);

    private final OptionCategory tablist = new OptionCategory("tablist");

    @Override
    public void init() {
        tablist.add(numericalPing, showPlayerHeads, shadow);
        tablist.add(pingColor0, pingColor1, pingColor2, pingColor3, pingColor4, pingColor5);

        AxolotlClient.CONFIG.rendering.add(tablist);
    }

    public boolean renderNumericPing(int width, int x, int y, PlayerListEntry entry){
        if(numericalPing.get()){
            Color current;
            if (entry.getLatency() < 0) {
                current = pingColor0.get();
            } else if (entry.getLatency() < 150) {
                current = pingColor1.get();
            } else if (entry.getLatency() < 300) {
                current = pingColor2.get();
            } else if (entry.getLatency() < 600) {
                current = pingColor3.get();
            } else if (entry.getLatency() < 1000) {
                current = pingColor4.get();
            } else {
                current = pingColor5.get();
            }

            DrawUtil.drawString(
                    String.valueOf(entry.getLatency()),
                    x+width - 1 - MinecraftClient.getInstance().textRenderer.getStringWidth(String.valueOf(entry.getLatency())),
                    y, current, shadow.get());
            return true;
        }
        return false;
    }
}