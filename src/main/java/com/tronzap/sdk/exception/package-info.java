/**
 * Exceptions thrown by {@link com.tronzap.sdk.TronzapClient}.
 *
 * <p>Every failure of an API call is a {@link com.tronzap.sdk.exception.TronzapException}, and
 * each one is exactly one of {@link com.tronzap.sdk.exception.ApiException}, {@link
 * com.tronzap.sdk.exception.HttpException}, {@link com.tronzap.sdk.exception.InvalidResponseException}
 * or {@link com.tronzap.sdk.exception.NetworkException}. Arguments rejected before a request is sent
 * raise the standard {@link java.lang.IllegalArgumentException} instead.
 */
package com.tronzap.sdk.exception;
