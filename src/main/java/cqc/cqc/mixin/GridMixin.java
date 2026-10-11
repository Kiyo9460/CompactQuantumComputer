package cqc.cqc.mixin;

import java.util.LinkedHashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.SetMultimap;

import net.pedroksl.advanced_ae.common.entities.AdvCraftingBlockEntity;

import appeng.api.networking.IGridNode;
import appeng.me.Grid;

/**
 * appeng.me.Grid indexes machines by their exact runtime class. Our compact block entity is a
 * subclass of AdvCraftingBlockEntity, so naive lookups for the AAE superclass miss it. Extend
 * Grid.getMachines so subclass instances still show up for the superclass key.
 */
@Mixin(value = Grid.class, remap = false)
public class GridMixin {

    @Shadow
    @Final
    private SetMultimap<Class<?>, IGridNode> machines;

    @Inject(method = "getMachines", at = @At("RETURN"), cancellable = true)
    private void cqc$getMachines(Class<?> machineClass, CallbackInfoReturnable<Set<?>> cir) {
        if (machineClass == AdvCraftingBlockEntity.class) {
            Set<Object> result = new LinkedHashSet<>(cir.getReturnValue());
            for (IGridNode node : this.machines.values()) {
                Object owner = node.getOwner();
                if (machineClass.isInstance(owner)) {
                    result.add(owner);
                }
            }
            cir.setReturnValue(result);
        }
    }
}