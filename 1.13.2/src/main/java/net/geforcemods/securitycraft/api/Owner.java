package net.geforcemods.securitycraft.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.util.text.ITextComponent;

/**
 * This class is used with {@link IOwnable} to get the player of the block.
 * Allows for easy access to the player's IGN and UUID, with a few helpful methods as well.
 *
 * @author Geforce
 */
public class Owner {
	public static final DataSerializer<Owner> SERIALIZER = new DataSerializer<Owner>()
	{
		@Override
		public void write(PacketBuffer buf, Owner value)
		{
			buf.writeString(value.getName());
			buf.writeString(value.getUUID());
		}

		@Override
		public Owner read(PacketBuffer buf)
		{
			String name = buf.readString(Integer.MAX_VALUE / 4);
			String uuid = buf.readString(Integer.MAX_VALUE / 4);

			return new Owner(name, uuid);
		}

		@Override
		public DataParameter<Owner> createKey(int id)
		{
			return new DataParameter<Owner>(id, this);
		}

		@Override
		public Owner copyValue(Owner value)
		{
			return new Owner(value.getName(), value.getUUID());
		}
	};

	private String playerName = "owner";
	private String playerUUID = "ownerUUID";

	public Owner() {}

	public Owner(String playerName, String playerUUID) {
		this.playerName = playerName;
		this.playerUUID = playerUUID;
	}

	/**
	 * @return If this user is the owner of the given blocks.
	 */
	public boolean owns(IOwnable... ownables) {
		for(IOwnable ownable : ownables) {
			if(ownable == null) continue;

			String uuid = ownable.getOwner().getUUID();
			String owner = ownable.getOwner().getName();

			// Check the player's UUID first.
			if(uuid != null && !uuid.equals(playerUUID))
				return false;

			// If the TileEntity doesn't have a UUID saved, use the player's name instead.
			if(owner != null && uuid.equals("ownerUUID") && !owner.equals("owner") && !owner.equals(playerName))
				return false;
		}

		return true;
	}

	/**
	 * @return If this person is the same person as the given player.
	 */
	public boolean isOwner(EntityPlayer player) {
		if(player == null) return false;
		String uuid = player.getGameProfile().getId().toString();
		String owner = player.getName().getFormattedText();

		if(uuid != null && uuid.equals(playerUUID))
			return true;

		if(owner != null && playerUUID.equals("ownerUUID") && owner.equals(playerName))
			return true;

		return false;
	}

	/**
	 * Set the UUID and name of a new owner using strings.
	 */
	public void set(String uuid, String name) {
		playerName = name;
		playerUUID = uuid;
	}

	/**
	 * Set the UUID and name of a new owner using strings.
	 */
	public void set(String uuid, ITextComponent name) {
		playerName = name.getFormattedText();
		playerUUID = uuid;
	}

	/**
	 * Set the UUID and name of a new owner using another Owner object.
	 */
	public void set(Owner newOwner) {
		playerName = newOwner.getName();
		playerUUID = newOwner.getUUID();
	}

	/**
	 * Set the owner's new name.
	 *
	 * @param name The new owner's name
	 */
	public void setOwnerName(String name) {
		playerName = name;
	}

	/**
	 * Set the owner's new UUID.
	 *
	 * @param uuid The new owner's UUID
	 */
	public void setOwnerUUID(String uuid) {
		playerUUID = uuid;
	}

	/**
	 * @return The owner's name.
	 */
	public String getName() {
		return playerName;
	}

	/**
	 * @return The owner's UUID.
	 */
	public String getUUID() {
		return playerUUID;
	}

	@Override
	public String toString() {
		return "Name: " + playerName + "  UUID: " + playerUUID;
	}

}
