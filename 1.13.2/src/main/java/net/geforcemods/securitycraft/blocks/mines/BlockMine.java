package net.geforcemods.securitycraft.blocks.mines;

import net.geforcemods.securitycraft.ConfigHandler.CommonConfig;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.tileentity.TileEntityOwnable;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.World;

public class BlockMine extends BlockExplosive {

	public static final BooleanProperty DEACTIVATED = BooleanProperty.create("deactivated");
	private static final VoxelShape SHAPE = Block.makeCuboidShape(5, 0, 5, 11, 3, 11);

	public BlockMine(Material material, float baseHardness) {
		super(SoundType.STONE, material, baseHardness);
		setDefaultState(stateContainer.getBaseState().with(DEACTIVATED, false));
	}

	/**
	 * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
	 */
	@Override
	public boolean isNormalCube(IBlockState state)
	{
		return false;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}

	/**
	 * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
	 * their own) Args: x, y, z, neighbor blockID
	 */
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos){
		if (world.getBlockState(pos.down()).getMaterial() != Material.AIR)
			return;
		else
			explode(world, pos);
	}

	/**
	 * Checks to see if its valid to put this block at the specified coordinates. Args: world, pos
	 */
	@Override
	public boolean isValidPosition(IBlockState state, IWorldReaderBase world, BlockPos pos){
		if(BlockUtils.getBlockMaterial(world, pos.down()) == Material.GLASS || BlockUtils.getBlockMaterial(world, pos.down()) == Material.CACTUS || BlockUtils.getBlockMaterial(world, pos.down()) == Material.AIR || BlockUtils.getBlockMaterial(world, pos.down()) == Material.CAKE || BlockUtils.getBlockMaterial(world, pos.down()) == Material.PLANTS)
			return false;
		else
			return true;
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest, IFluidState fluid){
		if(!player.isCreative() && !world.isRemote)
			if(player != null && player.isCreative() && !CommonConfig.CONFIG.mineExplodesWhenInCreative.get())
				return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
			else{
				explode(world, pos);
				return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
			}

		return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
	}

	@Override
	public VoxelShape getShape(IBlockState state, IBlockReader source, BlockPos pos)
	{
		return SHAPE;
	}

	/**
	 * Triggered whenever an entity collides with this block (enters into the block). Args: world, x, y, z, entity
	 */
	@Override
	public void onEntityCollision(IBlockState state, World world, BlockPos pos, Entity entity){
		if(world.isRemote)
			return;
		else if(entity instanceof EntityCreeper || entity instanceof EntityOcelot || entity instanceof EntityEnderman || entity instanceof EntityItem)
			return;
		else if(entity instanceof EntityLivingBase && !PlayerUtils.isPlayerMountedOnCamera((EntityLivingBase)entity))
			explode(world, pos);
	}

	@Override
	public void activateMine(World world, BlockPos pos) {
		if(!world.isRemote)
			BlockUtils.setBlockProperty(world, pos, DEACTIVATED, false);
	}

	@Override
	public void defuseMine(World world, BlockPos pos) {
		if(!world.isRemote)
			BlockUtils.setBlockProperty(world, pos, DEACTIVATED, true);
	}

	@Override
	public void explode(World world, BlockPos pos) {
		if(world.isRemote)
			return;

		if(!world.getBlockState(pos).get(DEACTIVATED).booleanValue()){
			world.destroyBlock(pos, false);
			if(CommonConfig.CONFIG.smallerMineExplosion.get())
				world.createExplosion((Entity) null, pos.getX(), pos.getY(), pos.getZ(), 1.0F, true);
			else
				world.createExplosion((Entity) null, pos.getX(), pos.getY(), pos.getZ(), 3.0F, true);
		}
	}

	/**
	 * Returns the ID of the items to drop on destruction.
	 */
	@Override
	public IItemProvider getItemDropped(IBlockState state, World worldIn, BlockPos pos, int fortune){
		return SCContent.mine.asItem();
	}

	/**
	 * only called by clickMiddleMouseButton , and passed to inventory.setCurrentItem (along with isCreative)
	 */
	@Override
	public ItemStack getItem(IBlockReader worldIn, BlockPos pos, IBlockState state){
		return new ItemStack(SCContent.mine.asItem());
	}

	@Override
	protected void fillStateContainer(Builder<Block, IBlockState> builder)
	{
		builder.add(DEACTIVATED);
	}

	@Override
	public boolean isActive(World world, BlockPos pos) {
		return !world.getBlockState(pos).get(DEACTIVATED).booleanValue();
	}

	@Override
	public boolean isDefusable() {
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader world) {
		return new TileEntityOwnable();
	}

}
