package net.geforcemods.securitycraft.gui.components;

import java.util.function.Consumer;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiPictureButton extends GuiButtonClick{

	private final ItemRenderer itemRenderer;
	private Block blockToRender;
	private Item itemToRender;
	private ResourceLocation textureLocation;
	private int u;
	private int v;
	private int texWidth;
	private int texHeight;
	public int id;

	public GuiPictureButton(int id, int xPos, int yPos, int width, int height, ItemRenderer par7, ItemStack itemToRender) {
		this(id, xPos, yPos, width, height, par7, itemToRender, null);
	}

	public GuiPictureButton(int id, int xPos, int yPos, int width, int height, ResourceLocation texture, int textureX, int textureY, int textureWidth, int textureHeight)
	{
		this(id, xPos, yPos, width, height, texture, textureX, textureY, textureWidth, textureHeight, null);
	}

	public GuiPictureButton(int id, int xPos, int yPos, int width, int height, ItemRenderer par7, ItemStack itemToRender, Consumer<GuiButtonClick> onClick) {
		super(id, xPos, yPos, width, height, "", onClick);
		itemRenderer = par7;

		if(!itemToRender.isEmpty() && itemToRender.getItem() instanceof BlockItem)
			blockToRender = Block.getBlockFromItem(itemToRender.getItem());
		else
			this.itemToRender = itemToRender.getItem();

		this.id = id;
	}

	public GuiPictureButton(int id, int xPos, int yPos, int width, int height, ResourceLocation texture, int textureX, int textureY, int textureWidth, int textureHeight, Consumer<GuiButtonClick> onClick)
	{
		super(id, xPos, yPos, width, height, "", onClick);

		itemRenderer = null;
		textureLocation = texture;
		u = textureX;
		v = textureY;
		texWidth = textureWidth;
		texHeight = textureHeight;
		this.id = id;
	}

	/**
	 * Draws this button to the screen.
	 */
	@Override
	public void render(int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();

		if (visible)
		{
			FontRenderer font = mc.fontRenderer;
			mc.getTextureManager().bindTexture(WIDGETS_LOCATION);
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			int hoverState = !active ? 0 : !isHovered ? 1 : 2;
			GlStateManager.enableBlend();
			GLX.glBlendFuncSeparate(770, 771, 1, 0);
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			this.blit(x, y, 0, 46 + hoverState * 20, width / 2, height);
			this.blit(x + width / 2, y, 200 - width / 2, 46 + hoverState * 20, width / 2, height);

			if(blockToRender != null){
				GlStateManager.enableRescaleNormal(); //(this.width / 2) - 8
				itemRenderer.renderItemAndEffectIntoGUI(new ItemStack(blockToRender), x + 2, y + 3);
				itemRenderer.renderItemOverlayIntoGUI(font, new ItemStack(blockToRender), x + 2, y + 3, "");
			}else if(itemToRender != null){
				GlStateManager.enableRescaleNormal();
				itemRenderer.renderItemAndEffectIntoGUI(new ItemStack(itemToRender), x + 2, y + 2);
				itemRenderer.renderItemOverlayIntoGUI(font, new ItemStack(itemToRender), x + 2, y + 2, "");
				GlStateManager.disableLighting();
			}
			else if(textureLocation != null)
			{
				GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				mc.getTextureManager().bindTexture(textureLocation);
				blit(x, y + 1, u, v, texWidth, texHeight);
			}

			onDrag(mouseX, mouseY, 0, 0);

			int color = 14737632;

			if (!active)
				color = 10526880;
			else if (isHovered)
				color = 16777120;

			drawCenteredString(font, getMessage(), x + width / 2, y + (height - 8) / 2, color);

		}
	}

	public void setDisplayItem(ItemStack stack){
		blockToRender = null;
		itemToRender = null;

		if(stack.getTranslationKey().startsWith("tile."))
			blockToRender = Block.getBlockFromItem(stack.getItem());
		else
			itemToRender = stack.getItem();

	}

	public Item getItemStack() {
		return (blockToRender != null ? blockToRender.asItem() : itemToRender);
	}

}
