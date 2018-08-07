//------------------------------------------------------------------------------------------------
//
//   SG Craft - Packet Handling
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.*;

public class SGChannel extends BaseDataChannel {

    protected static BaseDataChannel channel;
    
    public SGChannel(String name) {
        super(name);
        channel = this;
//         for (Object h : handlers)
//             System.out.printf("SGChannel: handlers include %s\n", h);
    }

    public static void sendConnectOrDisconnectToServer(SGBaseTE te, String address) {
        ChannelOutput data = channel.openServer("ConnectOrDisconnect");
        writeCoords(data, te);
        data.writeUTF(address);
        data.close();
    }
    
    @ServerMessageHandler("ConnectOrDisconnect")
    public void handleConnectOrDisconnectFromClient(EntityPlayer player, ChannelInput data) {
        BlockPos pos = readCoords(data);
        String address = data.readUTF();
        SGBaseTE te = SGBaseTE.at(player.world, pos);
        if (te != null) {
            te.connectOrDisconnect(address, player);
        }
    }
    
    public static void sendClearAddressToServer(DHDTE te) {
        ChannelOutput data = channel.openServer("ClearAddress");
        writeCoords(data, te);
        data.close();
    }
    
    @ServerMessageHandler("ClearAddress")
    public void handleClearAddressFromClient(EntityPlayer player, ChannelInput data) {
        BlockPos pos = readCoords(data);
        DHDTE te = DHDTE.at(player.world, pos);
        if (te != null)
            te.clearAddress();
    }

    public static void sendUnsetSymbolToServer(DHDTE te) {
        ChannelOutput data = channel.openServer("UnsetSymbol");
        writeCoords(data, te);
        data.close();
    }

    @ServerMessageHandler("UnsetSymbol")
    public void handleUnsetSymbolFromClient(EntityPlayer player, ChannelInput data) {
        BlockPos pos = readCoords(data);
        if (player.world.isBlockLoaded(pos)) {
            DHDTE te = DHDTE.at(player.world, pos);
            if (te != null) {
                te.unsetSymbol();
            }
        }
    }

    public static void sendEnterSymbolToServer(DHDTE te, char symbol) {
        ChannelOutput data = channel.openServer("EnterSymbol");
        writeCoords(data, te);
        data.writeChar(symbol);
        data.close();
    }

    @ServerMessageHandler("EnterSymbol")
    public void handleEnterSymbolFromClient(EntityPlayer player, ChannelInput data) {
        BlockPos pos = readCoords(data);
        char symbol = data.readChar();
        if (player.world.isBlockLoaded(pos)) {
            DHDTE te = DHDTE.at(player.world, pos);
            if (te != null) {
                te.enterSymbol(symbol);
            }
        }
    }
    
    public static void writeCoords(ChannelOutput data, TileEntity te) {
        BaseBlockUtils.writeBlockPos(data, te.getPos());
    }
    
    public BlockPos readCoords(ChannelInput data) {
        return BaseBlockUtils.readBlockPos(data);
    }
}
