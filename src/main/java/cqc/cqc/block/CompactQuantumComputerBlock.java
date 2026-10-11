package cqc.cqc.block;

import net.pedroksl.advanced_ae.common.blocks.AAEAbstractCraftingUnitBlock;
import net.pedroksl.advanced_ae.common.blocks.AAECraftingUnitBlock;
import net.pedroksl.advanced_ae.common.blocks.AAECraftingUnitType;

import cqc.cqc.entity.CompactQuantumComputerBlockEntity;

public class CompactQuantumComputerBlock extends AAEAbstractCraftingUnitBlock<CompactQuantumComputerBlockEntity> {

    public CompactQuantumComputerBlock() {
        super(
                AAECraftingUnitBlock.getProps(AAECraftingUnitType.QUANTUM_CORE, true),
                AAECraftingUnitType.QUANTUM_CORE);
    }
}