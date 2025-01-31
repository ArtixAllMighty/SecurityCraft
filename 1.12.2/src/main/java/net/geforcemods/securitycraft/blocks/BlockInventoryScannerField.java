package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.IIntersectable;
import net.geforcemods.securitycraft.api.TileEntitySCTE;
import net.geforcemods.securitycraft.misc.EnumCustomModules;
import net.geforcemods.securitycraft.tileentity.TileEntityInventoryScanner;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.ModuleUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockInventoryScannerField extends BlockContainer implements IIntersectable {

	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

	public BlockInventoryScannerField(Material material) {
		super(material);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess world, BlockPos pos)
	{
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.TRANSLUCENT;
	}

	/**
	 * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
	 * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
	 */
	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
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
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}

	@Override
	public boolean isPassable(IBlockAccess world, BlockPos pos)
	{
		return true;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public void onEntityIntersected(World world, BlockPos pos, Entity entity)
	{
		TileEntityInventoryScanner connectedScanner = BlockInventoryScanner.getConnectedInventoryScanner(world, pos);

		if(connectedScanner == null)
			return;

		if(entity instanceof EntityPlayer)
		{
			if(ModuleUtils.checkForModule(world, connectedScanner.getPos(), (EntityPlayer)entity, EnumCustomModules.WHITELIST))
				return;

			for(int i = 0; i < 10; i++)
			{
				for(int j = 0; j < ((EntityPlayer)entity).inventory.mainInventory.size(); j++)
				{
					if(!connectedScanner.getStackInSlotCopy(i).isEmpty() && !((EntityPlayer)entity).inventory.mainInventory.get(j).isEmpty())
						checkInventory((EntityPlayer)entity, connectedScanner, connectedScanner.getStackInSlotCopy(i));
				}
			}
		}
		else if(entity instanceof EntityItem)
		{
			for(int i = 0; i < 10; i++)
			{
				if(!connectedScanner.getStackInSlotCopy(i).isEmpty() && !((EntityItem)entity).getItem().isEmpty())
					checkEntityItem((EntityItem)entity, connectedScanner, connectedScanner.getStackInSlotCopy(i));
			}
		}
	}

	public static void checkInventory(EntityPlayer entity, TileEntityInventoryScanner te, ItemStack stack)
	{
		boolean hasSmartModule = te.hasModule(EnumCustomModules.SMART);
		boolean hasStorageModule = te.hasModule(EnumCustomModules.STORAGE);

		if(te.getType().equals("redstone"))
		{
			for(int i = 1; i <= entity.inventory.mainInventory.size(); i++)
			{
				ItemStack itemStackChecking = entity.inventory.mainInventory.get(i - 1);

				if(!itemStackChecking.isEmpty())
				{
					if((hasSmartModule && areItemStacksEqual(entity.inventory.mainInventory.get(i - 1), stack) && ItemStack.areItemStackTagsEqual(entity.inventory.mainInventory.get(i - 1), stack))
							|| (!hasSmartModule && entity.inventory.mainInventory.get(i - 1).getItem() == stack.getItem()) || checkForShulkerBox(itemStackChecking, stack, te, hasSmartModule, hasStorageModule))
					{
						updateInventoryScannerPower(te);
					}
				}
			}
		}
		else if(te.getType().equals("check"))
		{
			for(int i = 1; i <= entity.inventory.mainInventory.size(); i++)
			{
				ItemStack itemStackChecking = entity.inventory.mainInventory.get(i - 1);

				if(!itemStackChecking.isEmpty())
				{
					checkForShulkerBox(itemStackChecking, stack, te, hasSmartModule, hasStorageModule);

					if((hasSmartModule && areItemStacksEqual(entity.inventory.mainInventory.get(i - 1), stack) && ItemStack.areItemStackTagsEqual(entity.inventory.mainInventory.get(i - 1), stack))
							|| (!hasSmartModule && entity.inventory.mainInventory.get(i - 1).getItem() == stack.getItem()))
					{
						if(hasStorageModule)
							te.addItemToStorage(entity.inventory.mainInventory.get(i - 1));

						entity.inventory.mainInventory.set(i - 1, ItemStack.EMPTY);
					}
				}
			}
		}
	}

	public static void checkEntityItem(EntityItem entity, TileEntityInventoryScanner te, ItemStack stack)
	{
		boolean hasSmartModule = te.hasModule(EnumCustomModules.SMART);
		boolean hasStorageModule = te.hasModule(EnumCustomModules.STORAGE);

		if(te.getType().equals("redstone"))
		{
			if((hasSmartModule && areItemStacksEqual(entity.getItem(), stack) && ItemStack.areItemStackTagsEqual(entity.getItem(), stack))
					|| (!hasSmartModule && entity.getItem().getItem() == stack.getItem()) || checkForShulkerBox(entity.getItem(), stack, te, hasSmartModule, hasStorageModule))
			{
				updateInventoryScannerPower(te);
			}
		}
		else if(te.getType().equals("check"))
		{
			checkForShulkerBox(entity.getItem(), stack, te, hasSmartModule, hasStorageModule);

			if((hasSmartModule && areItemStacksEqual(entity.getItem(), stack) && ItemStack.areItemStackTagsEqual(entity.getItem(), stack))
					|| (!hasSmartModule && entity.getItem().getItem() == stack.getItem()))
			{
				if(hasStorageModule)
					te.addItemToStorage(entity.getItem());

				entity.setDead();
			}
		}
	}

	private static boolean checkForShulkerBox(ItemStack item, ItemStack stackToCheck, TileEntityInventoryScanner te, boolean hasSmartModule, boolean hasStorageModule) {
		boolean deletedItem = false;

		if(item != null) {
			if(!item.isEmpty() && item.getTagCompound() != null && Block.getBlockFromItem(item.getItem()) instanceof BlockShulkerBox) {
				NBTTagList list = item.getTagCompound().getCompoundTag("BlockEntityTag").getTagList("Items", NBT.TAG_COMPOUND);
	
				for(int i = 0; i < list.tagCount(); i++) {
					ItemStack itemInChest = new ItemStack(list.getCompoundTagAt(i));
					if((hasSmartModule && areItemStacksEqual(itemInChest, stackToCheck) && ItemStack.areItemStackTagsEqual(itemInChest, stackToCheck)) || (!hasSmartModule && areItemStacksEqual(itemInChest, stackToCheck))) {
						list.removeTag(i);
						deletedItem = true;
						
						if(hasStorageModule)
							te.addItemToStorage(itemInChest);
					}
				}
			}
		}

		return deletedItem;
	}

	private static void updateInventoryScannerPower(TileEntityInventoryScanner te)
	{
		if(!te.shouldProvidePower())
			te.setShouldProvidePower(true);

		SecurityCraft.log("Running te update");
		te.setCooldown(60);
		checkAndUpdateTEAppropriately(te);
		BlockUtils.updateAndNotify(te.getWorld(), te.getPos(), te.getWorld().getBlockState(te.getPos()).getBlock(), 1, true);
		SecurityCraft.log("Emitting redstone on the " + FMLCommonHandler.instance().getEffectiveSide() + " side. (te coords: " + Utils.getFormattedCoordinates(te.getPos()));
	}

	/**
	 * See {@link ItemStack#areItemStacksEqual(ItemStack, ItemStack)} but without size restriction
	 */
	public static boolean areItemStacksEqual(ItemStack stack1, ItemStack stack2)
	{
		ItemStack s1 = stack1.copy();
		ItemStack s2 = stack2.copy();

		s1.setCount(1);
		s2.setCount(1);
		return ItemStack.areItemStacksEqual(s1, s2);
	}

	private static void checkAndUpdateTEAppropriately(TileEntityInventoryScanner te)
	{
		TileEntityInventoryScanner connectedScanner = BlockInventoryScanner.getConnectedInventoryScanner(te.getWorld(), te.getPos());

		if(connectedScanner == null)
			return;

		te.setShouldProvidePower(true);
		te.setCooldown(60);
		BlockUtils.updateAndNotify(te.getWorld(), te.getPos(), te.getBlockType(), 1, true);
		connectedScanner.setShouldProvidePower(true);
		connectedScanner.setCooldown(60);
		BlockUtils.updateAndNotify(connectedScanner.getWorld(), connectedScanner.getPos(), connectedScanner.getBlockType(), 1, true);
	}

	@Override
	public void onPlayerDestroy(World world, BlockPos pos, IBlockState state)
	{
		if(!world.isRemote)
		{
			for(int i = 0; i < ConfigHandler.inventoryScannerRange; i++)
			{
				if(BlockUtils.getBlock(world, pos.west(i)) == SCContent.inventoryScanner)
				{
					for(int j = 1; j < i; j++)
					{
						world.destroyBlock(pos.west(j), false);
					}

					break;
				}
			}

			for(int i = 0; i < ConfigHandler.inventoryScannerRange; i++)
			{
				if(BlockUtils.getBlock(world, pos.east(i)) == SCContent.inventoryScanner)
				{
					for(int j = 1; j < i; j++)
					{
						world.destroyBlock(pos.east(j), false);
					}

					break;
				}
			}

			for(int i = 0; i < ConfigHandler.inventoryScannerRange; i++)
			{
				if(BlockUtils.getBlock(world, pos.north(i)) == SCContent.inventoryScanner)
				{
					for(int j = 1; j < i; j++)
					{
						world.destroyBlock(pos.north(j), false);
					}

					break;
				}
			}

			for(int i = 0; i < ConfigHandler.inventoryScannerRange; i++)
			{
				if(BlockUtils.getBlock(world, pos.south(i)) == SCContent.inventoryScanner)
				{
					for(int j = 1; j < i; j++)
					{
						world.destroyBlock(pos.south(j), false);
					}

					break;
				}
			}
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
	{
		if (source.getBlockState(pos).getValue(FACING) == EnumFacing.EAST || source.getBlockState(pos).getValue(FACING) == EnumFacing.WEST)
			return new AxisAlignedBB(0.000F, 0.000F, 0.400F, 1.000F, 1.000F, 0.600F); //ew
		else if (source.getBlockState(pos).getValue(FACING) == EnumFacing.NORTH || source.getBlockState(pos).getValue(FACING) == EnumFacing.SOUTH)
			return new AxisAlignedBB(0.400F, 0.000F, 0.000F, 0.600F, 1.000F, 1.000F); //ns
		return state.getBoundingBox(source, pos);
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		if(EnumFacing.values()[meta] == EnumFacing.DOWN || EnumFacing.values()[meta] == EnumFacing.UP)
			return getDefaultState();
		return getDefaultState().withProperty(FACING, EnumFacing.values()[meta]);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(FACING).getIndex();
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] {FACING});
	}

	@SideOnly(Side.CLIENT)

	/**
	 * only called by clickMiddleMouseButton , and passed to inventory.setCurrentItem (along with isCreative)
	 */
	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntitySCTE().intersectsEntities();
	}

}
