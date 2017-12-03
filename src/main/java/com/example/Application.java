/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package com.example;

import com.example.bookmarks.Account;
import com.example.bookmarks.AccountRepository;
import com.example.bookmarks.Bookmark;
import com.example.bookmarks.BookmarkRepository;
import com.fasterxml.classmate.TypeResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.EnvironmentEndpoint;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.LocalDate;
import java.util.*;

import static springfox.documentation.schema.AlternateTypeRules.newRule;

@Slf4j
@EnableSwagger2
@ComponentScan("gov.pmm")
@ComponentScan("com.example")
@SpringBootApplication
public class Application {

    public static final String[] required = {
            "endpoints.actuator.enabled",
            "endpoints.actuator.path",
            "endpoints.actuator.sensitive",
            "logging.level.root",
            "logging.level.com.example",
            "management.context-path",
            "management.security.enabled",
            "server.port",
            "spring.application.name",
            "spring.boot.admin.auto-registration",
            "spring.boot.admin.auto-deregistration",
            "spring.boot.admin.client.enabled",
            "spring.boot.admin.url",
            "spring.cloud.config.uri",
            "spring.cloud.config.server.bootstrap",
            "spring.cloud.config.server.prefix",
            "spring.datasource.initialize",
            "spring.datasource.url",
            "spring.datasource.username",
            "spring.datasource.password",
            "spring.jpa.database-platform",
            "spring.jpa.hibernate.ddl-auto",
            "spring.jpa.properties.hibernate.show_sql",
            "spring.jpa.properties.hibernate.use_sql_comments",
            "spring.jpa.properties.hibernate.format_sql",
            "spring.profiles.active",
            "user.timezone"
    };

    @Autowired
    private Environment env;

    @Autowired
    private EnvironmentEndpoint envEndPoint;
    //    @Bean
//    public Collection<Map.Entry<String, String>> actuatorEnvironmentProperties() {
//        final Collection<Map.Entry<String, String>> ret = new LinkedList<>();
//        if (envEndPoint == null) {
//            log.error("Spring EnvironmentEnpoint not injected");
//            return ret;
//        }
//        Map<String, Object> envMap = envEndPoint.invoke();
//        if (envMap == null) {
//            log.error("Spring EnvironmentEnpoint returned null");
//            return ret;
//        }
//        envMap.forEach((key, value) -> {
//            if (value instanceof Map) {
//                ret.addAll(toChildMap(key, (Map) value));
//            } else if (value instanceof Collection) {
//                ((Collection) value).forEach(it -> ret.add(toMapEntry(key, it)));
//            } else {
//                ret.add(toMapEntry(key, value));
//            }
//        });
//        log.info("========= Actuator Environment Properties =========");
//        ret.forEach((it) -> {
//            if (it.getValue() == null) log.warn("{}='null'", it.getKey());
//            else log.info("{}='{}'", it.getKey(), it.getValue());
//        });
//        log.info("===================================================");
//        return ret;
//    }
//
//    private Collection<Map.Entry<String, String>> toChildMap(String parentKey, Map map) {
//        final Collection<Map.Entry<String, String>> ret = new LinkedList<>();
//        map.forEach((key, value) -> {
//            String keyStr = parentKey + "." + ((key == null) ? "null" : key.toString());
//            if (value instanceof Map) {
//                ret.addAll(toChildMap(keyStr, (Map) value));
//            } else if (value instanceof Collection) {
//                ((Collection) value).forEach(it -> ret.add(toMapEntry(keyStr, it)));
//            } else {
//                ret.add(toMapEntry(keyStr, value));
//            }
//        });
//        return ret;
//    }
//
//    private Map.Entry<String, String> toMapEntry(String key, Object value) {
//        String keyStr = (key == null) ? "null" : key;
//        String valueStr="";
//        if (value == null) {
//            valueStr = "null";
//        } else if (value instanceof String) {
//            valueStr = (String) value;
//        } else if (value instanceof String[]) {
//            String.join(",",(String[]) value);
//        } else {
//            valueStr = value.toString();
//        }
//        return new AbstractMap.SimpleEntry<String, String>(keyStr, valueStr);
//    }
    @Autowired
    private TypeResolver typeResolver;

//    @PostConstruct
//    public void init(){
//        Collection<Map.Entry<String, String>> props = actuatorEnvironmentProperties();
//        if (props==null)
//            log.warn("ActuatorEnvironmentProperties was not auto wired");
//        else {
//            try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream("ActuatorEnv.txt") )) {
//                ;
//                props.forEach(it ->{
//                    try {
//                        os.write(String.format("%s='%s'\n",it.getKey(), it.getValue()).getBytes());
//                    } catch (IOException e) {
//                        log.error(e.getMessage());
//                    }
//                });
//            } catch (FileNotFoundException e) {
//                log.error(e.getMessage());
//            } catch (IOException e) {
//                log.error(e.getMessage());
//            }
//        }
//    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner init(AccountRepository accountRepository,
                           BookmarkRepository bookmarkRepository) {
        return (evt) -> Arrays.asList(
                "jhoeller,dsyer,pwebb,ogierke,rwinch,mfisher,mpollack,jlong".split(","))
                .forEach(
                        userId -> {
                            if (!accountRepository.findByUsername(userId).isPresent()) {
                                Account account = accountRepository.save(new Account(userId,
                                        "password"));
                                bookmarkRepository.save(new Bookmark(account,
                                        "http://bookmark.com/1/" + userId, "Description 1 for " + userId));
                                bookmarkRepository.save(new Bookmark(account,
                                        "http://bookmark.com/2/" + userId, "Description 2 for " + userId));
                            }
                        });
    }

    @Bean
    public Collection<Map.Entry<String, String>> requiredPropertiesStreamed() {
        final Collection<Map.Entry<String, String>> ret = new LinkedList<>();
        if (env == null) {
            log.error("Spring environment not injected");
            return ret;
        }
        Arrays.stream(required).forEach(key -> {
            ret.add(new AbstractMap.SimpleEntry<String, String>(key, env.getProperty(key)));
        });
        log.info("========= Required Environment properties =========");
        ret.forEach((it) -> {
            if (it.getValue() == null) log.warn("{}='null'", it.getKey());
            else log.info("{}='{}'", it.getKey(), it.getValue());
        });
        log.info("===================================================");
        return ret;
    }

    @Bean
    public Docket swaggerApi() {
        String basePackage = this.getClass().getPackage().getName();
        log.info("Configuring Swagger to scan from: {}", basePackage);
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage(basePackage))
                .paths(PathSelectors.any())
                .build()
                .pathMapping("/")
                .directModelSubstitute(LocalDate.class,
                        String.class)
                .genericModelSubstitutes(ResponseEntity.class)
                .alternateTypeRules(
                        newRule(typeResolver.resolve(DeferredResult.class,
                                typeResolver.resolve(ResponseEntity.class, WildcardType.class)),
                                typeResolver.resolve(WildcardType.class)))
                .useDefaultResponseMessages(false)
                .enableUrlTemplating(true)
                .tags(new Tag("Test Service", "All APIs for testing"))
                ;
    }
}
