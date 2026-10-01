package com.tronzap.sdk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

final class BoundedBodyHandler implements HttpResponse.BodyHandler<byte[]> {

    private final long maxBytes;

    BoundedBodyHandler(long maxBytes) {
        this.maxBytes = maxBytes;
    }

    @Override
    public HttpResponse.BodySubscriber<byte[]> apply(HttpResponse.ResponseInfo responseInfo) {
        return new Subscriber(maxBytes, responseInfo.statusCode());
    }

    static final class ResponseTooLargeException extends IOException {

        private static final long serialVersionUID = 1L;

        private final int statusCode;

        ResponseTooLargeException(int statusCode, long maxBytes) {
            super("response body exceeds " + maxBytes + " bytes");
            this.statusCode = statusCode;
        }

        int statusCode() {
            return statusCode;
        }
    }

    private static final class Subscriber implements HttpResponse.BodySubscriber<byte[]> {

        private final long maxBytes;
        private final int statusCode;
        private final CompletableFuture<byte[]> body = new CompletableFuture<>();
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private Flow.Subscription subscription;
        private long received;

        Subscriber(long maxBytes, int statusCode) {
            this.maxBytes = maxBytes;
            this.statusCode = statusCode;
        }

        @Override
        public CompletionStage<byte[]> getBody() {
            return body;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(List<ByteBuffer> items) {
            if (body.isDone()) {
                return;
            }
            for (ByteBuffer item : items) {
                received += item.remaining();
                if (received > maxBytes) {
                    subscription.cancel();
                    body.completeExceptionally(new ResponseTooLargeException(statusCode, maxBytes));
                    return;
                }
                byte[] chunk = new byte[item.remaining()];
                item.get(chunk);
                buffer.write(chunk, 0, chunk.length);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            body.completeExceptionally(throwable);
        }

        @Override
        public void onComplete() {
            body.complete(buffer.toByteArray());
        }
    }
}
