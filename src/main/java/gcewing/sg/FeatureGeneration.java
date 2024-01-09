// ------------------------------------------------------------------------------------------------
//
// SG Craft - Map feature generation
//
// ------------------------------------------------------------------------------------------------

package gcewing.sg;

import static gcewing.sg.BaseUtils.getFieldDef;
import static gcewing.sg.BaseUtils.setField;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.world.gen.structure.ComponentScatteredFeaturePieces;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraftforge.event.terraingen.InitMapGenEvent;

public class FeatureGeneration {

    public static boolean augmentStructures = false;
    public static boolean debugStructures = false;

    static Field structureMap = getFieldDef(MapGenStructure.class, "structureMap", "field_75053_d");

    public static void configure(BaseConfiguration config) {
        augmentStructures = config.getBoolean("options", "augmentStructures", augmentStructures);
        debugStructures = config.getBoolean("debug", "debugStructures", debugStructures);
    }

    public static void onInitMapGen(InitMapGenEvent e) {
        if (augmentStructures) {
            switch (e.type) {
                case SCATTERED_FEATURE:
                    if (e.newGen instanceof MapGenStructure)
                        e.newGen = modifyScatteredFeatureGen((MapGenStructure) e.newGen);
                    else SGCraft.log.warn(
                            "SGCraft: FeatureGeneration: SCATTERED_FEATURE generator is not a MapGenStructure, cannot customise");
                    break;
            }
        }
    }

    static MapGenStructure modifyScatteredFeatureGen(MapGenStructure gen) {
        setField(gen, structureMap, new SGStructureMap());
        return gen;
    }

}

class SGStructureMap extends HashMap {

    @Override
    public Object put(Object key, Object value) {
        if (value instanceof StructureStart) augmentStructureStart((StructureStart) value);
        return super.put(key, value);
    }

    void augmentStructureStart(StructureStart start) {
        LinkedList oldComponents = start.getComponents();
        LinkedList newComponents = new LinkedList();
        for (Object comp : oldComponents) {
            if (comp instanceof ComponentScatteredFeaturePieces.DesertPyramid) {
                StructureBoundingBox box = ((StructureComponent) comp).getBoundingBox();
                if (FeatureGeneration.debugStructures) SGCraft.log.debug(
                        String.format(
                                "SGCraft: FeatureGeneration: Augmenting %s at (%s, %s)",
                                comp.getClass().getSimpleName(),
                                box.getCenterX(),
                                box.getCenterZ()));
                newComponents.add(new FeatureUnderDesertPyramid((ComponentScatteredFeaturePieces.DesertPyramid) comp));
            }
        }
        oldComponents.addAll(newComponents);
    }

}
