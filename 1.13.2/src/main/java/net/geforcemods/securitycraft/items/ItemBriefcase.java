package net.geforcemods.securitycraft.items;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.gui.GuiHandler;
import net.geforcemods.securitycraft.network.server.OpenGui;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBriefcase extends Item {

	public ItemBriefcase()
	{
		super(new Item.Properties().group(SecurityCraft.groupSCTechnical).maxStackSize(1));
	}

	@Override
	public EnumActionResult onItemUse(ItemUseContext ctx)
	{
		return onItemUse(ctx.getPlayer(), ctx.getWorld(), ctx.getPos(), ctx.getItem(), ctx.getFace(), ctx.getHitX(), ctx.getHitY(), ctx.getHitZ());
	}

	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, ItemStack stack, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			if(!stack.hasTag()) {
				stack.setTag(new NBTTagCompound());
				ClientUtils.syncItemNBT(stack);
			}

			if(!stack.getTag().contains("passcode"))
				SecurityCraft.channel.sendToServer(new OpenGui(GuiHandler.BRIEFCASE_SETUP, player.getPosition()));
			else
				SecurityCraft.channel.sendToServer(new OpenGui(GuiHandler.BRIEFCASE_INSERT, player.getPosition()));
		}

		return EnumActionResult.FAIL;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);

		if(world.isRemote) {
			if(!stack.hasTag()) {
				stack.setTag(new NBTTagCompound());
				ClientUtils.syncItemNBT(stack);
			}

			if(!stack.getTag().contains("passcode"))
				SecurityCraft.channel.sendToServer(new OpenGui(GuiHandler.BRIEFCASE_SETUP, player.getPosition()));
			else
				SecurityCraft.channel.sendToServer(new OpenGui(GuiHandler.BRIEFCASE_INSERT, player.getPosition()));
		}

		return ActionResult.newResult(EnumActionResult.PASS, stack);
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack)
	{
		ItemStack newStack = stack.copy();

		if(newStack.getTag() != null && newStack.getTag().contains("passcode"))
			newStack.getTag().remove("passcode");

		return newStack;
	}

	@Override
	public boolean hasContainerItem()
	{
		return true;
	}
}
