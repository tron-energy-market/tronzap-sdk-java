package com.tronzap.sdk;

import com.tronzap.sdk.exception.ConnectionException;
import com.tronzap.sdk.exception.NetworkException;
import com.tronzap.sdk.exception.SslException;
import com.tronzap.sdk.exception.RequestTimeoutException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;
import java.nio.channels.UnresolvedAddressException;
import javax.net.ssl.SSLException;

final class NetworkErrors {

    private static final int MAX_CAUSE_DEPTH = 16;

    private NetworkErrors() {
    }

    static NetworkException classify(Throwable failure) {
        String detail = describe(failure);
        if (hasCause(failure, SSLException.class)) {
            return new SslException("TLS error: " + detail, failure);
        }
        if (hasCause(failure, HttpTimeoutException.class) || hasCause(failure, SocketTimeoutException.class)) {
            return new RequestTimeoutException("request timed out: " + detail, failure);
        }
        if (hasCause(failure, ConnectException.class)
                || hasCause(failure, UnknownHostException.class)
                || hasCause(failure, UnresolvedAddressException.class)
                || hasCause(failure, NoRouteToHostException.class)
                || hasCause(failure, PortUnreachableException.class)) {
            return new ConnectionException("connection failed: " + detail, failure);
        }
        return new NetworkException("network error: " + detail, failure);
    }

    private static boolean hasCause(Throwable failure, Class<? extends Throwable> type) {
        Throwable current = failure;
        for (int depth = 0; current != null && depth < MAX_CAUSE_DEPTH; depth++) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static String describe(Throwable failure) {
        Throwable current = failure;
        for (int depth = 0; current != null && depth < MAX_CAUSE_DEPTH; depth++) {
            if (current.getMessage() != null && !current.getMessage().isBlank()) {
                return current.getMessage();
            }
            current = current.getCause();
        }
        return failure.getClass().getName();
    }
}
