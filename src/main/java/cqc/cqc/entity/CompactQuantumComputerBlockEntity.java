package cqc.cqc.entity;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.pedroksl.advanced_ae.common.entities.AdvCraftingBlockEntity;

import appeng.api.orientation.BlockOrientation;

/**
 * A single-block replacement for a maximum-size AdvancedAE quantum computer. It reports the
 * maximum possible storage and accelerator stats of a full structure while only occupying one
 * block.
 */
public final class CompactQuantumComputerBlockEntity extends AdvCraftingBlockEntity {

    /**
     * Storage is carried as a long end to end (grid CPU, crafting plans, menus), so the ceiling is
     * the long itself, and we use Long.MAX_VALUE. AE2's Tooltips#getByteAmount walks a 4-entry unit
     * table and throws ArrayIndexOutOfBoundsException for anything >= {@link #AE2_BYTE_AMOUNT_SAFE_CEILING}
     * (its final unit is 1024^3), so TooltipsMixin clamps the display of anything at or above that.
     * The per-block storage multiplier is forced to 1 so that AdvCraftingCPUCluster's
     * {@code totalStorage = storage * multiplier} cannot overflow.
     */
    public static final long MAX_STORAGE_BYTES = Long.MAX_VALUE;
    /**
     * Highest byte count AE2's Tooltips#getByteAmount can format before its unit table overflows.
     */
    public static final long AE2_BYTE_AMOUNT_SAFE_CEILING = 1000L * 1024L * 1024L * 1024L - 1L;
    /**
     * AdvCraftingCPUCluster.addBlockEntity throws IllegalArgumentException when a unit block
     * reports more than 16 threads ("Co-processor threads may not exceed 16 per single unit
     * block."), so 16 is the hard ceiling for one block.
     */
    public static final int MAX_ACCELERATOR_THREADS = 16;
    /**
     * Cluster co-processor count is {@code threads * multiplier} computed in int, and is consumed
     * by {@code remainingOperations = getCoProcessors() + 1 - ...}. The highest multiplier that
     * keeps {@code 16 * multiplier + 1} inside int range.
     */
    public static final int MAX_ACCELERATION_MULTIPLIER = Integer.MAX_VALUE / MAX_ACCELERATOR_THREADS;

    public CompactQuantumComputerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public long getStorageBytes() {
        return MAX_STORAGE_BYTES;
    }

    @Override
    public int getStorageMultiplier() {
        return 1;
    }

    @Override
    public int getAcceleratorThreads() {
        return MAX_ACCELERATOR_THREADS;
    }

    @Override
    public int getAccelerationMultiplier() {
        return MAX_ACCELERATION_MULTIPLIER;
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return isFormed() ? EnumSet.allOf(Direction.class) : EnumSet.noneOf(Direction.class);
    }
}