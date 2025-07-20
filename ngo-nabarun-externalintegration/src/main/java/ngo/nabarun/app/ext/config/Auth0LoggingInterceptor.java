package ngo.nabarun.app.ext.config;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import okio.Buffer;

/**
 * Custom OkHttp interceptor to log requests and responses similar to RestTemplate interceptor.
 */
@Slf4j
public class Auth0LoggingInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Add Content-Type if missing
        Request.Builder requestBuilder = originalRequest.newBuilder();
        if (originalRequest.header("Content-Type") == null) {
            requestBuilder.header("Content-Type", "application/json");
        }

        // Add X-Correlation-ID
        String correlationId = UUID.randomUUID().toString();
        requestBuilder.header("X-Correlation-ID", correlationId);

        Request request = requestBuilder.build();

        // Log request
        logRequest(request);

        // Proceed with request
        Response response = chain.proceed(request);

        // Log response
        logResponse(request, response);

        return response;
    }

    private void logRequest(Request request) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("===========================request begin================================================");
            log.debug("Method      : {}", request.method());
            log.debug("URL         : {}", request.url());
            Headers headers = request.headers();

            // Mask Authorization token if present
            Headers.Builder safeHeaders = headers.newBuilder();
            if (headers.get("Authorization") != null) {
                safeHeaders.set("Authorization", "<token_type> <access_token_value>");
            }

            log.debug("Headers     : {}", safeHeaders.build());

            if (request.body() != null) {
                Buffer buffer = new Buffer();
                request.body().writeTo(buffer);
                log.debug("Request body: {}", buffer.readString(StandardCharsets.UTF_8));
            }

            log.debug("==========================request end===================================================");
        }
    }

    private void logResponse(Request request, Response response) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("============================response begin==========================================");
            log.debug("URL         : {}", request.url());
            log.debug("Status code : {}", response.code());
            log.debug("Message     : {}", response.message());
            log.debug("Headers     : {}", response.headers());

            if (response.body() != null) {
                //MediaType contentType = response.body().contentType();
                String content = response.peekBody(Long.MAX_VALUE).string(); // safe peek
                log.debug("Response body: {}", content);
            }

            log.debug("============================response end============================================");
        }
    }
}
