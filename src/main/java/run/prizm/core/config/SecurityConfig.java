package run.prizm.core.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import run.prizm.core.auth.constant.ApiEndpoint;
import run.prizm.core.auth.service.CustomOAuth2UserService;
import run.prizm.core.auth.service.oauth2.OAuth2FailureHandler;
import run.prizm.core.auth.service.oauth2.OAuth2SuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(AbstractHttpConfigurer::disable)  // CORS는 API Gateway에서 처리
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest()
                                               .permitAll())
            .oauth2Login(this::configureOAuth2Login);

        return http.build();
    }

    private void configureOAuth2Login(org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer<HttpSecurity> oauth2) {
        oauth2.loginPage(ApiEndpoint.USER_OAUTH2_LOGIN_PAGE.getPath())
              .authorizationEndpoint(authorization ->
                      authorization.baseUri(ApiEndpoint.USER_OAUTH2_AUTHORIZATION_BASE.getPath()))
              .redirectionEndpoint(redirection ->
                      redirection.baseUri(ApiEndpoint.USER_OAUTH2_REDIRECT_PATTERN.getPath()))
              .userInfoEndpoint(userInfo ->
                      userInfo.userService(customOAuth2UserService))
              .successHandler(oAuth2SuccessHandler)
              .failureHandler(oAuth2FailureHandler);
    }
}