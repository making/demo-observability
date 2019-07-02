package io.pivotal.jp.f;

import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@SpringBootApplication
@RestController
public class FApplication {

    public static void main(String[] args) {
        SpringApplication.run(FApplication.class, args);
    }

    private static final String[] hearts = {"💚", "💛", "💜", "🧡", "💙", "💖"};

    @GetMapping
    public Flux<String> f() {
        final int i = (int) (Math.random() * hearts.length);
        final String heart = hearts[i];
        return Flux.fromArray((heart + heart + heart + heart + heart + heart + heart + heart + "\n" +
            heart + heart + heart + heart + heart + heart + heart + heart + "\n" +
            heart + heart + "🖤🖤🖤🖤🖤🖤\n" +
            heart + heart + heart + heart + heart + heart + heart + heart + "\n" +
            heart + heart + heart + heart + heart + heart + heart + heart + "\n" +
            heart + heart + "🖤🖤🖤🖤🖤🖤\n" +
            heart + heart + "🖤🖤🖤🖤🖤🖤\n" +
            heart + heart + "🖤🖤🖤🖤🖤🖤\n" +
            heart + heart + "🖤🖤🖤🖤🖤🖤")
            .split("\n"))
            .log("f");
    }

    @Bean
    public MeterFilter meterFilter() {
        return MeterFilter.deny(id -> {
            String uri = id.getTag("uri");
            return uri != null && (uri.startsWith("/actuator") || uri.startsWith("/cloudfoundryapplication"));
        });
    }
}
