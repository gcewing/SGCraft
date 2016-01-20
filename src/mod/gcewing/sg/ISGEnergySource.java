//------------------------------------------------------------------------------------------------
//
//   SG Craft - Stargate energy source interface
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

public interface ISGEnergySource {

    double availableEnergy();
    double drawEnergy(double amount);

}
