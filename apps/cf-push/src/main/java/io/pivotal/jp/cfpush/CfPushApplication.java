package io.pivotal.jp.cfpush;

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
public class CfPushApplication {

    private final WebClient webClient;

    private final Props props;

    public CfPushApplication(WebClient.Builder builder, Props props) {
        this.webClient = builder.build();
        this.props = props;
    }

    public static void main(String[] args) {
        SpringApplication.run(CfPushApplication.class, args);
    }

    @GetMapping
    public Flux<String> cfPush() {
        Flux<String> cf = this.webClient.get()
            .uri(this.props.cf)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(String.class);
        Flux<String> push = this.webClient.get()
            .uri(this.props.push)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(String.class);
        return cf.zipWith(push).map(t -> t.getT1() + "🖤🖤🖤🖤" + t.getT2())
            .map(s -> "🖤🖤" + s + "🖤🖤")
            .log("cf-push");
    }

    @ConfigurationProperties(prefix = "urls")
    public static class Props {

        private final String cf;

        private final String push;

        public Props(@DefaultValue("http://localhost:9001") String cf, @DefaultValue("http://localhost:9002") String push) {
            this.cf = cf;
            this.push = push;
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
