package net.geforcemods.securitycraft.tileentity;

import java.util.List;

import net.geforcemods.securitycraft.ConfigHandler.CommonConfig;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.CustomizableSCTE;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.OptionBoolean;
import net.geforcemods.securitycraft.api.Option.OptionDouble;
import net.geforcemods.securitycraft.api.Option.OptionInt;
import net.geforcemods.securitycraft.blocks.BlockPortableRadar;
import net.geforcemods.securitycraft.misc.EnumCustomModules;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.geforcemods.securitycraft.util.ModuleUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class TileEntityPortableRadar extends CustomizableSCTE {

	private OptionDouble searchRadiusOption = new OptionDouble("searchRadius", CommonConfig.CONFIG.portableRadarSearchRadius.get(), 5.0D, 50.0D, 5.0D);
	private OptionInt searchDelayOption = new OptionInt("searchDelay", CommonConfig.CONFIG.portableRadarDelay.get(), 4, 10, 1);
	private OptionBoolean repeatMessageOption = new OptionBoolean("repeatMessage", true);
	private OptionBoolean enabledOption = new OptionBoolean("enabled", true);

	private boolean shouldSendNewMessage = true;
	private String lastPlayerName = "";

	public TileEntityPortableRadar()
	{
		super(SCContent.teTypePortableRadar);
	}

	//Using TileEntitySCTE.attacks() and the attackEntity() method to check for players. :3
	@Override
	public boolean attackEntity(Entity attacked) {
		if (attacked instanceof EntityPlayer)
		{
			AxisAlignedBB area = new AxisAlignedBB(pos).grow(getAttackRange(), getAttackRange(), getAttackRange());
			List<?> entities = world.getEntitiesWithinAABB(entityTypeToAttack(), area);

			if(entities.isEmpty())
			{
				boolean redstoneModule = hasModule(EnumCustomModules.REDSTONE);

				if(!redstoneModule || (redstoneModule && world.getBlockState(pos).get(BlockPortableRadar.POWERED)))
				{
					BlockPortableRadar.togglePowerOutput(world, pos, false);
					return false;
				}
			}

			EntityPlayerMP owner = world.getServer().getPlayerList().getPlayerByUsername(getOwner().getName());

			if(owner != null && hasModule(EnumCustomModules.WHITELIST) && ModuleUtils.getPlayersFromModule(world, pos, EnumCustomModules.WHITELIST).contains(attacked.getName().getFormattedText().toLowerCase()))
				return false;


			if(PlayerUtils.isPlayerOnline(getOwner().getName()) && shouldSendMessage((EntityPlayer)attacked))
			{
				PlayerUtils.sendMessageToPlayer(owner, ClientUtils.localize(SCContent.portableRadar.getTranslationKey()), hasCustomSCName() ? (ClientUtils.localize("messages.securitycraft:portableRadar.withName").replace("#p", TextFormatting.ITALIC + attacked.getName().getFormattedText() + TextFormatting.RESET).replace("#n", TextFormatting.ITALIC + getCustomSCName().getFormattedText() + TextFormatting.RESET)) : (ClientUtils.localize("messages.securitycraft:portableRadar.withoutName").replace("#p", TextFormatting.ITALIC + attacked.getName().getFormattedText() + TextFormatting.RESET).replace("#l", Utils.getFormattedCoordinates(pos))), TextFormatting.BLUE);
				setSentMessage();
			}

			if(hasModule(EnumCustomModules.REDSTONE))
				BlockPortableRadar.togglePowerOutput(world, pos, true);

			return true;
		}
		else return false;
	}

	@Override
	public void attackFailed()
	{
		if(hasModule(EnumCustomModules.REDSTONE))
			BlockPortableRadar.togglePowerOutput(world, pos, false);
	}

	@Override
	public NBTTagCompound write(NBTTagCompound tag)
	{
		super.write(tag);

		tag.putBoolean("shouldSendNewMessage", shouldSendNewMessage);
		tag.putString("lastPlayerName", lastPlayerName);
		return tag;
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		super.read(tag);

		if (tag.contains("shouldSendNewMessage"))
			shouldSendNewMessage = tag.getBoolean("shouldSendNewMessage");

		if (tag.contains("lastPlayerName"))
			lastPlayerName = tag.getString("lastPlayerName");
	}

	public boolean shouldSendMessage(EntityPlayer player) {
		if(!player.getName().getFormattedText().equals(lastPlayerName)) {
			shouldSendNewMessage = true;
			lastPlayerName = player.getName().getFormattedText();
		}

		return (shouldSendNewMessage || repeatMessageOption.asBoolean()) && enabledOption.asBoolean() && !player.getName().getFormattedText().equals(getOwner().getName());
	}

	public void setSentMessage() {
		shouldSendNewMessage = false;
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
	public int getTicksBetweenAttacks() {
		return searchDelayOption.asInteger() * 20;
	}

	@Override
	public EnumCustomModules[] acceptedModules() {
		return new EnumCustomModules[]{EnumCustomModules.REDSTONE, EnumCustomModules.WHITELIST};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[]{ searchRadiusOption, searchDelayOption, repeatMessageOption, enabledOption };
	}

	@Override
	public ITextComponent getCustomName()
	{
		return getCustomSCName();
	}

	@Override
	public boolean hasCustomName()
	{
		return hasCustomSCName();
	}

}
