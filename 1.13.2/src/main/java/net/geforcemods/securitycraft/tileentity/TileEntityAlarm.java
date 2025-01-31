package net.geforcemods.securitycraft.tileentity;

import net.geforcemods.securitycraft.ConfigHandler.CommonConfig;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.blocks.BlockAlarm;
import net.geforcemods.securitycraft.misc.SCSounds;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;

public class TileEntityAlarm extends TileEntityOwnable {

	private int cooldown = 0;
	private boolean isPowered = false;

	public TileEntityAlarm()
	{
		super(SCContent.teTypeAlarm);
	}

	@Override
	public void tick(){
		if(world.isRemote)
			return;
		else{
			if(cooldown > 0){
				cooldown--;

				if(cooldown == 0)
					SecurityCraft.log("Cooldown is 0");
			}

			if(isPowered && cooldown == 0){
				TileEntityAlarm te = (TileEntityAlarm) world.getTileEntity(pos);
				getWorld().playSound(null, new BlockPos(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D), SCSounds.ALARM.event, SoundCategory.PLAYERS, CommonConfig.CONFIG.alarmSoundVolume.get().floatValue(), 1.0F);
				te.setCooldown((CommonConfig.CONFIG.alarmTickDelay.get() * 20));
				world.setBlockState(pos, world.getBlockState(pos).with(BlockAlarm.FACING, world.getBlockState(pos).get(BlockAlarm.FACING)), 2);
				world.setTileEntity(pos, te);
			}
		}
	}

	/**
	 * Writes a tile entity to NBT.
	 * @return
	 */
	@Override
	public NBTTagCompound write(NBTTagCompound tag)
	{
		super.write(tag);
		tag.putInt("cooldown", cooldown);
		tag.putBoolean("isPowered", isPowered);
		return tag;
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void read(NBTTagCompound tag)
	{
		super.read(tag);

		if (tag.contains("cooldown"))
			cooldown = tag.getInt("cooldown");

		if (tag.contains("isPowered"))
			isPowered = tag.getBoolean("isPowered");

	}

	public int getCooldown(){
		return cooldown;
	}

	public void setCooldown(int cooldown){
		SecurityCraft.log("Setting cooldown to " + cooldown + " | " + (world.isRemote ? LogicalSide.CLIENT : LogicalSide.SERVER));
		this.cooldown = cooldown;
	}

	public boolean isPowered() {
		return isPowered;
	}

	public void setPowered(boolean isPowered) {
		this.isPowered = isPowered;
	}

}
