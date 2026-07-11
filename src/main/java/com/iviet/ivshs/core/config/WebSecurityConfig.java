package com.iviet.ivshs.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import javax.sql.DataSource;
import java.util.List;
import com.iviet.ivshs.core.properties.SecurityProperties;
import com.iviet.ivshs.shared.filter.JwtAuthenticationFilter;
import com.iviet.ivshs.shared.security.AuthEntryPointJwt;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
  private final UserDetailsService userDetailsService;
  private final DataSource dataSource;
  private final SecurityProperties securityProperties;
  private final AuthEntryPointJwt unauthorizedHandler;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    String allowedOrigins = securityProperties.getAllowedOrigins();
    configuration.setAllowedOriginPatterns(List.of(allowedOrigins.split(",")));

    String allowedMethods = securityProperties.getAllowedMethods();
    configuration.setAllowedMethods(List.of(allowedMethods.split(",")));

    String allowedHeaders = securityProperties.getAllowedHeaders();
    configuration.setAllowedHeaders(List.of(allowedHeaders.split(",")));

    configuration.setExposedHeaders(List.of("Authorization", "X-Trace-Id", "X-Scenario-Id"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration);
    return source;
  }

  @Bean
  @Order(1)
  public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher(new AntPathRequestMatcher("/api/**"))
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .authorizeHttpRequests(
            auth -> auth.requestMatchers(
                    new AntPathRequestMatcher("/api/v1/auth/signin"),
                    new AntPathRequestMatcher("/api/v1/auth/signup"),
                    new AntPathRequestMatcher("/api/v1/public/**")
                )
                .permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/**"))
                .authenticated())
        .logout(
            logout -> logout.logoutUrl("/api/v1/auth/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler((request, response, authentication) -> {
                  response.setStatus(HttpServletResponse.SC_OK);
                  response.getWriter()
                      .flush();
                }))
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
    http.cors(AbstractHttpConfigurer::disable)
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth -> auth
                .requestMatchers(
                    new AntPathRequestMatcher("/css/**"),
                    new AntPathRequestMatcher("/js/**"),
                    new AntPathRequestMatcher("/fonts/**"),
                    new AntPathRequestMatcher("/imgs/**"),
                    new AntPathRequestMatcher("/static/**"),
                    new AntPathRequestMatcher("/resources/**"),
                    new AntPathRequestMatcher("/login"),
                    new AntPathRequestMatcher("/error"),
                    new AntPathRequestMatcher("/error/**"))
                .permitAll()
                .anyRequest()
                .authenticated())
        .exceptionHandling(exception -> exception.accessDeniedPage("/error/403"))
        .formLogin(
            form -> form.loginPage("/login")
                .loginProcessingUrl("/loginAction")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll())
        .logout(
            logout -> logout.logoutUrl("/logout")
                .deleteCookies("JSESSIONID", "remember-me")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutSuccessUrl("/login?logout=true")
                .permitAll())
        .rememberMe(
            remember -> remember.tokenRepository(persistentTokenRepository())
                .tokenValiditySeconds(1 * 24 * 60 * 60)
                .userDetailsService(userDetailsService))
        .authenticationProvider(authenticationProvider());

    return http.build();
  }

  @Bean
  public PersistentTokenRepository persistentTokenRepository() {
    JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
    tokenRepository.setDataSource(dataSource);
    return tokenRepository;
  }
}
