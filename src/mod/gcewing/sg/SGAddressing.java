//------------------------------------------------------------------------------------------------
//
//   SG Craft - Conversions between coordinates and  stargate addresses
//
//------------------------------------------------------------------------------------------------
//
//   New Addressing Scheme
//   ---------------------
//
//   There are 36 symbols. An address consists of 7 or 9 symbols:
//
//      CCCCCCC
//
//   or
//
//      CCCCCCCDD
//
//   where DD encodes the dimension and CCCCCCC encode the coordinates within
//   the dimension. When connecting to a gate in the same dimension, DD may
//   be omitted, allowing a 7-chevron gate to be used.
//
//   The coordinate range is +/- sqrt(36**7))/2-1 = 139,967 chunks = 2,239,472 blocks
//
//   The dimension range is -36**2/2 to 36**2/2-1 = -648 to 647
//

package gcewing.sg;

import net.minecraft.tileentity.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;
import net.minecraft.server.*;

import net.minecraftforge.common.*;

public class SGAddressing {

    static boolean debugAddressing = false;

    static class AddressingError extends Exception {
        AddressingError(String s) {super(s);}
    }
    static AddressingError malformedAddressError = new AddressingError("Malformed stargate address");
    static AddressingError coordRangeError = new AddressingError("Coordinates out of stargate range");
    static AddressingError dimensionRangeError = new AddressingError("Dimension not reachable by stargate");

    public final static String symbolChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public final static int numSymbols = symbolChars.length();
    public final static int numCoordSymbols = 7;
    public final static int numDimensionSymbols = 2;
    
    public final static int maxAddressLength = numCoordSymbols + numDimensionSymbols;
    public final static int maxCoord = 139967;
    public final static int minCoord = -maxCoord;
    public final static int coordRange = maxCoord - minCoord + 1;
    public final static int minDimension = -648;
    public final static int maxDimension = 647;
    public final static int dimensionRange = maxDimension - minDimension + 1;
    //final static String padding = "?????????";
    final static String padding = "---------";
    final static long mc = coordRange + 2; // == 2 * maxCoord + 3;
    final static long pc = 93563;   //  (pc * qc) % mc == 1
    final static long qc = 153742;
    final static long md = dimensionRange + 2;
    final static long pd = 953;  //  (pd * qd) % md == 1
    final static long qd = 459;
    
    static boolean isValidSymbolChar(char c) {
        return isValidSymbolChar(String.valueOf(c));
    }

    static boolean isValidSymbolChar(String c) {
        return symbolChars.indexOf(c) >= 0;
    }
    
    static char symbolToChar(int i) {
        return symbolChars.charAt(i);
    }
    
    static int charToSymbol(char c) {
        return charToSymbol(String.valueOf(c));
    }

    static int charToSymbol(String c) {
        return symbolChars.indexOf(c);
    }
    
    static boolean validSymbols(String s) {
        for (int i = 0; i < s.length(); i++)
            if (charToSymbol(s.charAt(i)) < 0)
                return false;
        return true;
    }
    
    static void validateAddress(String s) throws AddressingError {
        int l = s.length();
        if ((l ==  numCoordSymbols || l == numCoordSymbols + numDimensionSymbols)
            && validSymbols(s))
                return;
        throw malformedAddressError;
    }
    
    public static String normalizeAddress(String address) {
        return address.replace("-", "").toUpperCase();
    }
    
    public static String relativeAddress(String targetAddress, String contextAddress)
        throws AddressingError
    {
        validateAddress(targetAddress);
        if (addressesInSameDimension(targetAddress, contextAddress))
            return coordSymbolsOf(targetAddress);
        else
            return targetAddress;
    }
    
    public static String normalizedRelativeAddress(String targetAddress, String contextAddress)
        throws AddressingError
    {
        return relativeAddress(normalizeAddress(targetAddress), contextAddress);
    }
    
    public static boolean addressesInSameDimension(String a1, String a2) {
        int l1 = a1.length(), l2 = a2.length();
        if (debugAddressing)
            System.out.printf("SGAddressing.addressesInSameDimension(%s,%s): %s %s %s %s\n",
                a1, a2, l1, l2, dimensionSymbolsOf(a1), dimensionSymbolsOf(a2));
        return l1 == numCoordSymbols || l2 == numCoordSymbols ||
            dimensionSymbolsOf(a1).equals(dimensionSymbolsOf(a2));
    }
    
    public static String coordSymbolsOf(String address) {
        return address.substring(0, numCoordSymbols);
    }
    
    public static String dimensionSymbolsOf(String address) {
        return address.substring(numCoordSymbols);
    }

    public static String addressForLocation(SGLocation loc) throws AddressingError {
        if (debugAddressing)
            System.out.printf("SGAddressing.addressForLocation: " +
                "coord range = %d to %d " +
                "dim range = %d to %d\n", minCoord, maxCoord, minDimension, maxDimension);
        int chunkx = loc.pos.getX() >> 4;
        int chunkz = loc.pos.getZ() >> 4;
        if (!inCoordRange(chunkx) || !inCoordRange(chunkz))
            throw coordRangeError;
        if (!inDimensionRange(loc.dimension))
            throw dimensionRangeError;
        long c = interleaveCoords(hash(chunkx - minCoord, pc, mc), hash(chunkz - minCoord, pc, mc));
        int d = hash(loc.dimension - minDimension, pd, md);
        if (debugAddressing)
            System.out.printf(
                "SGAddressing.addressForLocation: chunk (%d,%d) in dimension %d gives c = %s d = %d\n",
                chunkx, chunkz, loc.dimension, c, d);
        return intToSymbols(c, numCoordSymbols) + intToSymbols(d, numDimensionSymbols);
    }
    
    public static SGBaseTE findAddressedStargate(String address, World fromWorld) throws AddressingError {
        if (debugAddressing)
            System.out.printf("SGAddressing.findAddressedStargate: %s\n", address);
        validateAddress(address);
        String csyms;
        int dimension = fromWorld.provider.getDimensionId();
        if (address.length() == maxAddressLength) {
            csyms = address.substring(0, numCoordSymbols);
            String dsyms = address.substring(numCoordSymbols);
            dimension = minDimension + hash((int)intFromSymbols(dsyms), qd, md);
        }
        else {
            if (address.length() != numCoordSymbols)
                throw malformedAddressError;
            csyms = address;
        }
        long c = intFromSymbols(csyms);
        int[] xz = uninterleaveCoords(c);
        int chunkX = minCoord + hash(xz[0], qc, mc);
        int chunkZ = minCoord + hash(xz[1], qc, mc);
        if (debugAddressing)
            System.out.printf("SGAddressing.findAddressedStargate: c = %s chunk = (%d,%d) dimension = %d\n",
                c, chunkX, chunkZ, dimension);
        World toWorld = getWorld(dimension);
        if (toWorld != null) {
            Chunk chunk = toWorld.getChunkFromChunkCoords(chunkX, chunkZ);
            if (chunk != null)
                for (Object te : chunk.getTileEntityMap().values()) {
                    if (te instanceof SGBaseTE)
                        return (SGBaseTE)te;
                }
        }
        return null;
    }
    
    static long interleaveCoords(int x, int z) {
        if (debugAddressing)
            System.out.printf("SGAddressing.interleaveCoords: %d, %d\n", x, z);
        long p6 = 1;
        long c = 0;
        while (x > 0 || z > 0) {
            if (debugAddressing)
                System.out.printf("SGAddressing.interleaveCoords: half-digits %d %d\n", x % 6, z % 6);
            c += p6 * (x % 6); x /= 6; p6 *= 6;
            c += p6 * (z % 6); z /= 6; p6 *= 6;
        }
        return c;
    }
    
    static int[] uninterleaveCoords(long c) {
        int p6 = 1;
        int[] xy = {0, 0};
        while (c > 0) {
            xy[0] += p6 * (c % 6); c /= 6;
            xy[1] += p6 * (c % 6); c /= 6;
            p6 *= 6;
        }
        return xy;
    }
    
    static int hash(int i, long f, long m) {
        int h = (int)(((i + 1) * f) % m) - 1;
        if (debugAddressing)
            System.out.printf("SGAddressing.hash(%s, %s, %s) = %s\n", i, f, m, h);
        return h;
    }
                
    public static World getWorld(int dimension) {
        MinecraftServer s = MinecraftServer.getServer();
        return s.worldServerForDimension(dimension);
    }
    
    static boolean inCoordRange(int i) {
        return i >= minCoord && i <= maxCoord;
    }
    
    static boolean inDimensionRange(int i) {
        return i >= minDimension && i <= maxDimension;
    }

    static String intToSymbols(long i, int n) {
        String s = "";
        while (n-- > 0) {
            s = symbolToChar((int)(i % numSymbols)) + s;
            i /= numSymbols;
        }
        return s;
    }
    
    static long intFromSymbols(String s) {
        long i = 0;
        int n = s.length();
        for (int j = 0; j < n; j++) {
            char c = s.charAt(j);
            i = i * numSymbols + charToSymbol(c);
        }
        return i;
    }
    
    public static String padAddress(String address, String caret, int maxLength) {
        if (maxLength < numCoordSymbols)
            maxLength = numCoordSymbols;
        return formatAddress(address + padding.substring(address.length(), maxLength), " ", " ");
    }
    
    public static String formatAddress(String address, String sep1, String sep2) {
        String coord = address.substring(0, numCoordSymbols);
        String dimen = address.substring(numCoordSymbols);
        int i = (numCoordSymbols + 1) / 2;
        String result = coord.substring(0, i) + sep1 + coord.substring(i);
        if (dimen.length() > 0)
            result += sep2 + dimen;
        return result;
    }
    
    public static String localAddress(String address) {
        return address.substring(0, numCoordSymbols);
    }
        
}
