package net.geforcemods.securitycraft.gui;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.containers.ContainerGeneric;
import net.geforcemods.securitycraft.gui.components.GuiButtonClick;
import net.geforcemods.securitycraft.network.server.OpenGui;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiBriefcase extends GuiContainer {

	public static final String UP_ARROW  = "\u2191";
	public static final String DOWN_ARROW  = "\u2193";

	private static final ResourceLocation TEXTURE = new ResourceLocation("securitycraft:textures/gui/container/blank.png");

	private GuiButton[] keycodeTopButtons = new GuiButton[4];
	private GuiButton[] keycodeBottomButtons = new GuiButton[4];
	private GuiTextField[] keycodeTextboxes = new GuiTextField[4];
	private GuiButton continueButton;

	public GuiBriefcase() {
		super(new ContainerGeneric());
	}

	@Override
	public void initGui() {
		super.initGui();

		for(int i = 0; i < keycodeTopButtons.length; i++) {
			keycodeTopButtons[i] = new GuiButtonClick(i, width / 2 - 40 + (i * 20), height / 2 - 52, 20, 20, UP_ARROW, this::actionPerformed);
			addButton(keycodeTopButtons[i]);
		}

		for(int i = 0; i < keycodeBottomButtons.length; i++) {
			keycodeBottomButtons[i] = new GuiButtonClick(4 + i, width / 2 - 40 + (i * 20), height / 2, 20, 20, DOWN_ARROW, this::actionPerformed);
			addButton(keycodeBottomButtons[i]);
		}

		continueButton = new GuiButtonClick(8, (width / 2 + 42), height / 2 - 26, 20, 20, ">", this::actionPerformed);
		addButton(continueButton);

		for(int i = 0; i < keycodeTextboxes.length; i++) {
			keycodeTextboxes[i] = new GuiTextField(9 + i, fontRenderer, (width / 2 - 37) + (i * 20), height / 2 - 22, 14, 12);

			keycodeTextboxes[i].setTextColor(-1);
			keycodeTextboxes[i].setDisabledTextColour(-1);
			keycodeTextboxes[i].setEnableBackgroundDrawing(true);
			keycodeTextboxes[i].setMaxStringLength(1);
			keycodeTextboxes[i].setText("0");
		}
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		super.render(mouseX, mouseY, partialTicks);
		GlStateManager.disableLighting();

		for(GuiTextField textfield : keycodeTextboxes)
			textfield.drawTextField(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRenderer.drawString(ClientUtils.localize("gui.securitycraft:briefcase.enterPasscode"), xSize / 2 - fontRenderer.getStringWidth(ClientUtils.localize("gui.securitycraft:briefcase.enterPasscode")) / 2, 6, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		drawDefaultBackground();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(TEXTURE);
		int startX = (width - xSize) / 2;
		int startY = (height - ySize) / 2;
		this.drawTexturedModalRect(startX, startY, 0, 0, xSize, ySize);
	}

	protected void actionPerformed(GuiButton button) {
		int[] keys = new int[]{Integer.parseInt(keycodeTextboxes[0].getText()), Integer.parseInt(keycodeTextboxes[1].getText()), Integer.parseInt(keycodeTextboxes[2].getText()), Integer.parseInt(keycodeTextboxes[3].getText())};

		switch(button.id) {
			case 0:
				if(keys[0] == 9)
					keys[0] = 0;
				else
					keys[0]++;
				break;
			case 1:
				if(keys[1] == 9)
					keys[1] = 0;
				else
					keys[1]++;
				break;
			case 2:
				if(keys[2] == 9)
					keys[2] = 0;
				else
					keys[2]++;
				break;
			case 3:
				if(keys[3] == 9)
					keys[3] = 0;
				else
					keys[3]++;
				break;
			case 4:
				if(keys[0] == 0)
					keys[0] = 9;
				else
					keys[0]--;
				break;
			case 5:
				if(keys[1] == 0)
					keys[1] = 9;
				else
					keys[1]--;
				break;
			case 6:
				if(keys[2] == 0)
					keys[2] = 9;
				else
					keys[2]--;
				break;
			case 7:
				if(keys[3] == 0)
					keys[3] = 9;
				else
					keys[3]--;
				break;
			case 8:
				if(PlayerUtils.isHoldingItem(Minecraft.getInstance().player, SCContent.briefcase)) {
					NBTTagCompound nbt = Minecraft.getInstance().player.inventory.getCurrentItem().getTag();
					String code = keys[0] + "" + keys[1] + "" +  keys[2] + "" + keys[3];

					if(nbt.getString("passcode").equals(code))
						SecurityCraft.channel.sendToServer(new OpenGui(GuiHandler.BRIEFCASE, Minecraft.getInstance().player.getPosition()));
				}

				break;
		}

		keycodeTextboxes[0].setText(String.valueOf(keys[0]));
		keycodeTextboxes[1].setText(String.valueOf(keys[1]));
		keycodeTextboxes[2].setText(String.valueOf(keys[2]));
		keycodeTextboxes[3].setText(String.valueOf(keys[3]));
	}
}
