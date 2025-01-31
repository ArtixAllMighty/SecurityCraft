package net.geforcemods.securitycraft.network;

import java.util.HashMap;
import java.util.Map;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.items.ItemCameraMonitor;
import net.geforcemods.securitycraft.tileentity.TileEntitySecretSign;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent.Register;

public class ServerProxy implements IProxy
{
	@Override
	public void clientSetup() {}

	@Override
	public void registerKeypadChestItem(Register<Item> event)
	{
		event.getRegistry().register(new BlockItem(SCContent.keypadChest, new Item.Properties().group(SecurityCraft.groupSCTechnical)).setRegistryName(SCContent.keypadChest.getRegistryName()));
	}

	@Override
	public Map<Block,Integer> getOrPopulateToTint()
	{
		return new HashMap<>();
	}

	@Override
	public void cleanup() {}

	@Override
	public World getClientWorld()
	{
		return null;
	}

	@Override
	public PlayerEntity getClientPlayer()
	{
		return null;
	}

	@Override
	public void displayMRATGui(ItemStack stack) {}

	@Override
	public void displayEditModuleGui(ItemStack stack) {}

	@Override
	public void displayCameraMonitorGui(PlayerInventory inv, ItemCameraMonitor item, CompoundNBT stackTag) {}

	@Override
	public void displaySCManualGui() {}

	@Override
	public void displayEditSecretSignGui(TileEntitySecretSign te) {}
}
