package chatbot.api.common.config;

import chatbot.api.common.security.CustomUserDetailsService;
import chatbot.api.common.security.oauth2.*;
import chatbot.api.common.security.oauth2.jwt.TokenAuthenticationFilter;
import chatbot.api.user.domain.KakaoUserInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Autowired
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .cors()
                .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .csrf()
                .disable()
            .formLogin()
                .disable()
            .httpBasic()
                .disable()

            .exceptionHandling()//보호된 리소스에 접근할려고 하는 경우
                .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                .and()

            .authorizeRequests()
                .antMatchers("/",
                    "/error",
                    "/favicon.ico",
                    "/**/*.png",
                    "/**/*.gif",
                    "/**/*.svg",
                    "/**/*.jpg",
                    "/**/*.html",
                    "/**/*.css",
                    "/**/*.js")
                    .permitAll()

                //허용하는 url 설정
                .antMatchers("/auth/**", "/oauth2/**")
                    .permitAll()
                //이외의 url은 인증 요청
                .anyRequest()
                    .authenticated()
                .and()

            .oauth2Login()//외부 서버에 대한 인증 요청을 트리거하기 위해 사용하는 엔드포인트
                .authorizationEndpoint() //자원 소유자로부터 권한을 얻어오기 위해 사용
                    .baseUri("/oauth2/authorize")
                    .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
                    .and()
                .redirectionEndpoint()//외부 공급자와의 인증 후 리디렉션되는 endpoint
                    .baseUri("/oauth2/callback/*")
                    .and()
                //.tokenEndpoint()
                //    .accessTokenResponseClient()
                //    .and()
                .userInfoEndpoint()//사용자 정보를 얻기 위해 활용할 수 있는 위치
                    .customUserType(KakaoUserInfoDto.class, "kakao")
                    .userService(this.oauth2UserService())
                    .and()
                .successHandler(oAuth2AuthenticationSuccessHandler) //작업이 성공 시에 호출
                .failureHandler(oAuth2AuthenticationFailureHandler); //작업이 실패 시에 호출
        // Add our custom Token based authentication filter
        //http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter();
    }

    /*private AuthorizationRequestRepository<OAuth2AuthorizationRequest> cookieAuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }*/

    private OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        return new CustomOAuth2UserService();
    }
}
