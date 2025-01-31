package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.misc.OwnershipEvent;
import net.geforcemods.securitycraft.tileentity.TileEntityKeypadChest;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class BlockKeypadChest extends ChestBlock implements IPasswordConvertible {

	public BlockKeypadChest(){
		super(Block.Properties.create(Material.WOOD).sound(SoundType.WOOD).hardnessAndResistance(-1.0F, 6000000.0F));
	}

	/**
	 * Called upon block activation (right click on the block.)
	 */
	@Override
	public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit){
		if(!world.isRemote) {
			if(!PlayerUtils.isHoldingItem(player, SCContent.codebreaker) && world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileEntityKeypadChest)
				((TileEntityKeypadChest) world.getTileEntity(pos)).openPasswordGUI(player);

			return true;
		}

		return true;
	}

	public static void activate(World world, BlockPos pos, PlayerEntity player){
		if(!world.isRemote) {
			BlockState state = world.getBlockState(pos);
			ChestBlock block = (ChestBlock)state.getBlock();
			INamedContainerProvider inamedcontainerprovider = block.getContainer(state, world, pos);
			if (inamedcontainerprovider != null) {
				player.openContainer(inamedcontainerprovider);
				player.addStat(Stats.CUSTOM.get(Stats.OPEN_CHEST));
			}
		}
	}

	/**
	 * Called when the block is placed in the world.
	 */
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack){
		super.onBlockPlacedBy(world, pos, state, entity, stack);

		if(entity instanceof PlayerEntity)
			MinecraftForge.EVENT_BUS.post(new OwnershipEvent(world, pos, (PlayerEntity)entity));

		if(world.getTileEntity(pos.east()) != null && world.getTileEntity(pos.east()) instanceof TileEntityKeypadChest)
			((TileEntityKeypadChest)(world.getTileEntity(pos))).setPassword(((TileEntityKeypadChest) world.getTileEntity(pos.east())).getPassword());
		else if(world.getTileEntity(pos.west()) != null && world.getTileEntity(pos.west()) instanceof TileEntityKeypadChest)
			((TileEntityKeypadChest)(world.getTileEntity(pos))).setPassword(((TileEntityKeypadChest) world.getTileEntity(pos.west())).getPassword());
		else if(world.getTileEntity(pos.south()) != null && world.getTileEntity(pos.south()) instanceof TileEntityKeypadChest)
			((TileEntityKeypadChest)(world.getTileEntity(pos))).setPassword(((TileEntityKeypadChest) world.getTileEntity(pos.south())).getPassword());
		else if(world.getTileEntity(pos.north()) != null && world.getTileEntity(pos.north()) instanceof TileEntityKeypadChest)
			((TileEntityKeypadChest)(world.getTileEntity(pos))).setPassword(((TileEntityKeypadChest) world.getTileEntity(pos.north())).getPassword());
	}

	@Override
	public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor){
		super.onNeighborChange(state, world, pos, neighbor);
		TileEntityKeypadChest ChestTileEntity = (TileEntityKeypadChest)world.getTileEntity(pos);

		if (ChestTileEntity != null)
			ChestTileEntity.updateContainingBlockInfo();

	}

	/**
	 * Returns a new instance of a block's tile entity class. Called on placing the block.
	 */
	@Override
	public TileEntity createNewTileEntity(IBlockReader reader)
	{
		return new TileEntityKeypadChest();
	}

	public static boolean isBlocked(World world, BlockPos pos)
	{
		return isBelowSolidBlock(world, pos);
	}

	private static boolean isBelowSolidBlock(World world, BlockPos pos)
	{
		return BlockUtils.isSideSolid(world, pos.up(), Direction.DOWN);
	}

	@Override
	public Block getOriginalBlock()
	{
		return Blocks.CHEST;
	}

	@Override
	public boolean convert(PlayerEntity player, World world, BlockPos pos)
	{
		Direction facing = world.getBlockState(pos).get(FACING);
		ChestTileEntity chest = (ChestTileEntity)world.getTileEntity(pos);
		CompoundNBT tag = chest.write(new CompoundNBT());

		chest.clear();
		world.setBlockState(pos, SCContent.keypadChest.getDefaultState().with(FACING, facing));
		((IOwnable) world.getTileEntity(pos)).getOwner().set(player.getUniqueID().toString(), player.getName().getFormattedText());
		((ChestTileEntity)world.getTileEntity(pos)).read(tag);
		return true;
	}
}
