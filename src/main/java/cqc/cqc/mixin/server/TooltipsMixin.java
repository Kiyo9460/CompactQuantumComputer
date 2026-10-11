package cqc.cqc.mixin.server;

import appeng.core.localization.Tooltips;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cqc.cqc.entity.CompactQuantumComputerBlockEntity;

/**
 * AE2's {@link Tooltips#getByteAmount(long)} walks a 4-entry byte-unit table and throws
 * ArrayIndexOutOfBoundsException once {@code bytes * 25 + 24 ... >= 1000 * 1024^3}, which the
 * quantum computer storage (now Long.MAX_VALUE) would otherwise hit. Turn any value at or above
 * that ceiling into a maxed-out display instead of crashing.
 */
@Mixin(value = Tooltips.class, remap = false)
public abstract class TooltipsMixin {

    @Inject(method = "getByteAmount(J)Lappeng/core/localization/Tooltips$Amount;", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cqc$preventByteAmountOverflow(long bytes, CallbackInfoReturnable<Tooltips.Amount> cir) {
        if (bytes >= CompactQuantumComputerBlockEntity.AE2_BYTE_AMOUNT_SAFE_CEILING) {
            cir.setReturnValue(new Tooltips.Amount("9.22", "E"));
        }
    }
}