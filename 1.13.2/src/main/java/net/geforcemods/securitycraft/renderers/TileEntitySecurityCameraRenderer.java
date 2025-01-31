package net.geforcemods.securitycraft.renderers;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blocks.BlockSecurityCamera;
import net.geforcemods.securitycraft.models.ModelSecurityCamera;
import net.geforcemods.securitycraft.tileentity.TileEntitySecurityCamera;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TileEntitySecurityCameraRenderer extends TileEntityRenderer<TileEntitySecurityCamera> {

	private static final ModelSecurityCamera modelSecurityCamera = new ModelSecurityCamera();
	private static final ResourceLocation cameraTexture = new ResourceLocation("securitycraft:textures/block/security_camera1.png");

	@Override
	public void render(TileEntitySecurityCamera par1TileEntity, double x, double y, double z, float par5, int par6) {
		if(par1TileEntity.down || PlayerUtils.isPlayerMountedOnCamera(Minecraft.getInstance().player) && Minecraft.getInstance().player.getRidingEntity().getPosition().equals(par1TileEntity.getPos()))
			return;

		float rotation = 0F;

		if(par1TileEntity.hasWorld()){
			Tessellator tessellator = Tessellator.getInstance();
			float brightness = par1TileEntity.getWorld().getBrightness(par1TileEntity.getPos());
			int skyBrightness = par1TileEntity.getWorld().getCombinedLight(par1TileEntity.getPos(), 0);
			int lightmapX = skyBrightness % 65536;
			int lightmapY = skyBrightness / 65536;
			tessellator.getBuffer().putColorRGBA(0, (int)(brightness * 255.0F), (int)(brightness * 255.0F), (int)(brightness * 255.0F), 255);

			OpenGlHelper.glMultiTexCoord2f(OpenGlHelper.GL_TEXTURE1, lightmapX, lightmapY);
		}

		GlStateManager.pushMatrix();
		GlStateManager.translatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);

		Minecraft.getInstance().textureManager.bindTexture(cameraTexture);

		GlStateManager.pushMatrix();

		if(par1TileEntity.hasWorld() && BlockUtils.getBlock(par1TileEntity.getWorld(), par1TileEntity.getPos()) == SCContent.securityCamera){
			EnumFacing side = BlockUtils.getBlockPropertyAsEnum(getWorld(), par1TileEntity.getPos(), BlockSecurityCamera.FACING);

			if(side == EnumFacing.EAST)
				rotation = -1F;
			else if(side == EnumFacing.SOUTH)
				rotation = -10000F;
			else if(side == EnumFacing.WEST)
				rotation = 1F;
			else if(side == EnumFacing.NORTH)
				rotation = 0F;
		}
		else
			rotation = -10000F;

		GlStateManager.rotatef(180F, rotation, 0.0F, 1.0F);

		modelSecurityCamera.cameraRotationPoint.rotateAngleY = par1TileEntity.cameraRotation;

		modelSecurityCamera.render((Entity) null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);

		GlStateManager.popMatrix();
		GlStateManager.popMatrix();
	}


}
