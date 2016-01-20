//------------------------------------------------------------------------------------------------
//
//   SG Craft - Computercraft Interface Peripheral
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.cc;

import com.google.common.base.Joiner;

import net.minecraft.tileentity.*;
import net.minecraft.world.*;
import net.minecraftforge.common.util.*;

import dan200.computercraft.api.lua.*;
import dan200.computercraft.api.peripheral.*;

import gcewing.sg.*;
import gcewing.sg.SGInterfaceTE.CIStargateState;

public class CCSGPeripheral implements IPeripheral {

    static CCMethod[] methods = {
    
        new SGMethod("stargateState") {
            Object[] call(SGInterfaceTE te, Object[] args) {
                CIStargateState result = te.ciStargateState();
                return new Object[] {result.state, result.chevrons, result.direction};
            }
        },
        
        new SGMethod("energyAvailable") {
            Object[] call(SGInterfaceTE te, Object[] args) {
                return new Object[] {te.ciEnergyAvailable()};
            }
        },
        
        new SGMethod("energyToDial", 1) {
            Object[] call(SGInterfaceTE te, Object[] args) {
                return new Object[] {te.ciEnergyToDial((String)args[0])};
            }
        },
        
        new SGMethod("localAddress") {
            Object[] call(SGInterfaceTE te, Object[] args) {
                return new Object[] {te.ciLocalAddress()};
            }
        },
        
        new SGMethod("remoteAddress") {
            Object[] call(SGInterfaceTE te, Object[] args) {
                return new Object[] {te.ciRemoteAddress()};
            }
        },
        
        new SGMethod("dial", 1) {
            Object[] call(SGInterfaceTE te, Object[] args) {
                te.ciDial((String)args[0]);
                return null;
            }
        },
        
        new SGMethod("disconnect") {
            Object[] call(SGInterfaceTE te, Object[] args) {
                te.ciDisconnect();
                return null;
            }
        },
        
//		new SGMethod("direction") {
//			Object[] call(SGInterfaceTE te, Object[] args) {
//				return new Object[] {te.ciDirection()};
//			}
//		},
        
        new SGMethod("irisState") {
            Object[] call(SGInterfaceTE te, Object[] args) {
                return new Object[] {te.ciIrisState()};
            }
        },
        
        new SGMethod("openIris") {
            Object[] call(SGInterfaceTE te, Object[] args) {
                te.ciOpenIris();
                return null;
            }
        },
        
        new SGMethod("closeIris") {
            Object[] call(SGInterfaceTE te, Object[] args) {
                te.ciCloseIris();
                return null;
            }
        },
        
        new SGMethod("sendMessage", -1) {
            Object[] call(SGInterfaceTE te, Object[] args) {
                te.ciSendMessage(args);
                return null;
            }
        },

    };
    
    World worldObj;
    int xCoord, yCoord, zCoord;
    
    public CCSGPeripheral(TileEntity te) {
        worldObj = te.getWorldObj();
        xCoord = te.xCoord;
        yCoord = te.yCoord;
        zCoord = te.zCoord;
        //System.out.printf("CCInterfaceTE: Created\n");
    }
    
//	SGBaseTE getBaseTE() {
//		return SGBaseTE.get(worldObj, xCoord, yCoord + 1, zCoord);
//	}
    
    CCInterfaceTE getInterfaceTE() {
        TileEntity te = worldObj.getTileEntity(xCoord, yCoord, zCoord);
        if (te instanceof CCInterfaceTE)
            return (CCInterfaceTE)te;
        else
            return null;
    }
    
    public String getType() {
        return "stargate";
    }
    
    public String[] getMethodNames() {
        String[] result = new String[methods.length];
        for (int i = 0; i < methods.length; i++)
            result[i] = methods[i].name;
        return result;
    }
    
    public Object[] callMethod(IComputerAccess cpu, ILuaContext ctx, int method, Object[] args)
        throws LuaException, InterruptedException
    {
        if (method >= 0 && method < methods.length)
            return CCMethodQueue.instance.invoke(cpu, ctx, this, methods[method], args);
        else
            throw new LuaException(String.format("Invalid method index %s", method));
    }
    
    public void attach(IComputerAccess cpu) {
        System.out.printf("CCSGPeripheral.attach: to %s\n", cpu);
        CCInterfaceTE te = getInterfaceTE();
        if (te != null)
            te.attachedComputers.add(cpu);
    }
    
    public void detach(IComputerAccess cpu) {
        System.out.printf("CCSGPeripheral.detach: from %s\n", cpu);
        CCInterfaceTE te = getInterfaceTE();
        if (te != null)
            te.attachedComputers.remove(cpu);
    }
    
    public boolean equals(IPeripheral other) {
        return this == other;
    }

}

//------------------------------------------------------------------------------------------------

abstract class SGMethod extends CCMethod {

    public SGMethod(String name) {
        super(name);
    }
    
    public SGMethod(String name, int nargs) {
        super(name, nargs);
    }

    @Override
    Object[] call(IComputerAccess cpu, ILuaContext ctx, Object target, Object[] args) {
        SGInterfaceTE te = ((CCSGPeripheral)target).getInterfaceTE();
        if (te != null)
            return call(te, args);
        else
            throw new IllegalArgumentException("Stargate interface failed internal diagnostics");
    }
    
    abstract Object[] call(SGInterfaceTE te, Object[] args);

}

