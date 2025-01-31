package net.geforcemods.securitycraft.gui;

import com.mojang.blaze3d.platform.GlStateManager;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.IPasswordProtected;
import net.geforcemods.securitycraft.containers.ContainerTEGeneric;
import net.geforcemods.securitycraft.gui.components.GuiButtonClick;
import net.geforcemods.securitycraft.network.server.SetPassword;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiKeyChanger extends ContainerScreen<ContainerTEGeneric> {

	private static final ResourceLocation TEXTURE = new ResourceLocation("securitycraft:textures/gui/container/blank.png");
	private char[] allowedChars = {'0', '1', '2', '3', '4', '5', '6' ,'7' ,'8', '9', '\u0008', '\u001B'}; //0-9, backspace and escape
	private TextFieldWidget textboxNewPasscode;
	private TextFieldWidget textboxConfirmPasscode;
	private GuiButtonClick confirmButton;

	private TileEntity tileEntity;

	public GuiKeyChanger(ContainerTEGeneric container, PlayerInventory inv, ITextComponent name) {
		super(container, inv, name);
		tileEntity = container.te;
	}

	@Override
	public void init(){
		super.init();
		minecraft.keyboardListener.enableRepeatEvents(true);
		addButton(confirmButton = new GuiButtonClick(0, width / 2 - 52, height / 2 + 52, 100, 20, ClientUtils.localize("gui.securitycraft:universalKeyChanger.confirm"), this::actionPerformed));
		confirmButton.active = false;

		textboxNewPasscode = new TextFieldWidget(font, width / 2 - 57, height / 2 - 47, 110, 12, "");

		textboxNewPasscode.setTextColor(-1);
		textboxNewPasscode.setDisabledTextColour(-1);
		textboxNewPasscode.setEnableBackgroundDrawing(true);
		textboxNewPasscode.setMaxStringLength(20);

		textboxConfirmPasscode = new TextFieldWidget(font, width / 2 - 57, height / 2 - 7, 110, 12, "");

		textboxConfirmPasscode.setTextColor(-1);
		textboxConfirmPasscode.setDisabledTextColour(-1);
		textboxConfirmPasscode.setEnableBackgroundDrawing(true);
		textboxConfirmPasscode.setMaxStringLength(20);

	}

	@Override
	public void onClose(){
		super.onClose();
		minecraft.keyboardListener.enableRepeatEvents(false);
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks){
		super.render(mouseX, mouseY, partialTicks);
		GlStateManager.disableLighting();
		textboxNewPasscode.render(mouseX, mouseY, partialTicks);
		textboxConfirmPasscode.render(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY){
		font.drawString(ClientUtils.localize(SCContent.universalKeyChanger.getTranslationKey()), xSize / 2 - font.getStringWidth(ClientUtils.localize(SCContent.universalKeyChanger.getTranslationKey())) / 2, 6, 4210752);
		font.drawString(ClientUtils.localize("gui.securitycraft:universalKeyChanger.enterNewPasscode"), xSize / 2 - font.getStringWidth(ClientUtils.localize("gui.securitycraft:universalKeyChanger.enterNewPasscode")) / 2, 25, 4210752);
		font.drawString(ClientUtils.localize("gui.securitycraft:universalKeyChanger.confirmNewPasscode"), xSize / 2 - font.getStringWidth(ClientUtils.localize("gui.securitycraft:universalKeyChanger.confirmNewPasscode")) / 2, 65, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY){
		renderBackground();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		minecraft.getTextureManager().bindTexture(TEXTURE);
		int startX = (width - xSize) / 2;
		int startY = (height - ySize) / 2;
		this.blit(startX, startY, 0, 0, xSize, ySize);
	}

	@Override
	public boolean charTyped(char typedChar, int keyCode) {
		if(!isValidChar(typedChar))
			return false;

		if(textboxNewPasscode.isFocused())
			textboxNewPasscode.charTyped(typedChar, keyCode);
		else if(textboxConfirmPasscode.isFocused())
			textboxConfirmPasscode.charTyped(typedChar, keyCode);
		else
			return super.charTyped(typedChar, keyCode);

		checkToEnableSaveButton();
		return true;
	}

	private boolean isValidChar(char c) {
		for(int x = 1; x <= allowedChars.length; x++)
			if(c == allowedChars[x - 1])
				return true;
			else
				continue;

		return false;
	}

	private void checkToEnableSaveButton() {
		String newPasscode = !textboxNewPasscode.getText().isEmpty() ? textboxNewPasscode.getText() : null;
		String confirmedPasscode = !textboxConfirmPasscode.getText().isEmpty() ? textboxConfirmPasscode.getText() : null;

		if(newPasscode == null || confirmedPasscode == null) return;
		if(!newPasscode.equals(confirmedPasscode)) return;

		confirmButton.active = true;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		textboxNewPasscode.mouseClicked(mouseX, mouseY, mouseButton);
		textboxConfirmPasscode.mouseClicked(mouseX, mouseY, mouseButton);
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	protected void actionPerformed(GuiButtonClick button){
		switch(button.id){
			case 0:
				((IPasswordProtected) tileEntity).setPassword(textboxNewPasscode.getText());
				SecurityCraft.channel.sendToServer(new SetPassword(tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ(), textboxNewPasscode.getText()));

				ClientUtils.closePlayerScreen();
				PlayerUtils.sendMessageToPlayer(Minecraft.getInstance().player, ClientUtils.localize(SCContent.universalKeyChanger.getTranslationKey()), ClientUtils.localize("messages.securitycraft:universalKeyChanger.passcodeChanged"), TextFormatting.GREEN);
		}
	}

}
