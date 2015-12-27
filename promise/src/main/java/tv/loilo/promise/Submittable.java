package tv.loilo.promise;

import java.util.concurrent.ExecutorService;

/**
 * Created by Junpei on 2015/06/16.
 */
public interface Submittable {

    Canceller submitOn(ExecutorService executorService, Object tag);


    Canceller submitOn(ExecutorService executorService);


    Canceller submit(Object tag);


    Canceller submit();
}
