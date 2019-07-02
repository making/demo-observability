package io.pivotal.jp.cf;

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

@SpringBootApplication
@RestController
public class CfApplication {

    public static void main(String[] args) {
        SpringApplication.run(CfApplication.class, args);
    }

    private final WebClient webClient;

    private final Props props;

    public CfApplication(WebClient.Builder builder, Props props) {
        this.webClient = builder.build();
        this.props = props;
    }

    @GetMapping
    public Flux<String> cf() {
        Flux<String> c = this.webClient.get().uri(this.props.c)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(String.class);
        Flux<String> f = this.webClient.get()
            .uri(this.props.f)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(String.class);
        return c.zipWith(f).map(t -> t.getT1() + "🖤🖤" + t.getT2())
            .log("cf");
    }

    @ConfigurationProperties(prefix = "urls")
    public static class Props {

        private final String c;

        private final String f;

        public Props(@DefaultValue("http://localhost:9001") String c, @DefaultValue("http://localhost:9002") String f) {
            this.c = c;
            this.f = f;
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
