package cqc.cqc.mixin.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Access to Font internals needed for direct per-glyph rendering (exact port of No-Sugar's
 * FontAccessor). {@code invokeGetFontSet}/{@code invokeRenderChar} target the package-private
 * methods; {@code getFilterFishyGlyphs} reads the private glyph-filter flag.
 */
@Mixin(Font.class)
public interface FontAccessor {

    @Accessor("filterFishyGlyphs")
    boolean cqc$getFilterFishyGlyphs();

    @Invoker("getFontSet")
    FontSet invokeGetFontSet(ResourceLocation location);

    @Invoker("renderChar")
    void invokeRenderChar(BakedGlyph glyph, boolean bold, boolean italic, float boldOffset, float x, float y,
                          Matrix4f matrix, VertexConsumer consumer,
                          float r, float g, float b, float a, int packedLight);
}