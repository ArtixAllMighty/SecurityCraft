package net.geforcemods.securitycraft.tileentity;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.CustomizableSCTE;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.OptionDouble;
import net.geforcemods.securitycraft.blocks.BlockMotionActivatedLight;
import net.geforcemods.securitycraft.misc.EnumCustomModules;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class TileEntityMotionLight extends CustomizableSCTE {

	private OptionDouble searchRadiusOption = new OptionDouble("searchRadius", ConfigHandler.motionActivatedLightSearchRadius, 5.0D, 20.0D, 5.0D);

	@Override
	public boolean attackEntity(Entity entity) {
		if(entity instanceof EntityPlayer && PlayerUtils.isPlayerMountedOnCamera((EntityPlayer)entity))
			BlockMotionActivatedLight.toggleLight(world, pos, searchRadiusOption.asDouble(), getOwner(), false);

		if(entity instanceof EntityLivingBase && BlockUtils.getBlock(getWorld(), pos) == SCContent.motionActivatedLight && !BlockUtils.getBlockPropertyAsBoolean(getWorld(), getPos(), BlockMotionActivatedLight.LIT))
			BlockMotionActivatedLight.toggleLight(world, pos, searchRadiusOption.asDouble(), getOwner(), true);

		return false;
	}

	@Override
	public void attackFailed() {
		if(BlockUtils.getBlock(getWorld(), pos) == SCContent.motionActivatedLight && BlockUtils.getBlockPropertyAsBoolean(getWorld(), getPos(), BlockMotionActivatedLight.LIT))
			BlockMotionActivatedLight.toggleLight(world, pos, searchRadiusOption.asDouble(), getOwner(), false);
	}

	@Override
	public boolean canAttack() {
		return true;
	}

	@Override
	public boolean shouldSyncToClient() {
		return false;
	}

	@Override
	public double getAttackRange() {
		return searchRadiusOption.asDouble();
	}

	@Override
	public EnumCustomModules[] acceptedModules() {
		return new EnumCustomModules[] {};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option<?>[] {searchRadiusOption};
	}

	@Override
	public String getName() {
		return "Motion-Activated Light";
	}

}
