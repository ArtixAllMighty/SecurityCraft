package net.geforcemods.securitycraft.entity;

import java.util.List;
import java.util.Random;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.entity.ai.EntityAIAttackRangedIfEnabled;
import net.geforcemods.securitycraft.entity.ai.EntityAITargetNearestPlayerOrMob;
import net.geforcemods.securitycraft.items.ItemModule;
import net.geforcemods.securitycraft.network.client.InitSentryAnimation;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.geforcemods.securitycraft.util.ModuleUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

public class EntitySentry extends CreatureEntity implements IRangedAttackMob //needs to be a creature so it can target a player, ai is also only given to living entities
{
	private static final DataParameter<Owner> OWNER = EntityDataManager.<Owner>createKey(EntitySentry.class, Owner.SERIALIZER);
	private static final DataParameter<CompoundNBT> MODULE = EntityDataManager.<CompoundNBT>createKey(EntitySentry.class, DataSerializers.COMPOUND_NBT);
	private static final DataParameter<CompoundNBT> WHITELIST = EntityDataManager.<CompoundNBT>createKey(EntitySentry.class, DataSerializers.COMPOUND_NBT);
	private static final DataParameter<Integer> MODE = EntityDataManager.<Integer>createKey(EntitySentry.class, DataSerializers.VARINT);
	public static final DataParameter<Float> HEAD_ROTATION = EntityDataManager.<Float>createKey(EntitySentry.class, DataSerializers.FLOAT);
	public static final float MAX_TARGET_DISTANCE = 20.0F;
	private float headYTranslation = 0.9F;
	private final float animationStepSize = 0.025F;
	public boolean animateUpwards = true;
	public boolean animate = false;
	private long previousTargetId = Long.MIN_VALUE;

	public EntitySentry(EntityType<EntitySentry> type, World world)
	{
		super(SCContent.eTypeSentry, world);
	}

	public void setupSentry(PlayerEntity owner)
	{
		dataManager.set(OWNER, new Owner(owner.getName().getFormattedText(), PlayerEntity.getUUID(owner.getGameProfile()).toString()));
		dataManager.set(MODULE, new CompoundNBT());
		dataManager.set(WHITELIST, new CompoundNBT());
		dataManager.set(MODE, EnumSentryMode.CAMOUFLAGE.ordinal());
		dataManager.set(HEAD_ROTATION, 0.0F);
	}

	@Override
	protected void registerData()
	{
		super.registerData();
		dataManager.register(OWNER, new Owner());
		dataManager.register(MODULE, new CompoundNBT());
		dataManager.register(WHITELIST, new CompoundNBT());
		dataManager.register(MODE, EnumSentryMode.CAMOUFLAGE.ordinal());
		dataManager.register(HEAD_ROTATION, 0.0F);
	}

	@Override
	protected void registerGoals()
	{
		goalSelector.addGoal(1, new EntityAIAttackRangedIfEnabled(this, 0.0F, 5, 10.0F));
		targetSelector.addGoal(1, new EntityAITargetNearestPlayerOrMob(this));
	}

	@Override
	public void tick()
	{
		super.tick();

		if(world.isRemote && animate)
		{
			if(animateUpwards && headYTranslation > 0.0F)
			{
				headYTranslation -= animationStepSize;

				if(headYTranslation <= 0.0F)
				{
					animateUpwards = false;
					animate = false;
				}
			}
			else if(!animateUpwards && headYTranslation < 0.9F)
			{
				headYTranslation += animationStepSize;

				if(headYTranslation >= 0.9F)
				{
					animateUpwards = true;
					animate = false;
				}
			}
		}
	}

	@Override
	public boolean processInteract(PlayerEntity player, Hand hand)
	{
		if(getOwner().isOwner(player) && hand == Hand.MAIN_HAND)
		{
			player.closeScreen();

			if(player.isSneaking())
				remove();
			else if(player.getHeldItemMainhand().getItem() == SCContent.disguiseModule)
			{
				ItemStack module = getDisguiseModule();

				if(!module.isEmpty()) //drop the old module as to not override it with the new one
					Block.spawnAsEntity(world, getPosition(), module);

				setDisguiseModule(player.getHeldItemMainhand());

				if(!player.isCreative())
					player.setItemStackToSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
			}
			else if(player.getHeldItemMainhand().getItem() == SCContent.whitelistModule)
			{
				ItemStack module = getWhitelistModule();

				if(!module.isEmpty()) //drop the old module as to not override it with the new one
					Block.spawnAsEntity(world, getPosition(), module);

				setWhitelistModule(player.getHeldItemMainhand());

				if(!player.isCreative())
					player.setItemStackToSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
			}
			else if(player.getHeldItemMainhand().getItem() == SCContent.universalBlockModifier)
			{
				world.destroyBlock(getPosition(), false);
				Block.spawnAsEntity(world, getPosition(), getDisguiseModule());
				Block.spawnAsEntity(world, getPosition(), getWhitelistModule());
				dataManager.set(MODULE, new CompoundNBT());
				dataManager.set(WHITELIST, new CompoundNBT());
			}
			else
				toggleMode(player);

			player.swingArm(Hand.MAIN_HAND);
			return true;
		}

		return super.processInteract(player, hand);
	}

	/**
	 * Cleanly removes this sentry from the world, dropping the module and removing any block
	 */
	@Override
	public void remove()
	{
		super.remove();
		Block.spawnAsEntity(world, getPosition(), new ItemStack(SCContent.sentry));
		Block.spawnAsEntity(world, getPosition(), getDisguiseModule()); //if there is none, nothing will drop
		Block.spawnAsEntity(world, getPosition(), getWhitelistModule()); //if there is none, nothing will drop
		world.setBlockState(getPosition(), Blocks.AIR.getDefaultState());
	}

	@Override
	public void onKillCommand()
	{
		remove();
	}

	/**
	 * Sets this sentry's mode to the next one and sends the player a message about the switch
	 * @param player The player to send the message to
	 */
	public void toggleMode(PlayerEntity player)
	{
		int mode = dataManager.get(MODE) + 1;

		if(mode == 3)
			mode = 0;

		dataManager.set(MODE, mode);

		if(player.world.isRemote)
		{
			if(mode == 0)
			{
				animateUpwards = true;
				animate = true;
			}
			else if(mode == 1)
			{
				animateUpwards = false;
				animate = true;
			}

			PlayerUtils.sendMessageToPlayer(player, ClientUtils.localize(SCContent.sentry.getTranslationKey()), ClientUtils.localize("messages.securitycraft:sentry.mode" + (mode + 1)), TextFormatting.DARK_RED);
		}
	}

	@Override
	public void setAttackTarget(LivingEntity target)
	{
		if(getMode() != EnumSentryMode.AGGRESSIVE && (target == null && previousTargetId != Long.MIN_VALUE || (target != null && previousTargetId != target.getEntityId())))
		{
			animateUpwards = getMode() == EnumSentryMode.CAMOUFLAGE && target != null;
			animate = true;
			SecurityCraft.channel.send(PacketDistributor.ALL.noArg(), new InitSentryAnimation(getPosition(), animate, animateUpwards));
		}

		previousTargetId = target == null ? Long.MIN_VALUE : target.getEntityId();
		super.setAttackTarget(target);
	}

	@Override
	public float getEyeHeight(Pose pose) //the sentry's eyes are higher so that it can see players even if it's inside a block when disguised - this also makes bullets spawn higher
	{
		return 1.5F;
	}

	@Override
	public void attackEntityWithRangedAttack(LivingEntity target, float distanceFactor)
	{
		//don't shoot if somehow a non player is a target, or if the player is in spectator or creative mode
		if(target instanceof PlayerEntity && (((PlayerEntity)target).isSpectator() || ((PlayerEntity)target).isCreative()))
			return;

		//also don't shoot if the target is too far away
		if(getDistanceSq(target) > MAX_TARGET_DISTANCE * MAX_TARGET_DISTANCE)
			return;

		EntityBullet throwableEntity = new EntityBullet(world, this);
		double y = target.posY + target.getEyeHeight() - 1.100000023841858D;
		double x = target.posX - posX;
		double d2 = y - throwableEntity.posY;
		double z = target.posZ - posZ;
		float f = MathHelper.sqrt(x * x + z * z) * 0.2F;

		throwableEntity.posY -= 0.1F;
		dataManager.set(HEAD_ROTATION, (float)(MathHelper.atan2(x, -z) * (180D / Math.PI)));
		throwableEntity.shoot(x, d2 + f, z, 1.6F, 0.0F); //no inaccuracy for sentries!
		playSound(SoundEvents.ENTITY_ARROW_SHOOT, 1.0F, 1.0F / (getRNG().nextFloat() * 0.4F + 0.8F));
		world.addEntity(throwableEntity);
	}

	@Override
	public void writeAdditional(CompoundNBT tag)
	{
		tag.put("TileEntityData", getOwnerTag());
		tag.put("InstalledModule", getDisguiseModule().write(new CompoundNBT()));
		tag.put("InstalledWhitelist", getWhitelistModule().write(new CompoundNBT()));
		tag.putInt("SentryMode", dataManager.get(MODE));
		tag.putFloat("HeadRotation", dataManager.get(HEAD_ROTATION));
		super.writeAdditional(tag);
	}

	private CompoundNBT getOwnerTag()
	{
		CompoundNBT tag = new CompoundNBT();
		Owner owner = dataManager.get(OWNER);

		tag.putString("owner", owner.getName());
		tag.putString("ownerUUID", owner.getUUID());
		return tag;
	}

	@Override
	public void readAdditional(CompoundNBT tag)
	{
		CompoundNBT teTag = tag.getCompound("TileEntityData");
		String name = teTag.getString("owner");
		String uuid = teTag.getString("ownerUUID");

		dataManager.set(OWNER, new Owner(name, uuid));
		dataManager.set(MODULE, (CompoundNBT)tag.get("InstalledModule"));
		dataManager.set(WHITELIST, (CompoundNBT)tag.get("InstalledWhitelist"));
		dataManager.set(MODE, tag.getInt("SentryMode"));
		dataManager.set(HEAD_ROTATION, tag.getFloat("HeadRotation"));
		super.readAdditional(tag);
	}

	/**
	 * @return The owner of this sentry
	 */
	public Owner getOwner()
	{
		return dataManager.get(OWNER);
	}

	/**
	 * Sets the sentry's disguise module and places a block if possible
	 * @param module The module to set
	 */
	public void setDisguiseModule(ItemStack module)
	{
		List<ItemStack> blocks = ((ItemModule)module.getItem()).getAddons(module.getTag());

		if(blocks.size() > 0)
		{
			ItemStack disguiseStack = blocks.get(0);
			BlockState state = Block.getBlockFromItem(disguiseStack.getItem()).getDefaultState();

			world.setBlockState(getPosition(), state.getShape(world, getPosition()) == VoxelShapes.fullCube() ? state : Blocks.AIR.getDefaultState());
		}

		dataManager.set(MODULE, module.write(new CompoundNBT()));
	}

	/**
	 * Sets the sentry's whitelist module
	 * @param module The module to set
	 */
	public void setWhitelistModule(ItemStack module)
	{
		dataManager.set(WHITELIST, module.write(new CompoundNBT()));
	}

	/**
	 * @return The disguise module that is added to this sentry. ItemStack.EMPTY if none available
	 */
	public ItemStack getDisguiseModule()
	{
		CompoundNBT tag = dataManager.get(MODULE);

		if(tag == null || tag.isEmpty())
			return ItemStack.EMPTY;
		else
			return ItemStack.read(tag);
	}

	/**
	 * @return The whitelist module that is added to this sentry. ItemStack.EMPTY if none available
	 */
	public ItemStack getWhitelistModule()
	{
		CompoundNBT tag = dataManager.get(WHITELIST);

		if(tag == null || tag.isEmpty())
			return ItemStack.EMPTY;
		else
			return ItemStack.read(tag);
	}

	/**
	 * @return The mode in which the sentry is currently in, CAMOUFLAGE if the saved mode is smaller than 0 or greater than 2 (there are only 3 valid modes: 0, 1, 2)
	 */
	public EnumSentryMode getMode()
	{
		int mode = dataManager.get(MODE);

		return mode < 0 || mode > 2 ? EnumSentryMode.CAMOUFLAGE : EnumSentryMode.values()[mode];
	}

	/**
	 * @return The amount of y translation from the head's default position, used for animation
	 */
	public float getHeadYTranslation()
	{
		return headYTranslation;
	}

	public boolean isTargetingWhitelistedPlayer(LivingEntity potentialTarget)
	{
		List<String> players = ModuleUtils.getPlayersFromModule(getWhitelistModule());

		for(String s : players)
		{
			if(potentialTarget == PlayerUtils.getPlayerFromName(s))
				return true;
		}

		return false;
	}

	//start: disallow sentry to take damage
	@Override
	public boolean attackEntityAsMob(Entity entity)
	{
		return false;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount)
	{
		return false;
	}

	@Override
	public boolean canBeAttackedWithItem()
	{
		return false;
	}

	@Override
	public boolean attackable()
	{
		return false;
	}
	//end: disallow sentry to take damage

	@Override
	public boolean canSpawn(IWorld world, SpawnReason reason)
	{
		return false;
	}

	@Override
	public void jump() {} //sentries don't jump!

	@Override
	public boolean hasPath()
	{
		return false;
	}

	@Override
	protected void checkDespawn() {} //sentries don't despawn

	@Override
	public boolean canDespawn(double distanceClosestToPlayer)
	{
		return false; //sentries don't despawn
	}

	//sentries are heavy, so don't push them around!
	@Override
	public void onCollideWithPlayer(PlayerEntity entity) {}

	@Override
	public void move(MoverType type, Vec3d vec) {} //no moving sentries!

	@Override
	protected void collideWithEntity(Entity entity) {}

	@Override
	protected void collideWithNearbyEntities() {}

	@Override
	public boolean isImmuneToExplosions()
	{
		return true; //does not get pushed around by explosions
	}

	@Override
	public boolean canBeCollidedWith()
	{
		return true; //needs to stay true so blocks can't be broken through the sentry
	}

	@Override
	public boolean canBePushed()
	{
		return false;
	}

	@Override
	public PushReaction getPushReaction()
	{
		return PushReaction.IGNORE;
	}

	@Override
	public void updateLeashedState() {} //no leashing for sentry

	//this last code is here so the ai task gets executed, which it doesn't for some weird reason
	@Override
	public Random getRNG()
	{
		return notRandom;
	}

	@Override
	public IPacket<?> createSpawnPacket()
	{
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	private static Random notRandom = new NotRandom();

	private static class NotRandom extends Random
	{
		@Override
		public int nextInt(int bound)
		{
			return 0;
		}
	}

	public static enum EnumSentryMode
	{
		AGGRESSIVE, CAMOUFLAGE, IDLE;
	}
}
