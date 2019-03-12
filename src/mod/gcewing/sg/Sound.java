package gcewing.sg;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Sound extends PositionedSound implements ITickableSound {
    private final SoundSource source;

    public Sound(SoundSource source, SoundEvent sound, SoundCategory category) {
        super(sound, category);
        this.source = source;
    }

    public Sound(SoundSource source, ResourceLocation sound, SoundCategory category) {
        super(sound, category);
        this.source = source;
    }

    @Override
    public boolean canRepeat() {
        return this.source.isSoundRepeatable(this.getEvent());
    }

    public World getWorld() {
        return this.source.getSoundWorld();
    }

    public BlockPos getPos() {
        return this.source.getSoundPos();
    }

    @Override
    public boolean isDonePlaying() {
        return !getWorld().isBlockLoaded(getPos()) || !this.source.isSoundActive(this.getEvent());
    }

    @Override
    public void update() {
        this.source.updateSound(this.getEvent());
    }

    @Override
    public float getXPosF() {
        return (float) getPos().getX();
    }

    @Override
    public float getYPosF() {
        return (float) getPos().getY();
    }

    @Override
    public float getZPosF() {
        return (float) getPos().getZ();
    }

    @Override
    public float getPitch() {
        return this.source.getSoundPitch(this.getEvent()) * this.sound.getPitch();
    }

    @Override
    public float getVolume() {
        return this.source.getSoundVolume(this.getEvent()) * this.sound.getVolume();
    }

    public SoundEvent getEvent() {
        return SoundEvent.REGISTRY.getObject(this.positionedSoundLocation);
    }
}
