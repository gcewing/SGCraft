package gcewing.sg;

import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface SoundSource {
    World getSoundWorld();

    BlockPos getSoundPos();

    boolean isSoundActive(SoundEvent sound);

    default boolean isSoundRepeatable(SoundEvent sound) {
        return false;
    }

    default float getSoundPitch(SoundEvent sound) {
        return 1F;
    }

    default float getSoundVolume(SoundEvent sound) {
        return 1F;
    }

    @SideOnly(Side.CLIENT)
    default void updateSound(SoundEvent sound) {}
}
