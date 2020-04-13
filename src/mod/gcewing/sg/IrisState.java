//------------------------------------------------------------------------------------------------
//
//   SG Craft - Iris states
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

public enum IrisState {
    Open, Closing, Closed, Opening;

    static final IrisState[] VALUES = values();

    public static IrisState valueOf(int i) {
        try {
            return VALUES[i];
        } catch (IndexOutOfBoundsException e) {
            return Open;
        }
    }

}
