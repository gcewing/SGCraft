//------------------------------------------------------------------------------------------------
//
//   SG Craft - IC2 Stargate Power Unit Block
//
//------------------------------------------------------------------------------------------------

package gcewing.sg.ic2;

import gcewing.sg.*;

public class IC2PowerBlock extends PowerBlock<IC2PowerTE> {

    public IC2PowerBlock() {
        super(IC2PowerTE.class);
        setPrefixedIconNames("gcewing_sg:ic2PowerUnit", "bottom", "top", "side");
    }
    
}
