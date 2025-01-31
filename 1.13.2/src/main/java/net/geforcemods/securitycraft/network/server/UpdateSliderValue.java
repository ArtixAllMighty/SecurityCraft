package net.geforcemods.securitycraft.network.server;

import java.util.function.Supplier;

import io.netty.buffer.ByteBuf;
import net.geforcemods.securitycraft.api.CustomizableSCTE;
import net.geforcemods.securitycraft.api.Option.OptionDouble;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class UpdateSliderValue {

	private BlockPos pos;
	private int id;
	private double value;

	public UpdateSliderValue(){ }

	public UpdateSliderValue(BlockPos pos, int id, double v){
		this.pos = pos;
		this.id = id;
		value = v;
	}

	public void toBytes(ByteBuf buf) {
		buf.writeLong(pos.toLong());
		buf.writeInt(id);
		buf.writeDouble(value);
	}

	public void fromBytes(ByteBuf buf) {
		pos = BlockPos.fromLong(buf.readLong());
		id = buf.readInt();
		value = buf.readDouble();
	}

	public static void encode(UpdateSliderValue message, PacketBuffer packet)
	{
		message.toBytes(packet);
	}

	public static UpdateSliderValue decode(PacketBuffer packet)
	{
		UpdateSliderValue message = new UpdateSliderValue();

		message.fromBytes(packet);
		return message;
	}

	public static void onMessage(UpdateSliderValue message, Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() -> {
			BlockPos pos = message.pos;
			int id = message.id;
			double value = message.value;
			EntityPlayer player = ctx.get().getSender();

			if(player.world.getTileEntity(pos) != null && player.world.getTileEntity(pos) instanceof CustomizableSCTE) {
				((OptionDouble)((CustomizableSCTE) player.world.getTileEntity(pos)).customOptions()[id]).setValue(value);
				((CustomizableSCTE) player.world.getTileEntity(pos)).onOptionChanged(((CustomizableSCTE) player.world.getTileEntity(pos)).customOptions()[id]);
				((CustomizableSCTE) player.world.getTileEntity(pos)).sync();
			}
		});

		ctx.get().setPacketHandled(true);
	}
}
