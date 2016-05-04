package tv.loilo.promise.http;

import okhttp3.Response;

public interface ResponseFilter<TOut> {
    TOut pass(Response response) throws Exception;
}
