//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.8 - Data Channel Networking
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.lang.annotation.*;

import io.netty.buffer.*;
import io.netty.channel.*;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.*;
import net.minecraft.network.*;

import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.common.network.*;
import net.minecraftforge.fml.common.network.FMLOutboundHandler.OutboundTarget;
import net.minecraftforge.fml.relauncher.*;

public class BaseDataChannel {

    public String name;
    public List handlers = new ArrayList();
    protected EnumMap<Side, FMLEmbeddedChannel> pipes;

    public BaseDataChannel(String name, Object... handlers) {
        this.name = name;
        ChannelHandler handler = new DataHandler(this);
        pipes = NetworkRegistry.INSTANCE.newChannel(name, handler);
        this.handlers.add(this);
        for (Object h : handlers)
            this.handlers.add(h);
    }
    
    protected ChannelOutput openTarget(String message, Side fromSide, OutboundTarget target) {
        return openTarget(message, fromSide, target, null);
    }

    protected ChannelOutput openTarget(String message, Side fromSide, OutboundTarget target, Object arg) {
        ChannelOutput out = new DataPacket(this, fromSide, target, arg);
        out.writeUTF(message);
        return out;
    }
    
    public ChannelOutput openServer(String message) {
        return openTarget(message, Side.CLIENT, OutboundTarget.TOSERVER);
    }
    
    public ChannelOutput openPlayer(EntityPlayer player, String message) {
        return openTarget(message, Side.SERVER, OutboundTarget.PLAYER, player);
    }
    
    public ChannelOutput openAllPlayers(String message) {
        return openTarget(message, Side.SERVER, OutboundTarget.ALL);
    }
    
    public ChannelOutput openAllAround(NetworkRegistry.TargetPoint point, String message) {
        return openTarget(message, Side.SERVER, OutboundTarget.ALLAROUNDPOINT, point);
    }
    
    public ChannelOutput openDimension(int dimensionId, String message) {
        return openTarget(message, Side.SERVER, OutboundTarget.DIMENSION, dimensionId);
    }
    
    public ChannelOutput openServerContainer(String message) {
        ChannelOutput out = openServer(".container.");
        out.writeUTF(message);
        return out;
    }

    public ChannelOutput openClientContainer(EntityPlayer player, String message) {
        ChannelOutput out = openPlayer(player, ".container.");
        out.writeUTF(message);
        return out;
    }
    
    @ServerMessageHandler(".container.")
    public void onServerContainerMessage(EntityPlayer player, ChannelInput data) {
        String message = data.readUTF();
        doServerDispatch(player.openContainer, message, player, data);
    }
    
    @SideOnly(Side.CLIENT)
    @ClientMessageHandler(".container.")
    public void onClientContainerMessage(ChannelInput data) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        String message = data.readUTF();
        doClientDispatch(player.openContainer, message, data);
    }

    protected void onReceiveFromClient(EntityPlayer player, ChannelInput data) {
        String message = data.readUTF();
        for (Object h : handlers)
            if (serverDispatch(h, message, player, data))
                return;
        System.out.printf("No ServerMessageHandler for '%s' found in registered handlers of %s\n", message, this);
    }
    
    public static void doServerDispatch(Object handler, String message, EntityPlayer player, ChannelInput data) {
        if (!serverDispatch(handler, message, player, data))
            System.out.printf("No ServerMessageHandler for '%s' found in %s\n", message, handler.getClass().getName());
    }
    
    public static boolean serverDispatch(Object handler, String message, EntityPlayer player, ChannelInput data) {
        if (handler != null) {
            //Method meth = findHandlerMethod(handler, message, HandlerAnnotation.SERVER, serverHandlerCaches);
            Method meth = HandlerMap.SERVER.get(handler, message);
            if (meth != null) {
                try {
                    meth.invoke(handler, player, data);
                }
                catch (Exception e) {
                    throw new RuntimeException(
                        String.format("Exception while calling server-side handler %s.%s for message %s",
                            handler.getClass().getName(), meth.getName(), message), e);
                }
                return true;
            }
        }
        return false;
    }
    
    protected void onReceiveFromServer(ChannelInput data) {
        String message = data.readUTF();
        for (Object h : handlers)
            if (clientDispatch(h, message, data))
                return;
        System.out.printf("No ClientMessageHandler for '%s' found in registered handlers\n", message);
    }
    
    public static void doClientDispatch(Object handler, String message, ChannelInput data) {
        if (!clientDispatch(handler, message, data))
            System.out.printf("No ClientMessageHandler for '%s' found in %s\n", message, handler.getClass().getName());
    }

    public static boolean clientDispatch(Object handler, String message, ChannelInput data) {
        if (handler != null) {
            //Method meth = findHandlerMethod(handler, message, HandlerAnnotation.CLIENT, clientHandlerCaches);
            Method meth = HandlerMap.CLIENT.get(handler, message);
            if (meth != null) {
                try {
                    meth.invoke(handler, data);
                }
                catch (Exception e) {
                    throw new RuntimeException(
                        String.format("Exception while calling client-side handler %s.%s for message %s",
                            handler.getClass().getName(), meth.getName(), message), e);
                }
                return true;
            }
        }
        return false;
    }
    
    //------------------------------------------------------------------------------------------------
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ServerMessageHandler {
        String value();
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ClientMessageHandler {
        String value();
    }
    
    protected static enum HandlerMap {
    
        SERVER(ServerMessageHandler.class) {
            protected String annotationValue(Object a) {return ((ServerMessageHandler)a).value();}
        },
        
        CLIENT(ClientMessageHandler.class) {
            protected String annotationValue(Object a) {return ((ClientMessageHandler)a).value();}
        };
        
        protected Class type;
        protected ClassCache classCache = new ClassCache();
        
        HandlerMap(Class type) {
            this.type = type;
        }
        
        // This method exists because annotation classes can't be extended. :-(
        protected abstract String annotationValue(Object a);
        
        public Method get(Object handler, String message) {
            Class cls = handler.getClass();
            MethodCache cache = classCache.get(cls);
            Method meth = cache.get(message);
            if (meth == null) {
                //System.out.printf("BaseDataChannel: Looking for handler for %s in %s\n", message, cls);
                for (Method m : cls.getMethods()) {
                    Object a = m.getAnnotation(type);
                    if (a != null && annotationValue(a).equals(message)) {
                        cache.put(message, m);
                        meth = m;
                        break;
                    }
                }
            }
            return meth;
        }
    }

    protected static class MethodCache extends HashMap<String, Method> {
    }

    protected static class ClassCache extends HashMap<Class, MethodCache> {
    
        //@Override // Technically doesn't, because HashMao.get() is declared get(Object(), not get(K)
        public MethodCache get(Class key) {
            MethodCache result = super.get(key);
            if (result == null) {
                result = new MethodCache();
                put(key, result);
            }
            return result;
        }
    
    }

    //------------------------------------------------------------------------------------------------
    
    public interface ChannelInput extends DataInput {
        boolean     readBoolean();
        byte        readByte();
        char        readChar();
        double      readDouble();
        float       readFloat();
        void        readFully(byte[] b);
        void        readFully(byte[] b, int off, int len);
        int         readInt();
        String      readLine();
        long        readLong();
        short       readShort();
        int         readUnsignedByte();
        int         readUnsignedShort();
        String      readUTF();
        int         skipBytes(int n);
    }

    public interface ChannelOutput extends DataOutput {
        void        write(byte[] b);
        void        write(byte[] b, int off, int len);
        void        write(int b);
        void        writeBoolean(boolean v);
        void        writeByte(int v);
        void        writeBytes(String s);
        void        writeChar(int v);
        void        writeChars(String s);
        void        writeDouble(double v);
        void        writeFloat(float v);
        void        writeInt(int v);
        void        writeLong(long v);
        void        writeShort(int v);
        void        writeUTF(String s);
        void        close();
    }

    //------------------------------------------------------------------------------------------------
    
    static class ChannelInputStream extends ByteBufInputStream implements ChannelInput {
    
        public ChannelInputStream(ByteBuf buf) {
            super(buf);
        }
    
        public boolean  readBoolean()
            {try {return super.readBoolean();} catch (Exception e) {throw new RuntimeException(e);}}

        public byte readByte()
            {try {return super.readByte();} catch (Exception e) {throw new RuntimeException(e);}}

        public char readChar()
            {try {return super.readChar();} catch (Exception e) {throw new RuntimeException(e);}}

        public double readDouble()
            {try {return super.readDouble();} catch (Exception e) {throw new RuntimeException(e);}}

        public float readFloat()
            {try {return super.readFloat();} catch (Exception e) {throw new RuntimeException(e);}}

        public void readFully(byte[] b)
            {try {super.readFully(b);} catch (Exception e) {throw new RuntimeException(e);}}

        public void readFully(byte[] b, int off, int len)
            {try {super.readFully(b, off, len);} catch (Exception e) {throw new RuntimeException(e);}}

        public int readInt()
            {try {return super.readInt();} catch (Exception e) {throw new RuntimeException(e);}}

        public String readLine()
            {try {return super.readLine();} catch (Exception e) {throw new RuntimeException(e);}}

        public long readLong()
            {try {return super.readLong();} catch (Exception e) {throw new RuntimeException(e);}}

        public short readShort()
            {try {return super.readShort();} catch (Exception e) {throw new RuntimeException(e);}}

        public int readUnsignedByte()
            {try {return super.readUnsignedByte();} catch (Exception e) {throw new RuntimeException(e);}}

        public int readUnsignedShort()
            {try {return super.readUnsignedShort();} catch (Exception e) {throw new RuntimeException(e);}}

        public String readUTF()
            {try {return super.readUTF();} catch (Exception e) {throw new RuntimeException(e);}}

        public int skipBytes(int n)
            {try {return super.skipBytes(n);} catch (Exception e) {throw new RuntimeException(e);}}

    }
    
    //------------------------------------------------------------------------------------------------
    
    static class DataPacket implements ChannelOutput {
    
        ByteBufOutputStream out;
        BaseDataChannel channel;
        Side side;
        OutboundTarget target;
        Object arg;
    
        DataPacket(BaseDataChannel channel, Side side, OutboundTarget target, Object arg) {
            out = new ByteBufOutputStream(Unpooled.buffer());
            this.channel = channel;
            this.side = side;
            this.target = target;
            this.arg = arg;
        }
        
        public void write(byte[] b)
            {try {out.write(b);} catch (Exception e) {throw new RuntimeException(e);}}

        public void write(byte[] b, int off, int len)
            {try {out.write(b, off, len);} catch (Exception e) {throw new RuntimeException(e);}}

        public void write(int b)
            {try {out.write(b);} catch (Exception e) {throw new RuntimeException(e);}}

        public void writeBoolean(boolean v)
            {try {out.writeBoolean(v);} catch (Exception e) {throw new RuntimeException(e);}}

        public void writeByte(int v)
            {try {out.writeByte(v);} catch (Exception e) {throw new RuntimeException(e);}}

        public void writeBytes(String s)
            {try {out.writeBytes(s);} catch (Exception e) {throw new RuntimeException(e);}}

        public void writeChar(int v)
            {try {out.writeChar(v);} catch (Exception e) {throw new RuntimeException(e);}}

        public void writeChars(String s)
            {try {out.writeChars(s);} catch (Exception e) {throw new RuntimeException(e);}}

        public void writeDouble(double v)
            {try {out.writeDouble(v);} catch (Exception e) {throw new RuntimeException(e);}}

        public void writeFloat(float v)
            {try {out.writeFloat(v);} catch (Exception e) {throw new RuntimeException(e);}}

        public void writeInt(int v)
            {try {out.writeInt(v);} catch (Exception e) {throw new RuntimeException(e);}}

        public void writeLong(long v)
            {try {out.writeLong(v);} catch (Exception e) {throw new RuntimeException(e);}}

        public void writeShort(int v)
            {try {out.writeShort(v);} catch (Exception e) {throw new RuntimeException(e);}}

        public void writeUTF(String s)
            {try {out.writeUTF(s);} catch (Exception e) {throw new RuntimeException(e);}}

        public void close() {
            try {out.close();} catch (Exception e) {throw new RuntimeException(e);}
            ByteBuf payload = out.buffer();
            Packet pkt = new FMLProxyPacket(new PacketBuffer(payload), channel.name);
            FMLEmbeddedChannel pipe = channel.pipes.get(side);
            pipe.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(target);
            pipe.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(arg);
            pipe.writeAndFlush(pkt).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        }
        
    }

    //------------------------------------------------------------------------------------------------
    
    @ChannelHandler.Sharable
    protected static class DataHandler extends ChannelInboundHandlerAdapter {
    
        BaseDataChannel channel;
        
        DataHandler(BaseDataChannel channel) {
            this.channel = channel;
        }
        
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
            if (obj instanceof FMLProxyPacket)
                handleProxyPacket(ctx, (FMLProxyPacket)obj);
            else
                System.out.printf("BaseDataChannel.DataHandler: Received unexpected message type %s\n",
                    obj.getClass().getName());
        }
        
        protected void handleProxyPacket(ChannelHandlerContext ctx, FMLProxyPacket msg) {
            ChannelInput data = new ChannelInputStream(msg.payload());
            if (ctx.channel() == channel.pipes.get(Side.SERVER)) {
                INetHandler net = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
                EntityPlayer player = ((NetHandlerPlayServer)net).player;
                player.getServer().addScheduledTask(() -> channel.onReceiveFromClient(player, data));
            }
            else
                channel.onReceiveFromServer(data);
        }
        
    }

}
