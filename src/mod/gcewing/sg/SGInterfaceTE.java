//------------------------------------------------------------------------------------------------
//
//   SG Craft - Base class for stargate computer interface tile entities
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import gcewing.sg.SGAddressing.AddressingError;

import static gcewing.sg.BaseBlockUtils.getTileEntityWorld;

public class SGInterfaceTE extends BaseTileEntity {

    public static Object[] prependArgs(Object... args) {
        int preLength = args.length - 1;
        Object[] post = (Object[]) args[preLength];
        Object[] xargs = new Object[preLength + post.length];
        System.arraycopy(args, 0, xargs, 0, preLength);
        System.arraycopy(post, 0, xargs, preLength, post.length);
        return xargs;
    }

    public SGBaseTE getBaseTE() {
        return SGBaseTE.get(worldObj, getPos().add(0, 1, 0));
    }

    public void rebroadcastNetworkPacket(Object packet) {
    }

    public SGBaseTE requireBaseTE() {
        SGBaseTE te = getBaseTE();
        if (te != null && te.isMerged)
            return te;
        throw new IllegalArgumentException("No stargate connected to interface");
    }

    public SGBaseTE requireIrisTE() {
        SGBaseTE te = requireBaseTE();
        if (te != null && te.hasIrisUpgrade)
            return te;
        else
            throw new IllegalArgumentException("No iris fitted to stargate");
    }

    String directionDescription(SGBaseTE te) {
        if (te.isConnected()) {
            if (te.isInitiator)
                return "Outgoing";
            else
                return "Incoming";
        } else
            return "";
    }

    public CIStargateState ciStargateState() {
        SGBaseTE te = getBaseTE();
        if (te != null)
            return new CIStargateState(te.sgStateDescription(), te.numEngagedChevrons, directionDescription(te));
        else
            return new CIStargateState("Offline", 0, "");
    }

    public double ciEnergyAvailable() {
        SGBaseTE te = getBaseTE();
        if (te != null)
            return te.availableEnergy();
        else
            return 0;
    }

    public double ciEnergyToDial(String address) {
        SGBaseTE te = requireBaseTE();
        try {
            address = SGAddressing.normalizeAddress(address);
            SGBaseTE dte = SGAddressing.findAddressedStargate(address, getTileEntityWorld(te));
            if (dte == null)
                throw new IllegalArgumentException("No stargate at address " + address);
            double distanceFactor = SGBaseTE.distanceFactorForCoordDifference(te, dte);
            return SGBaseTE.energyToOpen * distanceFactor;
        } catch (AddressingError e) {
            System.out.printf("SGBaseTE.ciEnergyToDial: caught %s\n", e);
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public String ciLocalAddress() {
        SGBaseTE te = getBaseTE();
        try {
            if (te != null)
                return te.getHomeAddress();
            else
                return "";
        } catch (AddressingError e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public String ciRemoteAddress() {
        SGBaseTE te = requireBaseTE();
        try {
            if (te.connectedLocation != null)
                return SGAddressing.addressForLocation(te.connectedLocation);
            else
                return "";
        } catch (AddressingError e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public void ciDial(String address) {
        SGBaseTE te = requireBaseTE();
        address = SGAddressing.normalizeAddress(address);
        String error = te.connect(address, null);
        if (error != null)
            throw new IllegalArgumentException(error);
    }

    public void ciDisconnect() {
        SGBaseTE te = requireBaseTE();
        String error = te.attemptToDisconnect(null);
        if (error != null)
            throw new IllegalArgumentException(error);
    }

    public String ciIrisState() {
        SGBaseTE te = getBaseTE();
        if (te != null && te.hasIrisUpgrade)
            return te.irisStateDescription();
        else
            return "Offline";
    }

    public void ciOpenIris() {
        requireIrisTE().openIris();
    }

    public void ciCloseIris() {
        requireIrisTE().closeIris();
    }

    public void ciSendMessage(Object[] args) {
        SGBaseTE te = requireBaseTE();
        String error = te.sendMessage(args);
        if (error != null)
            throw new IllegalArgumentException(error);
    }

    public static class CIStargateState {
        public final String state;
        public final int chevrons;
        public final String direction;

        public CIStargateState(String state, int chevrons, String direction) {
            this.state = state;
            this.chevrons = chevrons;
            this.direction = direction;
        }
    }

}
