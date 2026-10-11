package cqc.cqc.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import appeng.block.AEBaseBlockItem;

import cqc.cqc.Cqc;
import cqc.cqc.util.WaveName;

public class CompactQuantumComputerItem extends AEBaseBlockItem {

    public CompactQuantumComputerItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        String text = Component.translatable("block." + Cqc.MODID + ".compact_quantum_computer").getString();
        return WaveName.makeWaveLine(text);
    }
}