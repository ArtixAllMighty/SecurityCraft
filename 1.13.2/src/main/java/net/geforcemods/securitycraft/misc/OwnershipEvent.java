package net.geforcemods.securitycraft.misc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired when a SecurityCraft block needs to have ownership information attached
 */
public class OwnershipEvent extends Event
{
	private World world;
	private BlockPos pos;
	private EntityPlayer player;

	public OwnershipEvent(World world, BlockPos pos, EntityPlayer player)
	{
		this.world = world;
		this.pos = pos;
		this.player = player;
	}

	public World getWorld()
	{
		return world;
	}

	public BlockPos getPos()
	{
		return pos;
	}

	public EntityPlayer getPlayer()
	{
		return player;
	}
}
