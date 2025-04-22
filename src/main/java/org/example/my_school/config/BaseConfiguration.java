package org.example.my_school.config;

import lombok.RequiredArgsConstructor;
import org.example.my_school.Service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class BaseConfiguration {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userService);
        return provider;
    }

    // --- Bean جدید برای در دسترس قرار دادن AuthenticationManager ---
    /**
     * این Bean، AuthenticationManager پیکربندی شده توسط Spring Security را
     * در دسترس قرار می‌دهد تا بتوانیم از آن برای احراز هویت برنامه‌نویسانه
     * (مثلاً در کنترلر لاگین JavaFX) استفاده کنیم.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * پیکربندی فیلترهای امنیتی Spring Security.
     * چون برنامه ما وب نیست، تنظیمات HTTP را به حداقل می‌رسانیم.
     * CSRF را غیرفعال می‌کنیم چون کلاینت ما مرورگر نیست.
     * مهمترین کار این Bean، اطمینان از ساخته شدن صحیح AuthenticationManager
     * و فعال بودن مکانیزم‌های امنیتی Spring است.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF برای کلاینت‌های غیر مرورگری لازم نیست و می‌تواند مشکل‌ساز باشد
                .csrf(AbstractHttpConfigurer::disable)
                // در حال حاضر هیچ قانون خاصی برای دسترسی به مسیرهای HTTP نیاز نداریم
                // در صورت نیاز می‌توان قوانین را اینجا اضافه کرد (مثلا برای بخش وب آینده)
                .authorizeHttpRequests(auth -> auth
                                .anyRequest().permitAll() // فعلا به همه مسیرها اجازه دسترسی می‌دهیم (چون مسیر وبی نداریم)
                        // یا .anyRequest().authenticated() اگر می‌خواهید پیش‌فرض امن‌تری داشته باشید
                );
        // تنظیمات دیگری مثل formLogin, httpBasic و ... را فعلا نیاز نداریم

        return http.build();
    }

}//class
