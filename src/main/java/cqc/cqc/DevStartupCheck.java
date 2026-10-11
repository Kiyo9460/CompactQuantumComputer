package cqc.cqc;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPUCalculator;
import org.slf4j.Logger;

import appeng.api.networking.IGridNode;
import appeng.me.Grid;

import cqc.cqc.entity.CompactQuantumComputerBlockEntity;

/**
 * Dev-only startup verification. Enabled solely when the JVM property "cqc.devcheck" is set, so a
 * normal launch is completely unaffected. It forces the mixin target classes through the class
 * transformer (a failing injection throws during class load) and sanity-checks the maximum
 * reported stats of the compact computer.
 */
public final class DevStartupCheck {

    private static final Logger LOGGER = LogUtils.getLogger();

    private DevStartupCheck() {
    }

    public static void register() {
        if (System.getProperty("cqc.devcheck") != null) {
            MinecraftForge.EVENT_BUS.addListener(DevStartupCheck::onServerStarted);
        }
    }

    private static void onServerStarted(ServerStartedEvent event) {
        try {
            checkMixinsApply();
            checkRegistries();
            checkStats();
            LOGGER.info("cqc: dev startup check PASSED");
        } catch (Throwable t) {
            LOGGER.error("cqc: dev startup check FAILED", t);
            throw new RuntimeException("cqc dev startup check failed", t);
        }
    }

    private static void checkMixinsApply() throws ClassNotFoundException {
        // Class loading through the transforming class loader applies GridMixin to Grid and
        // MixinAdvCraftingCPUCalculator to AdvCraftingCPUCalculator. Bad @Inject/@Shadow targets
        // surface as an error right here instead of failing lazily inside the running game.
        Class.forName(Grid.class.getName());
        Class.forName(AdvCraftingCPUCalculator.class.getName());
        Class.forName(IGridNode.class.getName());
        LOGGER.info("cqc: mixin target classes loaded with transformations applied");
    }

    private static void checkRegistries() {
        if (!net.minecraftforge.registries.ForgeRegistries.BLOCKS.containsValue(
                Cqc.COMPACT_QUANTUM_COMPUTER.get())) {
            throw new IllegalStateException("compact_quantum_computer block not registered");
        }
        if (!net.minecraftforge.registries.ForgeRegistries.ITEMS.containsValue(
                Cqc.COMPACT_QUANTUM_COMPUTER_ITEM.get())) {
            throw new IllegalStateException("compact_quantum_computer item not registered");
        }
        if (!net.minecraftforge.registries.ForgeRegistries.BLOCK_ENTITY_TYPES.containsValue(
                Cqc.COMPACT_QUANTUM_COMPUTER_BE.get())) {
            throw new IllegalStateException("compact_quantum_computer block entity type not registered");
        }
    }

    private static void checkStats() {
        long coProcessors = (long) CompactQuantumComputerBlockEntity.MAX_ACCELERATOR_THREADS
                * CompactQuantumComputerBlockEntity.MAX_ACCELERATION_MULTIPLIER;

        LOGGER.info("cqc: dev stats maxStorageBytes={} threads={} accelMultiplier={} coProcessors={}",
                CompactQuantumComputerBlockEntity.MAX_STORAGE_BYTES,
                CompactQuantumComputerBlockEntity.MAX_ACCELERATOR_THREADS,
                CompactQuantumComputerBlockEntity.MAX_ACCELERATION_MULTIPLIER,
                coProcessors);

        if (coProcessors != 2_147_483_632L) {
            throw new IllegalStateException("expected 2147483632 co-processors, got " + coProcessors);
        }
        if (CompactQuantumComputerBlockEntity.MAX_STORAGE_BYTES != Long.MAX_VALUE) {
            throw new IllegalStateException("expected Long.MAX_VALUE storage bytes, got "
                    + CompactQuantumComputerBlockEntity.MAX_STORAGE_BYTES);
        }
    }
}