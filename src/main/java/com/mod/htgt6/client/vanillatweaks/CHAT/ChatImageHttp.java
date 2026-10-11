package com.mod.htgt6.client.vanillatweaks.CHAT;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/** Client-only caller; HTTP connects to the exact IP addresses validated by its DNS resolver. */
final class ChatImageHttp {
    static final int MAX_BYTES = 8 * 1024 * 1024;
    private static final ScheduledThreadPoolExecutor DEADLINES = new ScheduledThreadPoolExecutor(1, task -> {
        Thread thread = new Thread(task, "HTGT6-chat-image-deadline");
        thread.setDaemon(true); return thread;
    });
    static { DEADLINES.setRemoveOnCancelPolicy(true); }
    private ChatImageHttp() {}

    static void checkAddress(URL url) throws IOException {
        checkUrl(url);
        resolvePublic(url.getHost());
    }

    private static void checkUrl(URL url) throws IOException {
        if (!("https".equalsIgnoreCase(url.getProtocol()) || "http".equalsIgnoreCase(url.getProtocol()))
                || url.getUserInfo() != null || url.getRef() != null
                || url.toExternalForm().length() > ChatImageLinks.MAX_URL
                || (url.getPort() != -1 && url.getPort() != 80 && url.getPort() != 443))
            throw new IOException("Unsupported URL");
    }

    static InetAddress[] resolvePublic(String host) throws UnknownHostException {
        InetAddress[] addresses = InetAddress.getAllByName(host);
        for (InetAddress address : addresses) requirePublic(address);
        return addresses;
    }

    static void requirePublic(InetAddress address) throws UnknownHostException {
        byte[] b = address.getAddress(); int a = b[0] & 255, second = b[1] & 255;
        boolean reserved = address.isAnyLocalAddress() || address.isLoopbackAddress()
                || address.isLinkLocalAddress() || address.isSiteLocalAddress() || address.isMulticastAddress();
        if (b.length == 4) {
            int third = b[2] & 255;
            reserved |= a == 0 || a >= 224 || (a == 100 && second >= 64 && second <= 127)
                    || (a == 192 && second == 0 && (third == 0 || third == 2))
                    || (a == 198 && (second == 18 || second == 19 || (second == 51 && third == 100)))
                    || (a == 203 && second == 0 && third == 113);
        } else {
            // Global unicast only; reject transition ranges with embedded IPv4 destinations.
            reserved |= (a & 224) != 32 || (a == 32 && second == 2)
                    || (a == 32 && second == 1 && ((b[2] & 255) <= 1
                    || ((b[2] & 255) == 13 && (b[3] & 255) == 184)));
        }
        if (reserved) throw new UnknownHostException("Non-public image address");
    }

    static byte[] download(String source) throws Exception {
        if (!ChatImageLinks.valid(source)) throw new IOException("Invalid image link");
        URL url = new URL(source);
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        RequestConfig config = RequestConfig.custom().setConnectTimeout(5000)
                .setSocketTimeout(5000).setConnectionRequestTimeout(5000).build();
        PoolingHttpClientConnectionManager connections = new PoolingHttpClientConnectionManager(
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", new SSLConnectionSocketFactory(SSLContexts.createDefault(),
                                SSLConnectionSocketFactory.STRICT_HOSTNAME_VERIFIER)).build(),
                ChatImageHttp::resolvePublic);
        connections.setDefaultConnectionConfig(ConnectionConfig.custom().setMessageConstraints(
                MessageConstraints.custom().setMaxHeaderCount(64).setMaxLineLength(8192).build()).build());
        // No system proxy, cookies, authentication cache, retries, or transparent decompression.
        try (CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(connections).setDefaultRequestConfig(config)
                .disableRedirectHandling().disableAutomaticRetries().disableCookieManagement()
                .disableAuthCaching().disableContentCompression().build()) {
            for (int redirects = 0; redirects <= 3; redirects++) {
                checkUrl(url);
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) throw new IOException("Image deadline exceeded");
                HttpGet request = new HttpGet(url.toURI());
                request.setHeader("User-Agent", "HTGT6-Chat/1.0");
                request.setHeader("Accept", "image/png,image/jpeg");
                ScheduledFuture<?> abort = DEADLINES.schedule(request::abort, remaining, TimeUnit.NANOSECONDS);
                try (CloseableHttpResponse response = client.execute(request)) {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 300 && status < 400) {
                        Header location = response.getFirstHeader("Location");
                        if (location == null) throw new IOException("Missing redirect");
                        url = new URL(url, location.getValue()); continue;
                    }
                    HttpEntity entity = response.getEntity();
                    if (status != 200 || entity == null || entity.getContentLength() > MAX_BYTES)
                        throw new IOException("Image response rejected");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    try (InputStream input = entity.getContent()) {
                        byte[] buffer = new byte[8192]; int read;
                        while ((read = input.read(buffer)) != -1) {
                            if (bytes.size() + read > MAX_BYTES || System.nanoTime() - deadline >= 0)
                                throw new IOException("Image limit exceeded");
                            bytes.write(buffer, 0, read);
                        }
                    }
                    return bytes.toByteArray();
                } finally { request.abort(); abort.cancel(false); }
            }
        }
        throw new IOException("Too many redirects");
    }
}
