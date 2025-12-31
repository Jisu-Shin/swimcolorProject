package com.swimcolor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableWebSecurity // 스프링 시큐리티 설정을 활성화
@EnableMethodSecurity // 메소드 단위 보안(@PreAuthorize 등)을 위해 추가
public class SecurityConfig {

    @Value("${admin.username}") // 프로퍼티에서 읽어올 값
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 설정 (API 개발 시 초기에는 disable 하는 경우가 많음)
                .csrf(csrf -> csrf.disable())

                // 2. 요청에 대한 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스(CSS, JS, 이미지)는 누구나 접근 가능해야 함
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                        // 관리자만 막는다.
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // [핵심] 그 외 나머지는 다 통과시킨다!
                        .anyRequest().permitAll()
                )

                // 3. 로그인 설정 (관리자 접속용)
                .formLogin(login -> login
                        .defaultSuccessUrl("/admin", true) // 로그인 성공 시 이동할 곳
                        .permitAll()
                )

                // 4. 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/admin/logout") // 로그아웃 주소 설정
                        .logoutSuccessUrl("/")      // 로그아웃 성공 시 메인으로
                        .invalidateHttpSession(true) // 세션 삭제
                        .deleteCookies("JSESSIONID") // 쿠키 삭제
                        .permitAll()
                );

        return http.build();
    }

    // 비밀번호 암호화 빈 (보안의 필수!)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 관리자 계정을 메모리에 직접 등록 (프로퍼티 대신 이거 사용!)
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username(adminUsername)
                // 비밀번호 "115415"를 암호화해서 저장
                .password(passwordEncoder().encode(adminPassword))
                .roles("ADMIN") // 권한 부여
                .build();

        return new InMemoryUserDetailsManager(admin);
    }
}