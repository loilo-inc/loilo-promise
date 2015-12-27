package tv.loilo.promise;

/**
 * Created by Junpei on 2015/12/17.
 */
public class Transfer<TData> implements TaggedCancelState {

    final Object mTag;
    private final CancelToken mCancelToken;
    private final TData mData;

    public Transfer(TaggedCancelState cancelState, TData data) {
        mCancelToken = cancelState.getCancelToken();
        mTag = cancelState.getTag();
        mData = data;
    }

    public TData getData() {
        return mData;
    }

    @Override
    public CancelToken getCancelToken() {
        return mCancelToken;
    }

    @Override
    public Object getTag() {
        return mTag;
    }
}
