//------------------------------------------------------------------------------------------------
//
//   Mod Base - NBT Networking - MC 1.7
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.io.*;
import java.util.*;
import io.netty.buffer.*;
import io.netty.channel.*;

import net.minecraft.entity.player.*;
import net.minecraft.nbt.*;
import net.minecraft.network.*;
//import net.minecraft.network.packet.*;

import cpw.mods.fml.common.network.*;
import cpw.mods.fml.common.registry.*;
import cpw.mods.fml.relauncher.*;

public class BaseNBTChannel<PACKET_TYPE extends Enum> {

	EnumMap<Side, FMLEmbeddedChannel> channels;

	public BaseNBTChannel(String channelName) {
		//System.out.printf("BaseNBTChannel created with name '%s'\n", channelName);
		ChannelHandler codec = new NBTCodec<PACKET_TYPE>();
		channels = NetworkRegistry.INSTANCE.newChannel(channelName, codec);
		for (Side side : Side.values()) {
			//System.out.printf("BaseNBTChannel: Adding NBTHandler to %s side\n", side);
			ChannelPipeline pipe = channels.get(side).pipeline();
			pipe.removeLast(); // Remove the LastInboundHandler
			pipe.addLast(new NBTHandler<PACKET_TYPE>(this, side));
			//System.out.printf("Handlers on %s side:\n", side);
			//for (Map.Entry<String, ChannelHandler> e : pipe) {
			//	System.out.printf("...%s: %s\n", e.getKey(), e.getValue());
			//}
		}
	}
	
	void onReceiveFromClient(PACKET_TYPE type, NBTTagCompound nbt, EntityPlayer player) {
	}
	
	void onReceiveFromServer(PACKET_TYPE type, NBTTagCompound nbt) {
	}
	
	void sendToTarget(PACKET_TYPE type, NBTTagCompound nbt, Side fromSide,
		FMLOutboundHandler.OutboundTarget target, Object... args)
	{
		FMLEmbeddedChannel chan = channels.get(fromSide);
		NBTMessage<PACKET_TYPE> msg = new NBTMessage<PACKET_TYPE>();
		msg.type = type;
		msg.nbt = nbt;
		chan.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(target);
		if (args.length == 1)
			chan.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(args[0]);
		chan.writeAndFlush(msg).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
	}
	
	public void sendToServer(PACKET_TYPE type, NBTTagCompound nbt) {
		sendToTarget(type, nbt, Side.CLIENT, FMLOutboundHandler.OutboundTarget.TOSERVER);
	}
	
	public void sendToPlayer(PACKET_TYPE type, NBTTagCompound nbt, EntityPlayer player) {
		sendToTarget(type, nbt, Side.SERVER, FMLOutboundHandler.OutboundTarget.PLAYER, player);
	}
	
	public void sendToAllPlayers(PACKET_TYPE type, NBTTagCompound nbt) {
		sendToTarget(type, nbt, Side.SERVER, FMLOutboundHandler.OutboundTarget.ALL);
	}
	
	public void sendToAllAround(PACKET_TYPE type, NBTTagCompound nbt, NetworkRegistry.TargetPoint point) {
		sendToTarget(type, nbt, Side.SERVER, FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT, point);
	}
	
	public void sendToDimension(PACKET_TYPE type, NBTTagCompound nbt, int dimensionId) {
		sendToTarget(type, nbt, Side.SERVER, FMLOutboundHandler.OutboundTarget.DIMENSION, dimensionId);
	}

	//------------------------------------------------------------------------------------------------

	public static class NBTMessage<PACKET_TYPE extends Enum> {
		public NBTMessage() {super();}
		public PACKET_TYPE type;
		public NBTTagCompound nbt;
	}
	
	//------------------------------------------------------------------------------------------------
	
	static class NBTCodec<PACKET_TYPE extends Enum> extends FMLIndexedMessageToMessageCodec<NBTMessage<PACKET_TYPE>> {
	
		NBTCodec() {
			//@SuppressWarnings("unchecked")
			//addDiscriminator(0, (Class<? extends NBTMessage<PACKET_TYPE>>)NBTMessage.class);
			addDiscriminator(0, (Class)NBTMessage.class);
		}
	
		@Override
		public void encodeInto(ChannelHandlerContext ctx, NBTMessage<PACKET_TYPE> msg, ByteBuf target) throws Exception {
			OutputStream bytes = new ByteBufOutputStream(target);
			ObjectOutputStream stream = new ObjectOutputStream(bytes);
			//System.out.printf("BaseNBTChannel: Sending type %s nbt %s\n", msg.type, msg.nbt);
			stream.writeObject(msg.type);
			CompressedStreamTools.write(msg.nbt, stream);
			stream.close();
		}
		
		@Override
		public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, NBTMessage<PACKET_TYPE> msg) {
			InputStream bytes = new ByteBufInputStream(source);
			try {
				ObjectInputStream stream = new ObjectInputStream(bytes);
				msg.type = (PACKET_TYPE)stream.readObject();
				//System.out.printf("BaseNBTChannel: Received type %s\n", msg.type);
				msg.nbt = CompressedStreamTools.read(new DataInputStream(stream));
				//System.out.printf("BaseNBTChannel: Received nbt %s\n", msg.nbt);
				//System.out.printf("NBTCodec decoded %s %s into %s\n", msg.type, msg.nbt, msg);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	
	}
	
	//------------------------------------------------------------------------------------------------
	
	//static class NBTHandler<PACKET_TYPE extends Enum> extends SimpleChannelInboundHandler<NBTMessage<PACKET_TYPE>> {

	static class NBTHandler<PACKET_TYPE extends Enum> extends ChannelInboundHandlerAdapter {
	
		BaseNBTChannel<PACKET_TYPE> client;
		Side side;
	
		NBTHandler(BaseNBTChannel<PACKET_TYPE> client, Side side) {
			//System.out.printf("NBTHandler created for %s side of %s\n", side, client);
			this.client = client;
			this.side = side;
		}
	
		//protected void channelRead0(ChannelHandlerContext ctx, NBTMessage<PACKET_TYPE> msg) throws Exception {

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
			NBTMessage<PACKET_TYPE> msg;
			//System.out.printf("NBTHandler on %s side received object %s\n", side, obj);
			if (obj instanceof NBTMessage) {
				msg = (NBTMessage<PACKET_TYPE>)obj;
				//System.out.printf("NBTHandler on %s received %s %s\n", side, msg.type, msg.nbt);
				switch (side) {
					case SERVER:
						INetHandler net = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
						//System.out.printf("NBTHandler: NetHandler = %s\n", net);
						EntityPlayer player = ((NetHandlerPlayServer)net).playerEntity;
						client.onReceiveFromClient(msg.type, msg.nbt, player);
						break;
					case CLIENT:
						client.onReceiveFromServer(msg.type, msg.nbt);
						break;
				}
			}
		}
	
	}

}


