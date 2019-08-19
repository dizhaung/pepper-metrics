package com.pepper.metrics.ds.prometheus;

import com.pepper.metrics.core.MeterRegistryFactory;
import com.pepper.metrics.core.extension.SpiMeta;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * @author zhangrongbincool@163.com
 * @date 19-8-9
 */
@SpiMeta(name = "promMeterRegistryFactory")
public class PrometheusMeterRegistryFactory implements MeterRegistryFactory {
    private static final PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    static {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(9146), 0);
            server.createContext("/metrics", httpExchange -> {
                String response = prometheusRegistry.scrape();
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });
            new Thread(server::start).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MeterRegistry createMeterRegistry() {
        return prometheusRegistry;
    }
}
