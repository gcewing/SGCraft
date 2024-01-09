// ------------------------------------------------------------------------------------------------
//
// SG Craft - Open Computers - Stargate Wireless Endpoint
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg.oc;

import net.minecraft.world.World;

import gcewing.sg.BaseConfiguration;
import gcewing.sg.SGBaseTE;
import gcewing.sg.SGCraft;
import li.cil.oc.api.Network;
import li.cil.oc.api.network.Packet;
import li.cil.oc.api.network.WirelessEndpoint;

public class OCWirelessEndpoint implements WirelessEndpoint {

    public static double forwardingStrength = 50;

    static boolean debugWireless = false;

    SGBaseTE te;

    public static void configure(BaseConfiguration config) {
        forwardingStrength = config.getDouble("opencomputers", "wirelessRebroadcastStrength", forwardingStrength);
    }

    public OCWirelessEndpoint(SGBaseTE te) {
        if (debugWireless) SGCraft.log.debug(String.format("OCSGWirelessEndpoint: added %s for %s", this, te));
        this.te = te;
        Network.joinWirelessNetwork(this);
    }

    public void remove() {
        if (debugWireless) SGCraft.log.debug(String.format("OCSGWirelessEndpoint: removed for %s", te));
        Network.leaveWirelessNetwork(this);
    }

    @Override
    public int x() {
        return te.xCoord;
    }

    @Override
    public int y() {
        return te.yCoord;
    }

    @Override
    public int z() {
        return te.zCoord;
    }

    @Override
    public World world() {
        return te.getWorldObj();
    }

    @Override
    public void receivePacket(Packet packet, WirelessEndpoint sender) {
        if (debugWireless) SGCraft.log.debug(
                String.format("OCSGWirelessEndpoint.receivePacket: ttl %s from %s by %s", packet.ttl(), sender, te));
        if (packet.ttl() > 0) {
            SGBaseTE dte = te.getConnectedStargateTE();
            if (dte != null && dte.ocWirelessEndpoint != null) {
                if (debugWireless)
                    SGCraft.log.debug(String.format("OCSGWirelessEndpoint.receivePacket: forwarding to %s", dte));
                Network.sendWirelessPacket(dte.ocWirelessEndpoint, forwardingStrength, packet.hop());
            }
        }
    }

}
