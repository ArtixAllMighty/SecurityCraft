package net.geforcemods.securitycraft;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.geforcemods.securitycraft.ConfigHandler.CommonConfig;
import net.geforcemods.securitycraft.api.CustomizableSCTE;
import net.geforcemods.securitycraft.api.EnumLinkedAction;
import net.geforcemods.securitycraft.api.INameable;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.IPasswordProtected;
import net.geforcemods.securitycraft.blocks.BlockCageTrap;
import net.geforcemods.securitycraft.blocks.BlockLaserBlock;
import net.geforcemods.securitycraft.blocks.BlockOwnable;
import net.geforcemods.securitycraft.blocks.BlockSecurityCamera;
import net.geforcemods.securitycraft.blocks.reinforced.IReinforcedBlock;
import net.geforcemods.securitycraft.containers.ContainerCustomizeBlock;
import net.geforcemods.securitycraft.entity.EntitySecurityCamera;
import net.geforcemods.securitycraft.entity.EntitySentry;
import net.geforcemods.securitycraft.items.ItemModule;
import net.geforcemods.securitycraft.misc.CustomDamageSources;
import net.geforcemods.securitycraft.misc.EnumCustomModules;
import net.geforcemods.securitycraft.misc.OwnershipEvent;
import net.geforcemods.securitycraft.misc.PortalSize;
import net.geforcemods.securitycraft.misc.SCSounds;
import net.geforcemods.securitycraft.network.client.PlaySoundAtPos;
import net.geforcemods.securitycraft.tileentity.TileEntityOwnable;
import net.geforcemods.securitycraft.tileentity.TileEntitySecurityCamera;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

@EventBusSubscriber(modid=SecurityCraft.MODID)
public class SCEventHandler {

	public static HashMap<String, String> tipsWithLink = new HashMap<String, String>();

	static
	{
		tipsWithLink.put("trello", "https://trello.com/b/dbCNZwx0/securitycraft");
		tipsWithLink.put("patreon", "https://www.patreon.com/Geforce");
		tipsWithLink.put("discord", "https://discord.gg/U8DvBAW");
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerLoggedInEvent event){
		if(!event.getPlayer().getEntityWorld().isRemote || !CommonConfig.CONFIG.sayThanksMessage.get())
			return;

		String tipKey = getRandomTip();
		ITextComponent message;

		if(tipsWithLink.containsKey(tipKey.split("\\.")[2]))
			message = new StringTextComponent("[" + TextFormatting.GOLD + "SecurityCraft" + TextFormatting.WHITE + "] " + ClientUtils.localize("messages.securitycraft:thanks").replace("#", SecurityCraft.getVersion()) + " " + ClientUtils.localize("messages.securitycraft:tip") + " " + ClientUtils.localize(tipKey) + " ").appendSibling(ForgeHooks.newChatWithLinks(tipsWithLink.get(tipKey.split("\\.")[2])));
		else
			message = new StringTextComponent("[" + TextFormatting.GOLD + "SecurityCraft" + TextFormatting.WHITE + "] " + ClientUtils.localize("messages.securitycraft:thanks").replace("#", SecurityCraft.getVersion()) + " " + ClientUtils.localize("messages.securitycraft:tip") + " " + ClientUtils.localize(tipKey));

		event.getPlayer().sendMessage(message);
	}

	@SubscribeEvent
	public static void onPlayerLoggedOut(PlayerLoggedOutEvent event)
	{
		if(PlayerUtils.isPlayerMountedOnCamera(event.getPlayer()) && event.getPlayer().getRidingEntity() instanceof EntitySecurityCamera)
			event.getPlayer().getRidingEntity().remove();
	}

	@SubscribeEvent
	public static void onDamageTaken(LivingHurtEvent event)
	{
		if(event.getEntity() != null && PlayerUtils.isPlayerMountedOnCamera(event.getEntityLiving())){
			event.setCanceled(true);
			return;
		}

		if(event.getSource() == CustomDamageSources.electricity)
			SecurityCraft.channel.send(PacketDistributor.ALL.noArg(), new PlaySoundAtPos(event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ, SCSounds.ELECTRIFIED.path, 0.25F, "blocks"));
	}

	@SubscribeEvent
	public static void onBucketUsed(FillBucketEvent event){
		if(event.getTarget() == null || event.getTarget().getType() == Type.BLOCK)
			return;

		ItemStack result = fillBucket(event.getWorld(), ((BlockRayTraceResult)event.getTarget()).getPos());
		if(result.isEmpty())
			return;
		event.setFilledBucket(result);
		event.setResult(Result.ALLOW);
	}

	@SubscribeEvent
	public static void onRightClickBlock(RightClickBlock event){
		if(event.getHand() == Hand.MAIN_HAND)
		{
			World world = event.getWorld();

			if(!world.isRemote){
				TileEntity tileEntity = world.getTileEntity(event.getPos());
				BlockState state  = world.getBlockState(event.getPos());
				Block block = state.getBlock();

				if(PlayerUtils.isHoldingItem(event.getEntityPlayer(), SCContent.codebreaker) && handleCodebreaking(event)) {
					event.setCanceled(true);
					return;
				}

				if(tileEntity != null && tileEntity instanceof CustomizableSCTE && PlayerUtils.isHoldingItem(event.getEntityPlayer(), SCContent.universalBlockModifier)){
					event.setCanceled(true);

					if(!((IOwnable) tileEntity).getOwner().isOwner(event.getEntityPlayer())){
						PlayerUtils.sendMessageToPlayer(event.getEntityPlayer(), ClientUtils.localize(SCContent.universalBlockModifier.getTranslationKey()), ClientUtils.localize("messages.securitycraft:notOwned").replace("#", ((IOwnable) tileEntity).getOwner().getName()), TextFormatting.RED);
						return;
					}

					if(event.getEntityPlayer() instanceof ServerPlayerEntity)
					{
						NetworkHooks.openGui((ServerPlayerEntity)event.getEntityPlayer(), new INamedContainerProvider() {
							@Override
							public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity player)
							{
								return new ContainerCustomizeBlock(windowId, world, event.getPos(), inv);
							}

							@Override
							public ITextComponent getDisplayName()
							{
								return new TranslationTextComponent(tileEntity.getBlockState().getBlock().getTranslationKey());
							}
						}, event.getPos());
					}

					return;
				}

				if(tileEntity instanceof INameable && ((INameable) tileEntity).canBeNamed() && PlayerUtils.isHoldingItem(event.getEntityPlayer(), Items.NAME_TAG) && event.getEntityPlayer().inventory.getCurrentItem().hasDisplayName()){
					event.setCanceled(true);

					for(String character : new String[]{"(", ")"})
						if(event.getEntityPlayer().inventory.getCurrentItem().getDisplayName().getFormattedText().contains(character)) {
							PlayerUtils.sendMessageToPlayer(event.getEntityPlayer(), "Naming", ClientUtils.localize("messages.securitycraft:naming.error").replace("#n", event.getEntityPlayer().inventory.getCurrentItem().getDisplayName().getFormattedText()).replace("#c", character), TextFormatting.RED);
							return;
						}

					if(((INameable) tileEntity).getCustomSCName().equals(event.getEntityPlayer().inventory.getCurrentItem().getDisplayName())) {
						PlayerUtils.sendMessageToPlayer(event.getEntityPlayer(), "Naming", ClientUtils.localize("messages.securitycraft:naming.alreadyMatches").replace("#n", ((INameable) tileEntity).getCustomSCName().getFormattedText()), TextFormatting.RED);
						return;
					}

					if(!event.getEntityPlayer().isCreative())
						event.getEntityPlayer().inventory.getCurrentItem().shrink(1);

					((INameable) tileEntity).setCustomSCName(event.getEntityPlayer().inventory.getCurrentItem().getDisplayName());
					return;
				}

				if(tileEntity != null && isOwnableBlock(block, tileEntity) && PlayerUtils.isHoldingItem(event.getEntityPlayer(), SCContent.universalBlockRemover)){
					event.setCanceled(true);

					if(!((IOwnable) tileEntity).getOwner().isOwner(event.getEntityPlayer())){
						PlayerUtils.sendMessageToPlayer(event.getEntityPlayer(), ClientUtils.localize(SCContent.universalBlockRemover.getTranslationKey()), ClientUtils.localize("messages.securitycraft:notOwned").replace("#", ((IOwnable) tileEntity).getOwner().getName()), TextFormatting.RED);
						return;
					}

					if(block == SCContent.laserBlock){
						world.destroyBlock(event.getPos(), true);
						BlockLaserBlock.destroyAdjacentLasers(event.getWorld(), event.getPos());
						event.getEntityPlayer().inventory.getCurrentItem().damageItem(1, event.getEntityPlayer(), p -> {});
					}else if(block == SCContent.cageTrap && world.getBlockState(event.getPos()).get(BlockCageTrap.DEACTIVATED)) {
						BlockPos originalPos = event.getPos();
						BlockPos pos = originalPos.east().up();

						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.east().up(2);
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.east().up(3);
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.east().up().south();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.east().up(2).south();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.east().up(3).south();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.west().up();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.west().up(2);
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.west().up(3);
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.west().up().south();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.west().up(2).south();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.west().up(3).south();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.up().south();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.up(2).south();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.up(3).south();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.east().up();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.east().up(2);
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.east().up(3);
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.up().north();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.up(2).north();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.up(3).north();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.east().up().north();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.east().up(2).north();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.east().up(3).north();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.west().up().north();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.west().up(2).north();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.west().up(3).north();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.east().up(4).south();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.east().up(4).north();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.west().up(4).south();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.west().up(4).north();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.west().up(4).north();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.east().up(4);
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.west().up(4);
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.up(4).south();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.up(4).north();
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						pos = originalPos.up(4);
						if(BlockUtils.getBlock(world, pos) == SCContent.reinforcedIronBars && ((IOwnable)world.getTileEntity(pos)).getOwner().isOwner(event.getEntityPlayer()))
							BlockUtils.destroyBlock(world, pos, false);

						BlockUtils.destroyBlock(world, originalPos, false);
					}else if(block instanceof DoorBlock){
						BlockPos pos = event.getPos();

						if(state.get(DoorBlock.HALF) == DoubleBlockHalf.UPPER)
							pos = pos.down();

						world.destroyBlock(pos, true);
						world.removeTileEntity(pos);
						event.getEntityPlayer().inventory.getCurrentItem().damageItem(1, event.getEntityPlayer(), p -> {});

					}else{
						world.destroyBlock(event.getPos(), true);
						world.removeTileEntity(event.getPos());
						event.getEntityPlayer().inventory.getCurrentItem().damageItem(1, event.getEntityPlayer(), p -> {});
					}

					return;
				}
			}

			//outside !world.isRemote for properly checking the interaction
			//all the sentry functionality for when the sentry is diguised
			List<EntitySentry> sentries = world.getEntitiesWithinAABB(EntitySentry.class, new AxisAlignedBB(event.getPos()));

			if(!sentries.isEmpty())
				event.setCanceled(sentries.get(0).processInteract(event.getEntityPlayer(), event.getHand())); //cancel if an action was taken
		}
	}

	@SubscribeEvent
	public static void onBlockEventBreak(BlockEvent.BreakEvent event)
	{
		if(!(event.getWorld() instanceof World))
			return;

		List<EntitySentry> sentries = ((World)event.getWorld()).getEntitiesWithinAABB(EntitySentry.class, new AxisAlignedBB(event.getPos()));

		//don't let people break the disguise block
		if(!sentries.isEmpty())
		{
			event.setCanceled(true);
			return;
		}

		sentries = ((World)event.getWorld()).getEntitiesWithinAABB(EntitySentry.class, new AxisAlignedBB(event.getPos().up()));

		//remove sentry if block below is broken
		if(!sentries.isEmpty())
			sentries.get(0).remove();
	}

	@SubscribeEvent
	public static void onOwnership(OwnershipEvent event)
	{
		handleOwnableTEs(event);
	}

	//	@SubscribeEvent
	//	public static void onBlockPlaced(PlaceEvent event) {
	//		//reinforced obsidian portal handling
	//		if(event.getState().getBlock() == Blocks.FIRE && event.getWorld().getBlockState(event.getPos().down()).getBlock() == SCContent.reinforcedObsidian)
	//		{
	//			PortalSize portalSize = new PortalSize(event.getWorld(), event.getPos(), Direction.Axis.X);
	//
	//			if (portalSize.isValid() && portalSize.getNetherPortalBlockCount() == 0)
	//				portalSize.placeNetherPortalBlocks();
	//			else
	//			{
	//				portalSize = new PortalSize(event.getWorld(), event.getPos(), Direction.Axis.Z);
	//
	//				if (portalSize.isValid() && portalSize.getNetherPortalBlockCount() == 0)
	//					portalSize.placeNetherPortalBlocks();
	//			}
	//		}
	//	}
	//
	//	@SubscribeEvent
	//	public static void onEntityJoinWorld(EntityJoinWorldEvent event)
	//	{
	//		//fix for spawning under the portal
	//		if(event.getEntity() instanceof PlayerEntity && !event.getWorld().isRemote) //nether
	//		{
	//			BlockPos pos = event.getEntity().getPosition();
	//
	//			//check for obsidian or reinforced obsidian from the player's position up to the world height
	//			do
	//			{
	//				if(event.getWorld().getBlockState(pos).getBlock() == Blocks.OBSIDIAN)
	//				{
	//					//check if the block is part of a valid portal, and if so move the entity down
	//					NetherPortalBlock.Size portalSize = new NetherPortalBlock.Size(event.getWorld(), pos, Direction.Axis.X);
	//
	//					if (portalSize.isValid())
	//					{
	//						double y = pos.getY() + 0.5D;
	//
	//						if(event.getWorld().getBlockState(pos.down()).getBlock() == Blocks.NETHER_PORTAL) //sometimes the top of the portal is more valid than the bottom o.O
	//							y -= 3.0D;
	//
	//						event.getEntity().setPosition(pos.getX() + 0.5D, y, pos.getZ() + 0.5D);
	//						break;
	//					}
	//					else //check other axis
	//					{
	//						portalSize = new NetherPortalBlock.Size(event.getWorld(), pos, Direction.Axis.Z);
	//
	//						if (portalSize.isValid())
	//						{
	//							double y = pos.getY() + 0.5D;
	//
	//							if(event.getWorld().getBlockState(pos.down()).getBlock() == Blocks.NETHER_PORTAL)
	//								y -= 3.0D;
	//
	//							event.getEntity().setPosition(pos.getX() + 0.5D, y, pos.getZ() + 0.5D);
	//							break;
	//						}
	//					}
	//				}
	//				else if(event.getWorld().getBlockState(pos).getBlock() == SCContent.reinforcedObsidian) //analogous to if check above
	//				{
	//					PortalSize portalSize = new PortalSize(event.getWorld(), pos, Direction.Axis.X);
	//
	//					if (portalSize.isValid())
	//					{
	//						double y = pos.getY() + 0.5D;
	//
	//						if(event.getWorld().getBlockState(pos.down()).getBlock() == Blocks.NETHER_PORTAL)
	//							y -= 3.0D;
	//
	//						event.getEntity().setPosition(pos.getX() + 0.5D, y, pos.getZ() + 0.5D);
	//						break;
	//					}
	//					else
	//					{
	//						portalSize = new PortalSize(event.getWorld(), pos, Direction.Axis.Z);
	//
	//						if (portalSize.isValid())
	//						{
	//							double y = pos.getY() + 0.5D;
	//
	//							if(event.getWorld().getBlockState(pos.down()).getBlock() == Blocks.NETHER_PORTAL)
	//								y -= 3.0D;
	//
	//							event.getEntity().setPosition(pos.getX() + 0.5D, y, pos.getZ() + 0.5D);
	//							break;
	//						}
	//					}
	//				}
	//			}
	//			while((pos = pos.up()).getY() < event.getWorld().getHeight());
	//
	//		}
	//	}

	@SubscribeEvent
	public static void onNeighborNotify(NeighborNotifyEvent event)
	{
		//prevent portal blocks from disappearing because they think they're not inside of a proper portal frame
		if(event.getState().getBlock() == Blocks.NETHER_PORTAL)
		{
			Direction.Axis axis = event.getState().get(NetherPortalBlock.AXIS);

			if (axis == Direction.Axis.X)
			{
				PortalSize portalSize = new PortalSize(event.getWorld(), event.getPos(), Direction.Axis.X);

				if (portalSize.isValid() || portalSize.getNetherPortalBlockCount() > portalSize.getWidth() * portalSize.getHeight())
					event.setCanceled(true);
			}
			else if (axis == Direction.Axis.Z)
			{
				PortalSize portalSize = new PortalSize(event.getWorld(), event.getPos(), Direction.Axis.Z);

				if (portalSize.isValid() || portalSize.getNetherPortalBlockCount() > portalSize.getWidth() * portalSize.getHeight())
					event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onBlockBroken(BreakEvent event){
		if(event.getWorld() instanceof World && !event.getWorld().isRemote())
			if(event.getWorld().getTileEntity(event.getPos()) != null && event.getWorld().getTileEntity(event.getPos()) instanceof CustomizableSCTE){
				CustomizableSCTE te = (CustomizableSCTE) event.getWorld().getTileEntity(event.getPos());

				for(int i = 0; i < te.getNumberOfCustomizableOptions(); i++)
					if(!te.modules.get(i).isEmpty()){
						ItemStack stack = te.modules.get(i);
						ItemEntity item = new ItemEntity((World)event.getWorld(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), stack);
						WorldUtils.addScheduledTask(event.getWorld(), () -> event.getWorld().addEntity(item));

						te.onModuleRemoved(stack, ((ItemModule) stack.getItem()).getModule());
						te.createLinkedBlockAction(EnumLinkedAction.MODULE_REMOVED, new Object[]{ stack, ((ItemModule) stack.getItem()).getModule() }, te);

						if(te instanceof TileEntitySecurityCamera)
							te.getWorld().notifyNeighborsOfStateChange(te.getPos().offset(te.getWorld().getBlockState(te.getPos()).get(BlockSecurityCamera.FACING), -1), te.getWorld().getBlockState(te.getPos()).getBlock());
					}
			}
	}

	@SubscribeEvent
	public static void onLivingSetAttackTarget(LivingSetAttackTargetEvent event)
	{
		if(event.getTarget() instanceof PlayerEntity && PlayerUtils.isPlayerMountedOnCamera(event.getTarget()))
			((MobEntity)event.getEntity()).setAttackTarget(null);
		else if(event.getTarget() instanceof EntitySentry)
			((MobEntity)event.getEntity()).setAttackTarget(null);
	}

	@SubscribeEvent
	public static void onBreakSpeed(BreakSpeed event)
	{
		if(event.getEntityPlayer() != null)
		{
			Item held = event.getEntityPlayer().getHeldItemMainhand().getItem();

			if(held == SCContent.universalBlockReinforcerLvL1 || held == SCContent.universalBlockReinforcerLvL2 || held == SCContent.universalBlockReinforcerLvL3)
			{
				for(Block rb : IReinforcedBlock.BLOCKS)
				{
					IReinforcedBlock reinforcedBlock = (IReinforcedBlock)rb;

					if(reinforcedBlock.getVanillaBlock() == event.getState().getBlock())
					{
						event.setNewSpeed(10000.0F);
						return;
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onLivingDestroyEvent(LivingDestroyBlockEvent event)
	{
		event.setCanceled(event.getEntity() instanceof WitherEntity && event.getState().getBlock() instanceof IReinforcedBlock);
	}

	@SubscribeEvent
	public void onEntityMount(EntityMountEvent event)
	{
		if(event.isDismounting() && event.getEntityBeingMounted() instanceof EntitySecurityCamera && event.getEntityMounting() instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity)event.getEntityMounting();
			TileEntity te = event.getWorldObj().getTileEntity(event.getEntityBeingMounted().getPosition());

			if(PlayerUtils.isPlayerMountedOnCamera(player) && te instanceof TileEntitySecurityCamera && ((TileEntitySecurityCamera)te).hasModule(EnumCustomModules.SMART))
			{
				((TileEntitySecurityCamera)te).lastPitch = player.rotationPitch;
				((TileEntitySecurityCamera)te).lastYaw = player.rotationYaw;
			}
		}
	}

	private static ItemStack fillBucket(World world, BlockPos pos){
		Block block = world.getBlockState(pos).getBlock();

		if(block == SCContent.fakeWaterBlock){
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
			return new ItemStack(SCContent.fWaterBucket, 1);
		}else if(block == SCContent.fakeLavaBlock){
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
			return new ItemStack(SCContent.fLavaBucket, 1);
		}
		else
			return ItemStack.EMPTY;
	}

	private static void handleOwnableTEs(OwnershipEvent event) {
		if(event.getWorld().getTileEntity(event.getPos()) instanceof IOwnable) {
			String name = event.getPlayer().getName().getFormattedText();
			String uuid = event.getPlayer().getGameProfile().getId().toString();

			((IOwnable) event.getWorld().getTileEntity(event.getPos())).getOwner().set(uuid, name);
		}
	}

	private static boolean handleCodebreaking(PlayerInteractEvent event) {
		World world = event.getEntityPlayer().world;
		TileEntity tileEntity = event.getEntityPlayer().world.getTileEntity(event.getPos());

		if(CommonConfig.CONFIG.allowCodebreakerItem.get() && event.getEntityPlayer().getHeldItem(event.getHand()).getItem() == SCContent.codebreaker) //safety so when codebreakers are disabled they can't take damage
			event.getEntityPlayer().getHeldItem(event.getHand()).damageItem(1, event.getEntityPlayer(), p -> {});

		if(tileEntity != null && tileEntity instanceof IPasswordProtected && new Random().nextInt(3) == 1)
			return ((IPasswordProtected) tileEntity).onCodebreakerUsed(world.getBlockState(event.getPos()), event.getEntityPlayer(), !CommonConfig.CONFIG.allowCodebreakerItem.get());

		return false;
	}

	private static String getRandomTip(){
		String[] tips = {
				"messages.tip.scHelp",
				"messages.tip.trello",
				"messages.tip.patreon",
				"messages.tip.discord",
				"messages.tip.scserver"
		};

		return tips[new Random().nextInt(tips.length)];
	}

	private static boolean isOwnableBlock(Block block, TileEntity tileEntity){
		return (tileEntity instanceof TileEntityOwnable || tileEntity instanceof IOwnable || block instanceof BlockOwnable);
	}

}
