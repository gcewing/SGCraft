//------------------------------------------------------------------------------------------------
//
//   SG Craft - Packet Handling
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.io.*;

import net.minecraft.entity.player.*;
import net.minecraft.nbt.*;
import net.minecraft.world.*;
import net.minecraft.tileentity.*;

//import net.minecraft.item.Item;
//import net.minecraft.item.ItemStack;

enum PacketType {
    SetHomeAddress, ConnectOrDisconnect, EnteredAddress;
}

public class SGChannel extends BaseNBTChannel<PacketType> {

    static SGChannel network;

    public SGChannel(String channelName) {
        super(channelName);
        network = this;
    }

    @Override
    public void onReceiveFromClient(PacketType type, NBTTagCompound nbt, EntityPlayer player) {
        switch (type) {
            case SetHomeAddress:
                //handleSetHomeAddressFromClient(nbt, player);
                break;
            case ConnectOrDisconnect:
                handleConnectOrDisconnectFromClient(nbt, player);
                break;
            case EnteredAddress:
                handleEnteredAddress(nbt, player);
                break;
        }
    }
    
    static NBTTagCompound nbtWithCoords(TileEntity te) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("x", te.xCoord);
        nbt.setInteger("y", te.yCoord);
        nbt.setInteger("z", te.zCoord);
        return nbt;
    }
    
    public static void sendConnectOrDisconnectToServer(SGBaseTE te, String address) {
        NBTTagCompound nbt = nbtWithCoords(te);
        nbt.setString("address", address);
        network.sendToServer(PacketType.ConnectOrDisconnect, nbt);
    }
    
    static void handleConnectOrDisconnectFromClient(NBTTagCompound nbt, EntityPlayer player) {
        SGBaseTE te = SGBaseTE.at(player.worldObj, nbt);
        String address = nbt.getString("address");
        te.connectOrDisconnect(address, player);
    }
    
    public static void sendEnteredAddressToServer(DHDTE te, String address) {
        //System.out.printf("SGChannel.sendEnteredAddressToServer: %s\n", address);
        NBTTagCompound nbt = nbtWithCoords(te);
        nbt.setString("address", address);
        network.sendToServer(PacketType.EnteredAddress, nbt);
    }
    
    static void handleEnteredAddress(NBTTagCompound nbt, EntityPlayer player) {
        DHDTE te = DHDTE.at(player.worldObj, nbt);
        String address = nbt.getString("address");
        //System.out.printf("SGChannel.handleEnteredAddress: %s\n", address);
        te.setEnteredAddress(address);
    }
    
    public static void sendSetHomeAddressToServer(SGBaseTE te, String address) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("x", te.xCoord);
        nbt.setInteger("y", te.yCoord);
        nbt.setInteger("z", te.zCoord);
        nbt.setString("address", address);
        network.sendToServer(PacketType.SetHomeAddress, nbt);
    }

//	void handleSetHomeAddressFromClient(NBTTagCompound nbt, EntityPlayer player) {
//		int x = nbt.getInteger("x");
//		int y = nbt.getInteger("y");
//		int z = nbt.getInteger("z");
//		String address = nbt.getString("address");
//		System.out.printf("SGChannel.handleSetHomeAddressFromClient: (%d,%d,%d) '%s' \n", x, y, z, address);
//		SGBaseTE te = SGBaseTE.at(player.worldObj, x, y, z);
//		if (te != null)
//			te.setHomeAddress(address);
//	}
        
}
