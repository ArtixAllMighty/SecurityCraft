package net.geforcemods.securitycraft.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.CustomizableSCTE;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.OptionDouble;
import net.geforcemods.securitycraft.containers.ContainerCustomizeBlock;
import net.geforcemods.securitycraft.gui.components.GuiButtonClick;
import net.geforcemods.securitycraft.gui.components.GuiPictureButton;
import net.geforcemods.securitycraft.gui.components.NamedSlider;
import net.geforcemods.securitycraft.network.server.ToggleOption;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.config.HoverChecker;

@OnlyIn(Dist.CLIENT)
public class GuiCustomizeBlock extends ContainerScreen<ContainerCustomizeBlock>{
	private static final ResourceLocation[] TEXTURES = {
			new ResourceLocation("securitycraft:textures/gui/container/customize1.png"),
			new ResourceLocation("securitycraft:textures/gui/container/customize2.png"),
			new ResourceLocation("securitycraft:textures/gui/container/customize3.png")
	};
	private static final List<Rectangle2d> EXTRA_AREAS = new ArrayList<>();
	private CustomizableSCTE tileEntity;
	private GuiPictureButton[] descriptionButtons = new GuiPictureButton[5];
	private Button[] optionButtons = new Button[5];
	private HoverChecker[] hoverCheckers = new HoverChecker[10];
	private final String blockName;

	public GuiCustomizeBlock(ContainerCustomizeBlock container, PlayerInventory inv, ITextComponent name)
	{
		super(container, inv, name);
		tileEntity = container.tileEntity;
		blockName = BlockUtils.getBlock(Minecraft.getInstance().world, tileEntity.getPos()).getTranslationKey().substring(5);
	}

	@Override
	public void init(){
		super.init();

		for(int i = 0; i < tileEntity.getNumberOfCustomizableOptions(); i++){
			descriptionButtons[i] = new GuiPictureButton(i, guiLeft + 130, (guiTop + 10) + (i * 25), 20, 20, itemRenderer, new ItemStack(tileEntity.acceptedModules()[i].getItem()));
			addButton(descriptionButtons[i]);
			hoverCheckers[i] = new HoverChecker(descriptionButtons[i], 20);
		}

		if(tileEntity.customOptions() != null)
		{
			for(int i = 0; i < tileEntity.customOptions().length; i++){
				Option<?> option = tileEntity.customOptions()[i];

				if(option instanceof OptionDouble && ((OptionDouble)option).isSlider())
				{
					optionButtons[i] = new NamedSlider((ClientUtils.localize("option" + blockName + "." + option.getName()) + " ").replace("#", option.toString()), blockName, i, guiLeft + 178, (guiTop + 10) + (i * 25), 120, 20, "", "", ((OptionDouble)option).getMin(), ((OptionDouble)option).getMax(), ((OptionDouble)option).getValue(), true, true, (OptionDouble)option);
					optionButtons[i].setFGColor(14737632);
				}
				else
				{
					optionButtons[i] = new GuiButtonClick(i, guiLeft + 178, (guiTop + 10) + (i * 25), 120, 20, getOptionButtonTitle(option), this::actionPerformed);
					optionButtons[i].setFGColor(option.toString().equals(option.getDefaultValue().toString()) ? 16777120 : 14737632);
				}

				addButton(optionButtons[i]);
				hoverCheckers[i + tileEntity.getNumberOfCustomizableOptions()] = new HoverChecker(optionButtons[i], 20);
			}
		}

		for(Button button : optionButtons)
		{
			if(button == null)
				continue;

			EXTRA_AREAS.add(new Rectangle2d(button.x, button.y, button.getWidth(), button.getHeight()));
		}
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks){
		super.render(mouseX, mouseY, partialTicks);

		if(getSlotUnderMouse() != null && !getSlotUnderMouse().getStack().isEmpty())
			renderTooltip(getSlotUnderMouse().getStack(), mouseX, mouseY);

		for(int i = 0; i < hoverCheckers.length; i++)
			if(hoverCheckers[i] != null && hoverCheckers[i].checkHover(mouseX, mouseY))
				if(i < tileEntity.getNumberOfCustomizableOptions())
					this.renderTooltip(minecraft.fontRenderer.listFormattedStringToWidth(getModuleDescription(i), 150), mouseX, mouseY, font);
				else
					this.renderTooltip(minecraft.fontRenderer.listFormattedStringToWidth(getOptionDescription(i), 150), mouseX, mouseY, font);
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		String s = ClientUtils.localize(tileEntity.getBlockState().getBlock().getTranslationKey());
		font.drawString(s, xSize / 2 - font.getStringWidth(s) / 2, 6, 4210752);
		font.drawString(ClientUtils.localize("container.inventory", new Object[0]), 8, ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		renderBackground();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		minecraft.getTextureManager().bindTexture(TEXTURES[tileEntity.getNumberOfCustomizableOptions() - 1]);
		int startX = (width - xSize) / 2;
		int startY = (height - ySize) / 2;
		this.blit(startX, startY, 0, 0, xSize, ySize);
	}

	protected void actionPerformed(GuiButtonClick button) {
		Option<?> tempOption = tileEntity.customOptions()[button.id];
		tempOption.toggle();
		button.setFGColor(tempOption.toString().equals(tempOption.getDefaultValue().toString()) ? 16777120 : 14737632);
		button.setMessage(getOptionButtonTitle(tempOption));
		SecurityCraft.channel.sendToServer(new ToggleOption(tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ(), button.id));
	}

	private String getModuleDescription(int buttonID) {
		String moduleDescription = "module" + blockName + "." + descriptionButtons[buttonID].getItemStack().getTranslationKey().substring(5).replace("securitycraft.", "") + ".description";

		return ClientUtils.localize(descriptionButtons[buttonID].getItemStack().getTranslationKey()) + ":" + TextFormatting.RESET + "\n\n" + ClientUtils.localize(moduleDescription);
	}

	private String getOptionDescription(int buttonID) {
		String optionDescription = "option" + blockName + "." + tileEntity.customOptions()[buttonID - tileEntity.getNumberOfCustomizableOptions()].getName() + ".description";

		return ClientUtils.localize(optionDescription);
	}

	private String getOptionButtonTitle(Option<?> option) {
		return (ClientUtils.localize("option" + blockName + "." + option.getName()) + " ").replace("#", option.toString());
	}

	public List<Rectangle2d> getGuiExtraAreas()
	{
		return EXTRA_AREAS;
	}
}