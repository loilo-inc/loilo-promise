package tv.loilo.promise;

import java.util.concurrent.ExecutorService;

/**
 * Created by Junpei on 2015/06/16.
 */
public interface Submittable {

    Canceller submitOn(final ExecutorService executorService, final Object tag);


    Canceller submitOn(final ExecutorService executorService);


    Canceller submit(final Object tag);


    Canceller submit();
}
