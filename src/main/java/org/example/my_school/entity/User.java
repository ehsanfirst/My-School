package org.example.my_school.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails; // اینترفیس UserDetails

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/**
 * کلاس پایه Entity برای تمام کاربران سیستم (مدیر، معلم، والد، دانش‌آموز).
 * از استراتژی وراثت JOINED و قفل‌گذاری خوش‌بینانه استفاده می‌کند.
 * همچنین UserDetails را برای ادغام آسان با Spring Security پیاده‌سازی می‌کند.
 */
@Entity
@Table(name = "users", indexes = { // اضافه کردن ایندکس برای فیلدهای پرتکرار در کوئری
        @Index(name = "idx_user_username", columnList = "username", unique = true),
        @Index(name = "idx_user_email", columnList = "email", unique = true),
        @Index(name = "idx_user_phonenumber", columnList = "phoneNumber", unique = true)
})
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "user_type", // نام ستون تمایز دهنده
        discriminatorType = DiscriminatorType.STRING) // نوع داده: رشته
@Getter
@Setter
// از سازنده‌های سفارشی استفاده می‌کنیم، پس NoArgs و AllArgs رو حذف یا با دقت استفاده می‌کنیم
// @NoArgsConstructor // سازنده بدون آرگومان برای JPA لازم است
// @AllArgsConstructor
@ToString(exclude = {"password"}) // عدم نمایش رمز عبور در toString
// @EqualsAndHashCode(of = "id") // پیاده‌سازی دقیق‌تر equals و hashCode در پایین
public abstract class User implements UserDetails {

    /**
     * نقش‌های ممکن برای کاربران سیستم.
     * این مقادیر در ستون user_type ذخیره می‌شوند.
     */
    public enum Role {
        ADMIN, TEACHER, STUDENT, PARENT // USER رو حذف کردم چون هر کاربر یک نقش مشخص داره
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "نام کاربری نمی‌تواند خالی باشد")
    @Size(min = 3, max = 50, message = "نام کاربری باید بین ۳ تا ۵۰ کاراکتر باشد")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "رمز عبور نمی‌تواند خالی باشد")
    // @Size(min = 8, message = "رمز عبور باید حداقل ۸ کاراکتر باشد") // ولیدیشن طول رمز بهتره در DTO/Service باشه
    @Column(nullable = false, length = 255) // طول بیشتر برای هش
    private String password;

    /**
     * این فیلد نقش کاربر را نگه می‌دارد و به ستون تمایز دهنده (user_type) نگاشت می‌شود.
     * EnumType.STRING باعث می‌شود نام enum (مثلاً "TEACHER") در دیتابیس ذخیره شود.
     * insertable=false, updatable=false مهمه چون مقدار این ستون توسط مکانیزم
     * Discriminator مدیریت می‌شود، نه مستقیماً از طریق این فیلد.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, insertable = false, updatable = false)
    private Role role;

    @NotBlank(message = "نام نمی‌تواند خالی باشد")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "نام خانوادگی نمی‌تواند خالی باشد")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String lastName;

    @Email(message = "فرمت ایمیل نامعتبر است")
    @Size(max = 255)
    @Column(unique = true, length = 255)
    private String email;

    // ولیدیشن شماره موبایل می‌تونه پیچیده باشه، فعلا فقط یکتایی و طول رو چک می‌کنیم
    // @Pattern(regexp = "^(\\+98|0)?9\\d{9}$", message = "فرمت شماره موبایل نامعتبر است") // نمونه regex ایرانی
    @Size(max = 20)
    @Column(unique = true, length = 20)
    private String phoneNumber;

    @Size(max = 512)
    @Column(length = 512)
    private String avatarUrl;

    @Column(nullable = false)
    private boolean enabled = true; // مقدار پیش‌فرض true

    @Version // برای قفل‌گذاری خوش‌بینانه
    private int version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // --- سازنده‌ها ---
    // سازنده پیش‌فرض برای JPA
    public User() {}

    // سازنده برای فیلدهای اصلی (بدون id, version, timestamps که خودکار تولید/مدیریت می‌شوند)
    // نقش هم توسط Discriminator مدیریت می‌شود
    public User(String username, String password, String firstName, String lastName, String email, String phoneNumber, String avatarUrl, boolean enabled) {
        this.username = username;
        this.password = password; // توجه: اینجا باید رمز عبور هش شده پاس داده شود!
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
        this.enabled = enabled;
    }


    // --- پیاده سازی دقیق‌تر equals و hashCode ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // بررسی می‌کنیم که آبجکت دیگر null نباشد و حتماً از همین کلاس یا زیرکلاس آن باشد
        // استفاده از getClass() به جای instanceof برای اطمینان از مقایسه فقط با کلاس‌های مشابه در سلسله مراتب
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        // فقط زمانی دو Entity برابرند که id آن‌ها non-null و برابر باشد
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        // همیشه از یک مقدار ثابت استفاده می‌کنیم اگر id نال باشد (قبل از persist شدن)
        // یا از hashCode کلاس استفاده می‌کنیم تا تضمین شود hashCode برای آبجکت‌های مختلف
        // (حتی با id یکسان قبل از persist) متفاوت باشد.
        // راه بهتر استفاده از یک ID تولید شده در بیزینس (مثل UUID) است اگر دارید.
        // اما برای سادگی، از کلاس استفاده می‌کنیم.
        return getClass().hashCode();
        // راه جایگزین (اگر مطمئنید همیشه با Entity های persist شده کار می‌کنید):
        // return Objects.hash(id); // این ساده‌تره ولی برای آبجکت‌های جدید (id=null) درست کار نمی‌کنه
    }


    // --- پیاده سازی متدهای UserDetails ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // نقش کاربر رو از فیلد role می‌گیریم
        // پیشوند "ROLE_" برای Spring Security مهم است
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    // متد getPassword() از UserDetails به فیلد password ما نگاشت می‌شود (توسط Lombok @Getter)
    // متد getUsername() از UserDetails به فیلد username ما نگاشت می‌شود (توسط Lombok @Getter)

    @Override
    public boolean isAccountNonExpired() {
        return true; // در این برنامه، حساب‌ها منقضی نمی‌شوند
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // فعلا منطق قفل شدن حساب نداریم
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // فعلا منطق انقضای رمز عبور نداریم
    }

    @Override
    public boolean isEnabled() {
        return this.enabled; // از فیلد enabled خودمان استفاده می‌کنیم
    }
}