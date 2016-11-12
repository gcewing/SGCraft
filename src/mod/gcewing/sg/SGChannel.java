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
        SGBaseTE te = SGBaseTE.at(player.worldObj, pos);
        if (te != null)
            te.connectOrDisconnect(address, player);
    }
    
    public static void sendEnteredAddressToServer(DHDTE te, String address) {
        ChannelOutput data = channel.openServer("EnteredAddress");
        writeCoords(data, te);
        data.writeUTF(address);
        data.close();
    }
    
    @ServerMessageHandler("EnteredAddress")
    public void handleEnteredAddressFromClient(EntityPlayer player, ChannelInput data) {
        BlockPos pos = readCoords(data);
        String address = data.readUTF();
        DHDTE te = DHDTE.at(player.worldObj, pos);
        if (te != null)
            te.setEnteredAddress(address);
    }
    
    public static void writeCoords(ChannelOutput data, TileEntity te) {
        BaseBlockUtils.writeBlockPos(data, te.getPos());
    }
    
    public BlockPos readCoords(ChannelInput data) {
        return BaseBlockUtils.readBlockPos(data);
    }

}
