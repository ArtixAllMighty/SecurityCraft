package net.geforcemods.securitycraft.blocks.reinforced;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IBeaconBeamColorProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.DyeColor;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class BlockReinforcedStainedGlassPane extends BlockReinforcedPane implements IBeaconBeamColorProvider
{
	private final DyeColor color;

	public BlockReinforcedStainedGlassPane(DyeColor color, Block vB, String registryPath)
	{
		super(SoundType.GLASS, Material.GLASS, vB, registryPath);
		this.color = color;
	}

	@Override
	public float[] getBeaconColorMultiplier(BlockState state, IWorldReader world, BlockPos pos, BlockPos beaconPos)
	{
		return color.getColorComponentValues();
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public DyeColor getColor()
	{
		return color;
	}
}
