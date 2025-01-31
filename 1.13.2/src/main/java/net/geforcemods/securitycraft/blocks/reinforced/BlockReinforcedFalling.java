package net.geforcemods.securitycraft.blocks.reinforced;

import java.util.Random;

import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.entity.EntityFallingOwnableBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Particles;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockReinforcedFalling extends BlockReinforcedBase
{
	public static boolean fallInstantly;

	public BlockReinforcedFalling(SoundType soundType, Material mat, Block vB, String registryPath)
	{
		super(soundType, mat, vB, registryPath);
	}

	@Override
	public void onBlockAdded(IBlockState state, World world, BlockPos pos, IBlockState oldState)
	{
		world.getPendingBlockTicks().scheduleTick(pos, this, tickRate(world));
	}

	@Override
	public IBlockState updatePostPlacement(IBlockState state, EnumFacing facing, IBlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos)
	{
		world.getPendingBlockTicks().scheduleTick(currentPos, this, tickRate(world));
		return super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
	}

	@Override
	public void tick(IBlockState state, World world, BlockPos pos, Random random)
	{
		if(!world.isRemote)
			checkFallable(world, pos);
	}

	private void checkFallable(World world, BlockPos pos)
	{
		if(canFallThrough(world.getBlockState(pos.down())) && pos.getY() >= 0)
		{
			if(!fallInstantly && world.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32)))
			{
				if(!world.isRemote && world.getTileEntity(pos) instanceof IOwnable)
					world.spawnEntity(new EntityFallingOwnableBlock(world, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, world.getBlockState(pos), ((IOwnable)world.getTileEntity(pos)).getOwner()));
			}
			else
			{
				IBlockState state = getDefaultState();

				if(world.getBlockState(pos).getBlock() == this)
				{
					state = world.getBlockState(pos);
					world.removeBlock(pos);
				}

				BlockPos blockpos;

				for(blockpos = pos.down(); canFallThrough(world.getBlockState(blockpos)) && blockpos.getY() > 0; blockpos = blockpos.down()) {}

				if(blockpos.getY() > 0)
					world.setBlockState(blockpos.up(), state); //Forge: Fix loss of state information during world gen.
			}
		}
	}

	@Override
	public int tickRate(IWorldReaderBase world)
	{
		return 2;
	}

	public static boolean canFallThrough(IBlockState state)
	{
		Block block = state.getBlock();
		Material material = state.getMaterial();

		return state.isAir() || block == Blocks.FIRE || material.isLiquid() || material.isReplaceable();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(IBlockState stateIn, World world, BlockPos pos, Random rand)
	{
		if(rand.nextInt(16) == 0)
		{
			BlockPos blockpos = pos.down();

			if(canFallThrough(world.getBlockState(blockpos)))
			{
				double x = pos.getX() + rand.nextFloat();
				double y = pos.getY() - 0.05D;
				double z = pos.getZ() + rand.nextFloat();

				world.addParticle(new BlockParticleData(Particles.FALLING_DUST, stateIn), false, x, y, z, 0.0D, 0.0D, 0.0D);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public int getDustColor(IBlockState state)
	{
		return -16777216;
	}
}