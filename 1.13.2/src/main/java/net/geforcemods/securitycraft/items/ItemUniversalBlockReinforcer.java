package net.geforcemods.securitycraft.items;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.blocks.reinforced.IReinforcedBlock;
import net.geforcemods.securitycraft.gui.GuiHandler;
import net.geforcemods.securitycraft.misc.BaseInteractionObject;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class ItemUniversalBlockReinforcer extends Item
{
	public ItemUniversalBlockReinforcer(int damage)
	{
		super(new Item.Properties().maxStackSize(1).defaultMaxDamage(damage).group(SecurityCraft.groupSCTechnical));
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		if(!world.isRemote && player instanceof EntityPlayerMP)
			NetworkHooks.openGui((EntityPlayerMP)player, new BaseInteractionObject(GuiHandler.BLOCK_REINFORCER));
		return super.onItemRightClick( world, player, hand);
	}

	@Override
	public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player)
	{
		if(!player.isCreative())
		{
			World world = player.getEntityWorld();
			IBlockState vanillaState = world.getBlockState(pos);
			Block block = vanillaState.getBlock();

			for(Block rb : IReinforcedBlock.BLOCKS)
			{
				if(((IReinforcedBlock)rb).getVanillaBlock() == block)
				{
					world.setBlockState(pos, ((IReinforcedBlock)rb).getConvertedState(vanillaState));
					((IOwnable)world.getTileEntity(pos)).getOwner().set(player.getGameProfile().getId().toString(), player.getName());
					stack.damageItem(1, player);
					return true;
				}
			}
		}

		return false;
	}
}
