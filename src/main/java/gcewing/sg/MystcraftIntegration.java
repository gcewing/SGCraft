//------------------------------------------------------------------------------------------------
//
//   SG Craft - Mystcraft integration
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import com.xcompwiz.mystcraft.network.NetworkUtils;
import com.xcompwiz.mystcraft.world.WorldProviderMyst;

public class MystcraftIntegration extends BaseSubsystem {

    public void sendAgeData(World world, EntityPlayer player) {
        if (world.provider instanceof WorldProviderMyst)
            NetworkUtils.sendAgeData(player, world.provider.dimensionId);
    }

}
