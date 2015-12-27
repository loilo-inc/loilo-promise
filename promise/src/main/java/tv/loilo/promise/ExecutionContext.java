package tv.loilo.promise;

/**
 * Created by Junpei on 2015/08/21.
 */
public interface ExecutionContext extends TaggedCancelState {
    CloseableStack getScope();
}
