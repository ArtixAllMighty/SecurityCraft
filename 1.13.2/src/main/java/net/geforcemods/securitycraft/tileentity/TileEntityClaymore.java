package net.geforcemods.securitycraft.tileentity;

import java.util.Iterator;
import java.util.List;

import net.geforcemods.securitycraft.ConfigHandler.CommonConfig;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.TileEntitySCTE;
import net.geforcemods.securitycraft.blocks.mines.BlockClaymore;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class TileEntityClaymore extends TileEntitySCTE{

	private double entityX = -1D;
	private double entityY = -1D;
	private double entityZ = -1D;
	private int cooldown = -1;

	public TileEntityClaymore()
	{
		super(SCContent.teTypeClaymore);
	}

	@Override
	public void tick() {
		if(getWorld().isRemote)
			return;
		else{
			if(getWorld().getBlockState(getPos()).get(BlockClaymore.DEACTIVATED))
				return;

			if(cooldown > 0){
				cooldown--;
				return;
			}

			if(cooldown == 0){
				BlockUtils.destroyBlock(getWorld(), getPos(), false);
				getWorld().createExplosion((Entity) null, entityX, entityY + 0.5F, entityZ, 3.5F, true);
				return;
			}

			EnumFacing dir = BlockUtils.getBlockProperty(getWorld(), getPos(), BlockClaymore.FACING);
			AxisAlignedBB area = BlockUtils.fromBounds(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);

			if(dir == EnumFacing.NORTH)
				area = area.contract(-0, -0, CommonConfig.CONFIG.claymoreRange.get());
			else if(dir == EnumFacing.SOUTH)
				area = area.contract(-0, -0, -CommonConfig.CONFIG.claymoreRange.get());if(dir == EnumFacing.EAST)
					area = area.contract(-CommonConfig.CONFIG.claymoreRange.get(), -0, -0);
				else if(dir == EnumFacing.WEST)
					area = area.contract(CommonConfig.CONFIG.claymoreRange.get(), -0, -0);

				List<?> entities = getWorld().getEntitiesWithinAABB(EntityLivingBase.class, area);
				Iterator<?> iterator = entities.iterator();
				EntityLivingBase entityliving;

				while(iterator.hasNext()){
					entityliving = (EntityLivingBase) iterator.next();

					if(PlayerUtils.isPlayerMountedOnCamera(entityliving))
						continue;

					entityX = entityliving.posX;
					entityY = entityliving.posY;
					entityZ = entityliving.posZ;
					cooldown = 20;
					getWorld().playSound(null, new BlockPos(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, 0.6F);
					break;
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
		tag.putDouble("entityX", entityX);
		tag.putDouble("entityY", entityY);
		tag.putDouble("entityZ", entityZ);
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

		if (tag.contains("entityX"))
			entityX = tag.getDouble("entityX");

		if (tag.contains("entityY"))
			entityY = tag.getDouble("entityY");

		if (tag.contains("entityZ"))
			entityZ = tag.getDouble("entityZ");
	}

}
