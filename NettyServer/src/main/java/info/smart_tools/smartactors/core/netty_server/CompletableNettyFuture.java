package info.smart_tools.smartactors.core.netty_server;


import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

import java.util.concurrent.CompletableFuture;

/**
 * Adapter from netty {@link Future} to java {@link CompletableFuture}
 * TODO: should we properly handle a call to {@link CompletableFuture#complete(Object)} method?
 *
 * @param <T> type of value returned by Future.
 */
public class CompletableNettyFuture<T> extends CompletableFuture<T> {
    private Future<T> nettyFuture;

    private CompletableNettyFuture(Future<T> nettyFuture) {
        this.nettyFuture = nettyFuture;
    }

    /**
     * Factory method for convenient wrapping of the given Future.
     *
     * @param nettyFuture netty future to be wrapped
     * @param <T>         type of value returned by Future
     * @return a {@link CompletableFuture} that wraps the given one
     */
    public static <T> CompletableFuture<T> from(Future<T> nettyFuture) {
        CompletableFuture<T> result = new CompletableNettyFuture<>(nettyFuture);

        nettyFuture.addListener(new FutureListener<T>() {
            @Override
            public void operationComplete(Future<T> future) throws Exception {
                if (future.isSuccess()) {
                    result.complete(future.getNow());
                } else {
                    result.completeExceptionally(future.cause());
                }
            }
        });

        return result;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean result = nettyFuture.cancel(mayInterruptIfRunning);
        super.cancel(mayInterruptIfRunning);
        return result;
    }

    @Override
    public boolean isDone() {
        return nettyFuture.isDone();
    }

    @Override
    public boolean isCancelled() {
        return nettyFuture.isCancelled();
    }

    @Override
    public boolean isCompletedExceptionally() {
        return !nettyFuture.isSuccess();
    }
}
