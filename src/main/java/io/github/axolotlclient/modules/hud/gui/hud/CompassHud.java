package io.github.axolotlclient.modules.hud.gui.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlclientConfig.Color;
import io.github.axolotlclient.AxolotlclientConfig.options.*;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import io.github.axolotlclient.modules.hud.util.RenderUtil;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 * @license GPL-3.0
 */

public class CompassHud extends TextHudEntry implements DynamicallyPositionable {

    public final Identifier ID = new Identifier("kronhud", "compasshud");

    private final IntegerOption widthOption = new IntegerOption("width", this::updateWidth, width, 100, 800);

    private final ColorOption lookingBox = new ColorOption("axolotlclient.lookingbox", new Color(0x80000000));
    private final ColorOption degreesColor = new ColorOption("axolotlclient.degreescolor",  new Color(-1));
    private final ColorOption majorIndicatorColor = new ColorOption("axolotlclient.majorindicator", new Color(-1));
    private final ColorOption minorIndicatorColor = new ColorOption("axolotlclient.minorindicator", new Color(0xCCFFFFFF));
    private final ColorOption cardinalColor = new ColorOption("axolotlclient.cardinalcolor", Color.WHITE);
    private final ColorOption semiCardinalColor = new ColorOption("axolotlclient.semicardinalcolor", new Color(0xFFAAAAAA));
    private final BooleanOption invert = new BooleanOption("axolotlclient.invert_direction", false);
    private final BooleanOption showDegrees = new BooleanOption("axolotlclient.showdegrees", true);

    private void updateWidth(int newWidth){
        setWidth(newWidth);
        onBoundsUpdate();
    }

    public CompassHud() {
        super(240, 33, false);
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public void renderComponent(float delta) {
        renderCompass();
    }

    @Override
    public void renderPlaceholderComponent(float delta) {
        renderCompass();
    }

    public void renderCompass() {
        // N = 0
        // E = 90
        // S = 180
        // W = 270
        if (client.player == null) {
            return;
        }
        float halfWidth = width / 2f;
        float degrees = (client.player.yaw + 180) % 360;
        if (degrees < -180) {
            degrees += 360;
        }
        float start = degrees - 150 + 360;
//        float end = degrees + 150 + 360;
        int startIndicator = ((int) (start + 8) / 15) * 15;
        int amount = 21;
//        int endIndicator = startIndicator + 15 * amount;
        int dist = width / (amount);
        DrawPosition pos = getPos();
        int x = pos.x();
        int y = pos.y() + 1;
        RenderUtil.drawRectangle(pos.x() + (int) halfWidth - 1, pos.y(), 3, 11, lookingBox.get());
        if (showDegrees.get()) {
            DrawUtil.drawCenteredString(
                    client.textRenderer, String.valueOf((int) degrees), x + (int) halfWidth, y + 20, degreesColor.get(),
                    shadow.get()
            );
        }
        float shift = (startIndicator - start) / 15f * dist;
        if (invert.get()) {
            shift = dist - shift;
        }
        GlStateManager.translatef(shift, 0, 0);
        for (int i = 0; i < amount; i++) {
            int d;
            if (invert.get()) {
                d = (startIndicator + ((amount - i - 2) * 15)) % 360;
            } else {
                d = (startIndicator + i * 15) % 360;
            }
            int innerX = x + dist * (i + 1);
            Indicator indicator = getIndicator(d);

            float trueDist;
            if (invert.get()) {
                trueDist = ((amount - i) * dist) - shift;
            } else {
                trueDist = ((i + 1) * dist) - shift;
            }

            float targetOpacity = 1 - Math.abs((halfWidth - trueDist)) / halfWidth;
            //System.out.println(targetOpacity);
            GlStateManager.color4f(1, 1, 1, targetOpacity);
            if (indicator == Indicator.CARDINAL) {
                // We have to call .color() here so that transparency stays
                RenderUtil.drawRectangle(innerX, y, 1, 9, majorIndicatorColor.get().withAlpha((int) (majorIndicatorColor.get().getAlpha() * targetOpacity)).getAsInt());
                Color color = cardinalColor.get();
                color = color.withAlpha((int) (color.getAlpha() * targetOpacity));
                if (color.getAlpha() > 0) {
                    DrawUtil.drawCenteredString(
                            client.textRenderer, getCardString(indicator, d), innerX + 1, y + 10, color, shadow.get());
                }

            } else if (indicator == Indicator.SEMI_CARDINAL) {
                Color color = semiCardinalColor.get();
                color = color.withAlpha((int) (color.getAlpha() * targetOpacity));
                if (color.getAlpha() > 0) {
                    DrawUtil.drawCenteredString(
                            client.textRenderer, getCardString(indicator, d), innerX + 1, y + 1, color, shadow.get());
                }
            } else {
                // We have to call .color() here so that transparency stays
                RenderUtil.drawRectangle(innerX, y, 1, 5, minorIndicatorColor.get().withAlpha((int) (minorIndicatorColor.get().getAlpha() * targetOpacity)).getAsInt());
            }
        }
        GlStateManager.color4f(1, 1, 1, 1);
        GlStateManager.translatef(-shift, 0, 0);
    }

    private static Indicator getIndicator(int degrees) {
        if (degrees % 90 == 0) {
            return Indicator.CARDINAL;
        }
        if (degrees % 45 == 0) {
            return Indicator.SEMI_CARDINAL;
        }
        return Indicator.SMALL;
    }

    private static String getCardString(Indicator indicator, int degrees) {
        if (indicator == Indicator.CARDINAL) {
            switch (degrees) {
                case 0:
                    return "N";

                case 90 :
                    return "E";
                case 180:
                    return "S";
                case 270:
                    return "W";
                default:
                    return "NaD";
            }
        }
        switch (degrees) {
            case 45:
                return "NE";
            case 135:
                return "SE";
            case 225:
                return "SW";
            case 315:
                return "NW";
            default:
                return "NaD";
        }
    }

    @Override
    public List<Option<?>> getConfigurationOptions() {
        List<Option<?>> options = super.getConfigurationOptions();
        options.add(widthOption);
        options.add(showDegrees);
        options.add(invert);
        options.add(lookingBox);
        options.add(degreesColor);
        options.add(cardinalColor);
        options.add(semiCardinalColor);
        options.add(majorIndicatorColor);
        options.add(minorIndicatorColor);
        return options;
    }

    @Override
    public AnchorPoint getAnchor() {
        // Won't be dynamically set
        return AnchorPoint.TOP_MIDDLE;
    }

    private enum Indicator {
        CARDINAL,
        SEMI_CARDINAL,
        SMALL

    }
}