package io.pivotal.jp.push;

import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

import java.util.function.Function;

@SpringBootApplication
@RestController
public class PushApplication {

    private final WebClient webClient;

    private final Props props;

    public PushApplication(WebClient.Builder builder, Props props) {
        this.webClient = builder.build();
        this.props = props;
    }

    public static void main(String[] args) {
        SpringApplication.run(PushApplication.class, args);
    }

    @GetMapping
    public Flux<String> push() {
        Flux<String> p = this.webClient.get().uri(this.props.p)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(String.class);
        Flux<String> u = this.webClient.get()
            .uri(this.props.u)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(String.class);
        Flux<String> s = this.webClient.get()
            .uri(this.props.s)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(String.class);
        Flux<String> h = this.webClient.get()
            .uri(this.props.h)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(String.class);
        final Function<Tuple2<String, String>, String> concat = t -> t.getT1() + "🖤🖤" + t.getT2();
        return p.zipWith(u).map(concat)
            .zipWith(s).map(concat)
            .zipWith(h).map(concat)
            .log("push");
    }

    @ConfigurationProperties(prefix = "urls")
    public static class Props {

        private final String p;

        private final String u;

        private final String s;

        private final String h;

        public Props(@DefaultValue("http://localhost:9004") String p,
                     @DefaultValue("http://localhost:9005") String u,
                     @DefaultValue("http://localhost:9006") String s,
                     @DefaultValue("http://localhost:9007") String h) {
            this.p = p;
            this.u = u;
            this.s = s;
            this.h = h;
        }
    }

    @Configuration
    public static class Config {

        @Bean
        public MeterFilter meterFilter() {
            return MeterFilter.deny(id -> {
                String uri = id.getTag("uri");
                return uri != null && (uri.startsWith("/actuator") || uri.startsWith("/cloudfoundryapplication"));
            });
        }
    }
}
