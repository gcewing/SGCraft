//------------------------------------------------------------------------------------------------
//
//   SG Craft - Open Computers - Stargate Wireless Endpoint
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.oc;

import net.minecraft.world.*;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.Packet;
import li.cil.oc.api.network.WirelessEndpoint;

import gcewing.sg.*;

public class OCWirelessEndpoint implements WirelessEndpoint {

    public static double forwardingStrength = 50;
    
    static boolean debugWireless = false;

    SGBaseTE te;
    
    public static void configure(BaseConfiguration config) {
        forwardingStrength = config.getDouble("opencomputers", "wirelessRebroadcastStrength", forwardingStrength);
    }
    
    public OCWirelessEndpoint(SGBaseTE te) {
        if (debugWireless)
            System.out.printf("SGCraft: OCSGWirelessEndpoint: added %s for %s\n", this, te);
        this.te = te;
        Network.joinWirelessNetwork(this);
    }
    
    public void remove() {
        if (debugWireless)
            System.out.printf("SGCraft: OCSGWirelessEndpoint: removed for %s\n", te);
        Network.leaveWirelessNetwork(this);
    }

    // --------------------------- WirelessEndpoint ---------------------------
    
    @Override public int x() {return te.getPos().getX();}
    @Override public int y() {return te.getPos().getY();}
    @Override public int z() {return te.getPos().getZ();}
    @Override public World world() {return te.getWorld();}
    
    @Override
    public void receivePacket(Packet packet, WirelessEndpoint sender) {
        if (debugWireless)
            System.out.printf("OCSGWirelessEndpoint.receivePacket: ttl %s from %s by %s\n",
                packet.ttl(), sender, te);
        if (packet.ttl() > 0) {
            SGBaseTE dte = te.getConnectedStargateTE();
            if (dte != null && dte.ocWirelessEndpoint != null) {
                if (debugWireless)
                    System.out.printf("OCSGWirelessEndpoint.receivePacket: forwarding to %s\n", dte);
                Network.sendWirelessPacket(dte.ocWirelessEndpoint, forwardingStrength, packet.hop());
            }
        }
    }

}
