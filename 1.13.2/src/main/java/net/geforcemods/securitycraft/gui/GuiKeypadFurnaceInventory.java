package net.geforcemods.securitycraft.gui;

import java.util.Random;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.network.server.CloseFurnace;
import net.geforcemods.securitycraft.tileentity.TileEntityKeypadFurnace;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiKeypadFurnaceInventory extends GuiContainer{

	private static final ResourceLocation furnaceGuiTextures = new ResourceLocation("textures/gui/container/furnace.png");
	private TileEntityKeypadFurnace tileFurnace;
	private boolean gurnace = false;

	public GuiKeypadFurnaceInventory(InventoryPlayer inventory, TileEntityKeypadFurnace te){
		super(new ContainerFurnace(inventory, te));
		tileFurnace = te;

		if(new Random().nextInt(100) < 5)
			gurnace = true;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks)
	{
		super.render(mouseX, mouseY, partialTicks);

		if(getSlotUnderMouse() != null && !getSlotUnderMouse().getStack().isEmpty())
			renderToolTip(getSlotUnderMouse().getStack(), mouseX, mouseY);
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		String s = gurnace ? "Keypad Gurnace" : (tileFurnace.hasCustomSCName() ? tileFurnace.getName().getFormattedText() : ClientUtils.localize("gui.securitycraft:protectedFurnace.name"));
		fontRenderer.drawString(s, xSize / 2 - fontRenderer.getStringWidth(s) / 2, 6, 4210752);
		fontRenderer.drawString(ClientUtils.localize("container.inventory"), 8, ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		drawDefaultBackground();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(furnaceGuiTextures);
		int startX = (width - xSize) / 2;
		int startY = (height - ySize) / 2;
		this.drawTexturedModalRect(startX, startY, 0, 0, xSize, ySize);

		if (tileFurnace.isBurning())
		{
			int burnTime = tileFurnace.getBurnTimeRemainingScaled(13);
			this.drawTexturedModalRect(startX + 56, startY + 36 + 12 - burnTime, 176, 12 - burnTime, 14, burnTime + 1);
			burnTime = tileFurnace.getCookProgressScaled(24);
			this.drawTexturedModalRect(startX + 79, startY + 34, 176, 14, burnTime + 1, 16);
		}
	}

	@Override
	public void onGuiClosed(){
		super.onGuiClosed();
		SecurityCraft.channel.sendToServer(new CloseFurnace(tileFurnace.getPos()));
	}

}