package cqc.cqc.mixin.client;

import cqc.cqc.util.WaveName;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Faithful port of No-Sugar's FontMixin + RenderUtils#renderWavingTextDirect: text whose name
 * carries {@link WaveName#MARKER} is intercepted in every {@link Font} render path and re-rendered
 * per glyph as a vertical sine wave whose colour is a flowing black-to-purple gradient
 * (ColorUtils.waveColor). {@code -Dcqc.debugWave=true} logs each interception to the log.
 */
@Mixin(Font.class)
public abstract class MixinFontWavingText {

    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("cqc.wave");

    @Unique
    private static final float WAVE_AMPLITUDE = 1.0f;
    @Unique
    private static final float WAVE_SPEED_MIN = 2.5f;
    @Unique
    private static final double WAVE_CHAR_SPACING = 0.3f;
    @Unique
    private static final float GRADIENT_SPEED = 5.0f;
    @Unique
    private static long debugCallCount;
    @Unique
    private static final float SHADOW_DIM = 0.1f;
    @Unique
    private static final float SHADOW_Z_OFFSET = 0.001f;
    @Unique
    private static final int MAX_LIGHT = 15728880;
    @Unique
    private static boolean detailed = Boolean.parseBoolean(System.getProperty("cqc.debugWave", "false"));
    @Unique
    private static boolean onceLogged;

    @Inject(method = "drawInBatch(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;IIZ)I", at = @At("HEAD"), cancellable = true)
    private void cqc$waveString11(String text, float x, float y, int color, boolean dropShadow, Matrix4f matrix, MultiBufferSource bufferSource, Font.DisplayMode displayMode, int backgroundColor, int packedLight, boolean forceShadow, CallbackInfoReturnable<Integer> cir) {
        if (this.cqc$attemptWave(text, x, y, color, dropShadow, matrix, bufferSource, displayMode, backgroundColor, packedLight, "String(11)")) {
            cir.setReturnValue(1);
            cir.cancel();
        }
    }

    @Inject(method = "drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I", at = @At("HEAD"), cancellable = true)
    private void cqc$waveComponent(Component component, float x, float y, int color, boolean dropShadow, Matrix4f matrix, MultiBufferSource bufferSource, Font.DisplayMode displayMode, int backgroundColor, int packedLight, CallbackInfoReturnable<Integer> cir) {
        if (this.cqc$attemptWave(component.getString(), x, y, color, dropShadow, matrix, bufferSource, displayMode, backgroundColor, packedLight, "Component")) {
            cir.setReturnValue(1);
            cir.cancel();
        }
    }

    @Inject(method = "drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I", at = @At("HEAD"), cancellable = true)
    private void cqc$waveFormatted(FormattedCharSequence sequence, float x, float y, int color, boolean dropShadow, Matrix4f matrix, MultiBufferSource bufferSource, Font.DisplayMode displayMode, int backgroundColor, int packedLight, CallbackInfoReturnable<Integer> cir) {
        if (sequence != null && this.cqc$attemptWave(this.cqc$sequenceToString(sequence), x, y, color, dropShadow, matrix, bufferSource, displayMode, backgroundColor, packedLight, "FCS")) {
            cir.setReturnValue(1);
            cir.cancel();
        }
    }

    @Inject(method = "drawInternal(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I", at = @At("HEAD"), cancellable = true)
    private void cqc$waveDrawInternal(FormattedCharSequence sequence, float x, float y, int color, boolean dropShadow, Matrix4f matrix, MultiBufferSource bufferSource, Font.DisplayMode displayMode, int backgroundColor, int packedLight, CallbackInfoReturnable<Integer> cir) {
        if (sequence != null && this.cqc$attemptWave(this.cqc$sequenceToString(sequence), x, y, color, dropShadow, matrix, bufferSource, displayMode, backgroundColor, packedLight, "drawInternal")) {
            cir.setReturnValue(1);
            cir.cancel();
        }
    }

    @Inject(method = "renderText(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)F", at = @At("HEAD"), cancellable = true)
    private void cqc$waveRenderText(FormattedCharSequence sequence, float x, float y, int color, boolean dropShadow, Matrix4f matrix, MultiBufferSource bufferSource, Font.DisplayMode displayMode, int backgroundColor, int packedLight, CallbackInfoReturnable<Float> cir) {
        if (sequence != null && this.cqc$attemptWave(this.cqc$sequenceToString(sequence), x, y, color, dropShadow, matrix, bufferSource, displayMode, backgroundColor, packedLight, "renderText")) {
            cir.setReturnValue(1.0f);
            cir.cancel();
        }
    }

    @Unique
    private boolean cqc$attemptWave(String text, float x, float y, int color, boolean dropShadow, Matrix4f matrix, MultiBufferSource bufferSource, Font.DisplayMode displayMode, int backgroundColor, int packedLight, String via) {
        if (text == null || text.indexOf(WaveName.MARKER) < 0) {
            return false;
        }
        if (detailed) {
            LOGGER.info("cqc.wave intercepted via {} -> '{}'", via, this.cqc$shorten(text));
        } else if (!onceLogged) {
            onceLogged = true;
            LOGGER.info("cqc.wave: first interception via {} '{}' (set -Dcqc.debugWave=true for every frame)", via, this.cqc$shorten(text));
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("cqc.wave active: '"
                        + this.cqc$shorten(text) + "'"), false);
            }
        }
        this.cqc$handleWavingText(text.replace(String.valueOf(WaveName.MARKER), ""), x, y, color, dropShadow, matrix, bufferSource, displayMode, packedLight, backgroundColor);
        return true;
    }

    @Unique
    private void cqc$handleWavingText(String str, float x, float y, int color, boolean dropShadow, Matrix4f matrix, MultiBufferSource bufferSource, Font.DisplayMode displayMode, int packedLight, int backgroundColor) {
        Font font = (Font) (Object) this;
        PoseStack poseStack = new PoseStack();
        poseStack.mulPoseMatrix(matrix);
        float waveSpeed = Math.max(WAVE_SPEED_MIN, 6.0f - (str.length() * 0.09f));
        this.cqc$renderWavingTextDirect(font, str, x, y, System.nanoTime() * 0.000000001f, waveSpeed, WAVE_AMPLITUDE, poseStack, (MultiBufferSource.BufferSource) bufferSource, dropShadow);
    }

    @Unique
    private void cqc$renderWavingTextDirect(Font font, String text, float x, float y, double timeSec, double waveSpeed, double waveAmplitude, PoseStack poseStack, MultiBufferSource.BufferSource buffer, boolean dropShadow) {
        FontAccessor accessor = (FontAccessor) font;
        FontSet fontSet = accessor.invokeGetFontSet(Style.DEFAULT_FONT);
        boolean filterFishy = accessor.cqc$getFilterFishyGlyphs();
        Matrix4f baseMatrix = poseStack.last().pose();

        float currentX = x;
        int charIndex = 0;

        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);

            int waveColorInt = WaveName.waveColor((long) (timeSec * 30.0), charIndex, GRADIENT_SPEED, WaveName.darkColor(), WaveName.brightColor());
            float r = (float) ((waveColorInt >> 16) & 0xFF) / 255.0F;
            float g = (float) ((waveColorInt >> 8) & 0xFF) / 255.0F;
            float b = (float) (waveColorInt & 0xFF) / 255.0F;
            float a = 1.0F;

            float phaseOffset = (float) (charIndex * 0.2f);
            float waveOffset = (float) (Math.sin(timeSec * waveSpeed + phaseOffset) * waveAmplitude);

            GlyphInfo glyphInfo = fontSet.getGlyphInfo(codePoint, filterFishy);
            BakedGlyph bakedGlyph = fontSet.getGlyph(codePoint);
            boolean bold = false;
            boolean italic = false;
            float boldOffset = bold ? glyphInfo.getBoldOffset() : 0.0F;
            float shadowOff = glyphInfo.getShadowOffset();

            if (!(bakedGlyph instanceof EmptyGlyph)) {
                if (dropShadow && shadowOff > 0) {
                    Matrix4f shadowMatrix = new Matrix4f(baseMatrix).translate(0, waveOffset, 0);
                    VertexConsumer shadowVC = buffer.getBuffer(bakedGlyph.renderType(Font.DisplayMode.NORMAL));
                    accessor.invokeRenderChar(
                            bakedGlyph, bold, italic, boldOffset,
                            currentX + shadowOff, y + waveOffset + shadowOff,
                            shadowMatrix, shadowVC,
                            r * SHADOW_DIM, g * SHADOW_DIM, b * SHADOW_DIM, a,
                            MAX_LIGHT
                    );
                }

                Matrix4f charMatrix = new Matrix4f(baseMatrix).translate(0, waveOffset, SHADOW_Z_OFFSET);
                VertexConsumer charVC = buffer.getBuffer(bakedGlyph.renderType(Font.DisplayMode.NORMAL));
                accessor.invokeRenderChar(
                        bakedGlyph, bold, italic, boldOffset,
                        currentX, y + waveOffset,
                        charMatrix, charVC,
                        r, g, b, a,
                        MAX_LIGHT
                );
            }

            currentX += glyphInfo.getAdvance(bold);
            charIndex++;
            i += Character.charCount(codePoint);
        }
        if (detailed && (debugCallCount++ % 30 == 0)) {
            LOGGER.info("cqc.wave poke text='{}' t={} lastOffset={}", text, String.format(java.util.Locale.ROOT, "%.3f", timeSec),
                    String.format(java.util.Locale.ROOT, "%.3f", (Math.sin(timeSec * waveSpeed + (charIndex - 1) * 0.2f) * waveAmplitude)));
        }
    }

    @Unique
    private String cqc$sequenceToString(FormattedCharSequence sequence) {
        StringBuilder sb = new StringBuilder();
        sequence.accept((index, style, codePoint) -> {
            sb.appendCodePoint(codePoint);
            return true;
        });
        return sb.toString();
    }

    @Unique
    private String cqc$shorten(String text) {
        String cleaned = text.indexOf(WaveName.MARKER) >= 0 ? text.replace(String.valueOf(WaveName.MARKER), "") : text;
        return cleaned.length() > 64 ? cleaned.substring(0, 64) : cleaned;
    }
}