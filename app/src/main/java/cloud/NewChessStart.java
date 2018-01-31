package cloud;

import kobi.chess.NewChessManager;
import sapphire.app.AppEntryPoint;
import sapphire.app.AppObjectNotCreatedException;
import sapphire.common.AppObjectStub;

import static sapphire.runtime.Sapphire.new_;

/**
 * Created by mbssaiakhil on 24/1/18.
 */

public class NewChessStart implements AppEntryPoint {
    @Override
    public AppObjectStub start() throws AppObjectNotCreatedException {
        return (AppObjectStub) new_(NewChessManager.class);
    }
}