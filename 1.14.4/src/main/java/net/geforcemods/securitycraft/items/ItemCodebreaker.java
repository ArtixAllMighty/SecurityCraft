package net.geforcemods.securitycraft.items;

import net.geforcemods.securitycraft.SecurityCraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Rarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemCodebreaker extends Item {

	public ItemCodebreaker() {
		super(new Item.Properties().maxStackSize(1).defaultMaxDamage(3).group(SecurityCraft.groupSCTechnical));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean hasEffect(ItemStack stack){
		return true;
	}

	/**
	 * Return an item rarity from Rarity
	 */
	@Override
	public Rarity getRarity(ItemStack stack){
		return Rarity.RARE;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
	{
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book)
	{
		return false;
	}

	@Override
	public boolean isEnchantable(ItemStack stack)
	{
		return false;
	}
}
