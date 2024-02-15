package gcewing.sg;

public enum EnumWorldBlockLayer {

    SOLID("Solid"),
    CUTOUT_MIPPED("Mipped Cutout"),
    CUTOUT("Cutout"),
    TRANSLUCENT("Translucent");

    private final String layerName;

    EnumWorldBlockLayer(String layerNameIn) {
        this.layerName = layerNameIn;
    }

    public String toString() {
        return this.layerName;
    }
}
