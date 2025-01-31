package net.geforcemods.securitycraft.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockFrame extends BlockOwnable {

	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	private static final VoxelShape SHAPE_NORTH = Block.makeCuboidShape(2, 2, 0, 14, 14, 1);
	private static final VoxelShape SHAPE_EAST = Block.makeCuboidShape(15, 2, 2, 16, 14, 14);
	private static final VoxelShape SHAPE_SOUTH = Block.makeCuboidShape(2, 2, 15, 14, 14, 16);
	private static final VoxelShape SHAPE_WEST = Block.makeCuboidShape(0, 2, 2, 1, 14, 14);

	public BlockFrame(Material material){
		super(SoundType.STONE, Block.Properties.create(material).hardnessAndResistance(-1.0F, 6000000.0F));
		setDefaultState(stateContainer.getBaseState().with(FACING, EnumFacing.NORTH));
	}

	@Override
	public VoxelShape getShape(IBlockState state, IBlockReader world, BlockPos pos)
	{
		VoxelShape shape = null;

		switch(state.get(FACING))
		{
			case NORTH: shape = SHAPE_NORTH; break;
			case EAST: shape = SHAPE_EAST; break;
			case SOUTH: shape = SHAPE_SOUTH; break;
			case WEST: shape = SHAPE_WEST; break;
			default: shape = VoxelShapes.empty();
		}

		return VoxelShapes.combine(VoxelShapes.fullCube(), shape, IBooleanFunction.ONLY_FIRST); //subtract
	}

	@Override
	public boolean isNormalCube(IBlockState state){
		return false;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockReader world, IBlockState state, BlockPos pos, EnumFacing face)
	{
		return face == state.get(FACING) ? BlockFaceShape.UNDEFINED : BlockFaceShape.SOLID;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public IBlockState getStateForPlacement(BlockItemUseContext ctx)
	{
		return getStateForPlacement(ctx.getWorld(), ctx.getPos(), ctx.getFace(), ctx.getHitX(), ctx.getHitY(), ctx.getHitZ(), ctx.getPlayer());
	}

	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, EntityPlayer placer)
	{
		return getDefaultState().with(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	protected void fillStateContainer(Builder<Block, IBlockState> builder)
	{
		builder.add(FACING);
	}
}
