package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IIntersectable;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.tileentity.TileEntityCageTrap;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockCageTrap extends BlockOwnable implements IIntersectable {

	public static final BooleanProperty DEACTIVATED = BooleanProperty.create("deactivated");

	public BlockCageTrap(Material material) {
		super(Block.Properties.create(material).hardnessAndResistance(-1.0F, 6000000.0F).doesNotBlockMovement());
		setDefaultState(stateContainer.getBaseState().with(DEACTIVATED, false));
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public VoxelShape getCollisionShape(IBlockState state, IBlockReader world, BlockPos pos){
		return state.get(DEACTIVATED) ? VoxelShapes.fullCube() : VoxelShapes.empty();
	}

	@Override
	public void onEntityIntersected(World world, BlockPos pos, Entity entity) {
		if(!world.isRemote){
			TileEntityCageTrap tileEntity = (TileEntityCageTrap) world.getTileEntity(pos);
			boolean isPlayer = entity instanceof EntityPlayer;
			boolean shouldCaptureMobs = tileEntity.getOptionByName("captureMobs").asBoolean();

			if(isPlayer || (entity instanceof EntityMob && shouldCaptureMobs)){
				if((isPlayer && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner((EntityPlayer)entity)))
					return;

				if(BlockUtils.getBlockPropertyAsBoolean(world, pos, DEACTIVATED))
					return;

				BlockUtils.setBlockProperty(world, pos, DEACTIVATED, true);
				BlockUtils.setBlock(world, pos.up(4), SCContent.reinforcedIronBars);
				BlockUtils.setBlock(world, pos.getX() + 1, pos.getY() + 4, pos.getZ(), SCContent.reinforcedIronBars);
				BlockUtils.setBlock(world, pos.getX() - 1, pos.getY() + 4, pos.getZ(), SCContent.reinforcedIronBars);
				BlockUtils.setBlock(world, pos.getX(), pos.getY() + 4, pos.getZ() + 1, SCContent.reinforcedIronBars);
				BlockUtils.setBlock(world, pos.getX(), pos.getY() + 4, pos.getZ() - 1, SCContent.reinforcedIronBars);

				BlockUtils.setBlockInBox(world, pos.getX(), pos.getY(), pos.getZ(), SCContent.reinforcedIronBars);
				setTileEntities(world, pos.getX(), pos.getY(), pos.getZ(), ((IOwnable)world.getTileEntity(pos)).getOwner().getUUID(), ((IOwnable)world.getTileEntity(pos)).getOwner().getName());

				world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 3.0F, 1.0F);

				if(isPlayer)
					entity.sendMessage(new TextComponentTranslation("["+ TextFormatting.BLACK + ClientUtils.localize(SCContent.cageTrap.getTranslationKey()) + TextFormatting.RESET + "] " + ClientUtils.localize("messages.securitycraft:cageTrap.captured").replace("#player", ((EntityPlayer) entity).getName().getFormattedText()).replace("#location", Utils.getFormattedCoordinates(pos))));
			}
		}
	}

	@Override
	public IBlockState getStateForPlacement(BlockItemUseContext ctx)
	{
		return getStateForPlacement(ctx.getWorld(), ctx.getPos(), ctx.getFace(), ctx.getHitX(), ctx.getHitY(), ctx.getHitZ(), ctx.getPlayer());
	}

	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, EntityPlayer placer)
	{
		return getDefaultState().with(DEACTIVATED, false);
	}

	@Override
	protected void fillStateContainer(Builder<Block, IBlockState> builder)
	{
		builder.add(DEACTIVATED);
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader world) {
		return new TileEntityCageTrap().intersectsEntities();
	}

	public void setTileEntities(World world, int x, int y, int z, String uuid, String name)
	{
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x, y, z))).getOwner().set(uuid, name);

		((IOwnable)world.getTileEntity(BlockUtils.toPos(x, y + 4, z))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x + 1, y + 4, z))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x - 1, y + 4, z))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x, y + 4, z + 1))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x, y + 4, z - 1))).getOwner().set(uuid, name);

		((IOwnable)world.getTileEntity(BlockUtils.toPos(x + 1, y + 1, z))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x + 1, y + 2, z))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x + 1, y + 3, z))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x + 1, y + 1, z + 1))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x + 1, y + 2, z + 1))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x + 1, y + 3, z + 1))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x - 1, y + 1, z))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x - 1, y + 2, z))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x - 1, y + 3, z))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x - 1, y + 1, z + 1))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x - 1, y + 2, z + 1))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x - 1, y + 3, z + 1))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x, y + 1, z + 1))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x, y + 2, z + 1))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x, y + 3, z + 1))).getOwner().set(uuid, name);

		((IOwnable)world.getTileEntity(BlockUtils.toPos(x + 1, y + 1, z))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x + 1, y + 2, z))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x + 1, y + 3, z))).getOwner().set(uuid, name);

		((IOwnable)world.getTileEntity(BlockUtils.toPos(x, y + 1, z - 1))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x, y + 2, z - 1))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x, y + 3, z - 1))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x + 1, y + 1, z - 1))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x + 1, y + 2, z - 1))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x + 1, y + 3, z - 1))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x - 1, y + 1, z - 1))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x - 1, y + 2, z - 1))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x - 1, y + 3, z - 1))).getOwner().set(uuid, name);

		((IOwnable)world.getTileEntity(BlockUtils.toPos(x + 1, y + 4, z + 1))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x + 1, y + 4, z - 1))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x - 1, y + 4, z + 1))).getOwner().set(uuid, name);
		((IOwnable)world.getTileEntity(BlockUtils.toPos(x - 1, y + 4, z - 1))).getOwner().set(uuid, name);
	}
}
