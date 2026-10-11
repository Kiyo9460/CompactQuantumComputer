package cqc.cqc.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Per-character sine-gradient coloring for item names, in the style of No-Sugar. The name is
 * prefixed with an invisible control-char marker; the client-side font mixin detects that marker
 * and re-renders the text as a smooth vertical wave with the same color gradient.
 */
public final class WaveName {

    public static final char MARKER = '\u0001';

    private static final int COLOR_DARK = 0x2B1A5E;
    private static final int COLOR_BRIGHT = 0xE6D6FF;

    private WaveName() {
    }

    public static int darkColor() {
        return COLOR_DARK;
    }

    public static int brightColor() {
        return COLOR_BRIGHT;
    }

    public static int waveColor(long time, int index, double speed, int start, int end) {
        double wave = (Math.sin((time / speed) + index) + 1.0) / 2.0;
        int r = (int) (((start >> 16) & 0xFF) * (1 - wave) + ((end >> 16) & 0xFF) * wave);
        int g = (int) (((start >> 8) & 0xFF) * (1 - wave) + ((end >> 8) & 0xFF) * wave);
        int b = (int) ((start & 0xFF) * (1 - wave) + ((end & 0xFF) & 0xFF) * wave);
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

    public static MutableComponent makeWaveLine(String text) {
        long time = System.currentTimeMillis() / 50;
        MutableComponent waveLine = Component.literal(String.valueOf(MARKER));
        int index = 0;
        for (int codePoint : text.codePoints().toArray()) {
            String glyph = new String(Character.toChars(codePoint));
            int color = waveColor(time, index, 5.0, COLOR_DARK, COLOR_BRIGHT);
            waveLine = waveLine.append(
                    Component.literal(glyph).withStyle(style -> style.withColor(color)));
            index++;
        }
        return waveLine;
    }
}