package cqc.cqc.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPUCalculator;
import net.pedroksl.advanced_ae.common.entities.AdvCraftingBlockEntity;

import cqc.cqc.entity.CompactQuantumComputerBlockEntity;

/**
 * The compact quantum computer is a standalone single block. It must never be absorbed into an
 * AdvancedAE multiblock region (which would break that structure or prevent the compact block
 * from forming), so the multiblock calculator treats it as not a valid multiblock member.
 */
@Mixin(value = AdvCraftingCPUCalculator.class, remap = false)
public class MixinAdvCraftingCPUCalculator {

    @Unique
    private AdvCraftingBlockEntity cqc$calculatorTarget;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void cqc$captureTarget(AdvCraftingBlockEntity target, CallbackInfo ci) {
        this.cqc$calculatorTarget = target;
    }

    @Inject(method = "isValidBlockEntity", at = @At("HEAD"), cancellable = true)
    private void cqc$isValidBlockEntity(BlockEntity te, CallbackInfoReturnable<Boolean> cir) {
        if (te instanceof CompactQuantumComputerBlockEntity || this.cqc$calculatorTarget instanceof CompactQuantumComputerBlockEntity) {
            cir.setReturnValue(false);
        }
    }
}