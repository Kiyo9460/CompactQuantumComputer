package cqc.cqc;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicReference;

import cqc.cqc.block.CompactQuantumComputerBlock;
import cqc.cqc.entity.CompactQuantumComputerBlockEntity;
import cqc.cqc.item.CompactQuantumComputerItem;

@Mod(Cqc.MODID)
public class Cqc {

    public static final String MODID = "cqc";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CompactQuantumComputerBlock> COMPACT_QUANTUM_COMPUTER =
            BLOCKS.register("compact_quantum_computer", CompactQuantumComputerBlock::new);
    public static final RegistryObject<CompactQuantumComputerItem> COMPACT_QUANTUM_COMPUTER_ITEM = ITEMS.register(
            "compact_quantum_computer",
            () -> new CompactQuantumComputerItem(
                    COMPACT_QUANTUM_COMPUTER.get(),
                    new Item.Properties().stacksTo(64)));
    public static final RegistryObject<BlockEntityType<CompactQuantumComputerBlockEntity>> COMPACT_QUANTUM_COMPUTER_BE =
            BLOCK_ENTITY_TYPES.register("compact_quantum_computer", Cqc::createCompactQuantumComputerBlockEntityType);

    private static final AtomicReference<BlockEntityType<CompactQuantumComputerBlockEntity>> COMPACT_BE_TYPE_REF =
            new AtomicReference<>();

    private static BlockEntityType<CompactQuantumComputerBlockEntity> createCompactQuantumComputerBlockEntityType() {
        // The block entity type is created lazily, so defer resolving it into the supplier.
        var type = BlockEntityType.Builder
                .<CompactQuantumComputerBlockEntity>of(
                        (pos, state) -> new CompactQuantumComputerBlockEntity(COMPACT_BE_TYPE_REF.get(), pos, state),
                        COMPACT_QUANTUM_COMPUTER.get())
                .build(null);
        COMPACT_BE_TYPE_REF.set(type);
        return type;
    }

    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register(
            "cqc",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + MODID))
                    .icon(() -> COMPACT_QUANTUM_COMPUTER_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> output.accept(COMPACT_QUANTUM_COMPUTER_ITEM.get()))
                    .build());

    public Cqc() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        DevStartupCheck.register();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Wire the block to its block entity type, otherwise newBlockEntity returns null.
            COMPACT_QUANTUM_COMPUTER.get().setBlockEntity(
                    CompactQuantumComputerBlockEntity.class,
                    COMPACT_QUANTUM_COMPUTER_BE.get(),
                    null,
                    null);
            LOGGER.debug("cqc: compact quantum computer ready");
        });
    }
}