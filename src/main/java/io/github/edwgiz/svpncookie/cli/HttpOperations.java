package io.github.edwgiz.svpncookie.cli;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.function.Function;

final class HttpOperations implements AutoCloseable {

    private static HttpClient createHttpClient() {
        final SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            throw new ExceptionInInitializerError(e);
        }
        // accept unknown certificates
        try {
            sslContext.init(null, new TrustManager[]{new MockX509ExtendedTrustManager()}, null);
        } catch (KeyManagementException e) {
            throw new ExceptionInInitializerError(e);
        }

        // remember cookies across the resuest chain
        final var cookieManager = new CookieManager();

        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .sslContext(sslContext)
                .cookieHandler(cookieManager)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    final HttpClient httpClient = createHttpClient();


    <R> R send(HttpRequest req, HttpResponseExtractor<R> extractor) {
        return send(req, (res) -> {
            final var contentLengthOpt = res.headers().firstValue("content-length").map(Integer::parseInt);
            if (contentLengthOpt.isPresent()) {
                final int contentLength = contentLengthOpt.get();
                try (final var is = res.body(); final var bodyScanner = new Scanner(is, StandardCharsets.UTF_8)) {
                    bodyScanner.useDelimiter("");
                    return extractor.apply(res, bodyScanner, contentLength);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        });
    }

    <R> R send(HttpRequest req, Function<HttpResponse<InputStream>, R> extractor) {
        try {
            final var res = httpClient.send(req, HttpResponse.BodyHandlers.ofInputStream());
            if (res.statusCode() == 200) {
                return extractor.apply(res);
            }
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException("Request failed by " + req.toString(), ex);
        }
        return null;
    }


    @Override
    public void close() {
        httpClient.close();
    }
}