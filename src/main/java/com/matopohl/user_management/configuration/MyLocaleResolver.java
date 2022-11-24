package com.matopohl.user_management.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Configuration
public class MyLocaleResolver extends AcceptHeaderLocaleResolver {

    @Value("${my.locale.default:en}")
    private String defaultLocale;

    @Value("${my.locale.accept:en}")
    private List<String> acceptLocales;

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        Locale.setDefault(Objects.requireNonNull(getDefaultLocale()));

        String lang = request.getParameter("lang");

        if(StringUtils.hasLength(lang)) {
            return Locale.forLanguageTag(lang);
        }

        String acceptLanguage = request.getHeader("Accept-Language");

        if(StringUtils.hasLength(acceptLanguage)) {
            List<Locale.LanguageRange> list = Locale.LanguageRange.parse(acceptLanguage.replace("_", "-"));

            return Locale.lookup(list, getSupportedLocales());
        }

        return Locale.getDefault();
    }

    @Bean
    public LocaleResolver localeResolver() {
        List<Locale> acceptLocale = acceptLocales.stream().map(StringUtils::parseLocale).filter(Objects::nonNull).toList();

        MyLocaleResolver localeResolver = new MyLocaleResolver();

        localeResolver.setDefaultLocale(StringUtils.parseLocale(defaultLocale));
        localeResolver.setSupportedLocales(acceptLocale);

        return localeResolver;
    }

}