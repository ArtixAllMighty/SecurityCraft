package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.tileentity.TileEntityMotionLight;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockMotionActivatedLight extends BlockOwnable {

	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyBool LIT = PropertyBool.create("lit");

	public BlockMotionActivatedLight(Material material) {
		super(material);
		setSoundType(SoundType.GLASS);
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state){
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state){
		return false;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos){
		EnumFacing dir = state.getValue(FACING);
		float px = 1.0F / 16.0F;

		if(dir == EnumFacing.NORTH) {
			return new AxisAlignedBB(px * 6, px * 3, 0F, px * 10, px * 9, px * 3);
		}
		else if(dir == EnumFacing.SOUTH) {
			return new AxisAlignedBB(px * 6, px * 3, 1F, px * 10, px * 9, 1F - (px * 3));
		}
		else if(dir == EnumFacing.EAST) {
			return new AxisAlignedBB(1F, px * 3, px * 6, 1F - (px * 3), px * 9, px * 10);
		}
		else if(dir == EnumFacing.WEST) {
			return new AxisAlignedBB(0F, px * 3, px * 6, px * 3, px * 9, px * 10);
		}

		return new AxisAlignedBB(px * 6, px * 3, 0F, px * 10, px * 9, px * 3);
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		if(state.getBlock() instanceof BlockMotionActivatedLight && state.getValue(LIT).booleanValue())
			return 15;
		else return 0;
	}

	public static void toggleLight(World world, BlockPos pos, double searchRadius, Owner owner, boolean isLit) {
		if(!world.isRemote)
		{
			if(isLit)
			{
				BlockUtils.setBlockProperty(world, pos, LIT, true);

				if(((IOwnable) world.getTileEntity(pos)) != null)
					((IOwnable) world.getTileEntity(pos)).setOwner(owner.getUUID(), owner.getName());

				BlockUtils.updateAndNotify(world, pos, SCContent.motionActivatedLight, 1, false);
			}
			else
			{
				BlockUtils.setBlockProperty(world, pos, LIT, false);

				if(((IOwnable) world.getTileEntity(pos)) != null)
					((IOwnable) world.getTileEntity(pos)).setOwner(owner.getUUID(), owner.getName());

				BlockUtils.updateAndNotify(world, pos, SCContent.motionActivatedLight, 1, false);
			}
		}
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side){
		if(side == EnumFacing.UP || side == EnumFacing.DOWN) return false;

		return world.isSideSolid(pos.offset(side.getOpposite()), side);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
	{
		return world.isSideSolid(pos.offset(facing.getOpposite()), facing, true) ? getDefaultState().withProperty(FACING, facing.getOpposite()) : getDefaultState().withProperty(FACING, EnumFacing.DOWN);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (!canPlaceBlockOnSide(world, pos, state.getValue(FACING).getOpposite())) {
			dropBlockAsItem(world, pos, state, 0);
			world.setBlockToAir(pos);
		}
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer){
		return getDefaultState().withProperty(FACING, placer.getHorizontalFacing()).withProperty(LIT, false);
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		if(meta == 15) return getDefaultState();

		if(meta <= 5)
			return getDefaultState().withProperty(FACING, EnumFacing.values()[meta].getAxis() == EnumFacing.Axis.Y ? EnumFacing.NORTH : EnumFacing.values()[meta]).withProperty(LIT, false);
		else
			return getDefaultState().withProperty(FACING, EnumFacing.values()[meta - 6]).withProperty(LIT, true);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		if(state.getProperties().containsKey(LIT) && state.getValue(LIT).booleanValue())
			return (state.getValue(FACING).getIndex() + 6);
		else{
			if(!state.getProperties().containsKey(FACING)) return 15;

			return state.getValue(FACING).getIndex();
		}
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] {FACING, LIT});
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityMotionLight().attacks(EntityPlayer.class, ConfigHandler.motionActivatedLightSearchRadius, 1);
	}

}
