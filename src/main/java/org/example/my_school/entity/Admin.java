package org.example.my_school.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity نماینده نقش مدیر (Admin) در سیستم.
 * این کلاس از User ارث‌بری می‌کند و از استراتژی وراثت JOINED استفاده می‌کند.
 * جدول مربوطه در دیتابیس `admins` نام دارد.
 */
@Entity
@Table(name = "admins") // نام جدول اختصاصی ادمین‌ها در دیتابیس
@DiscriminatorValue("ADMIN") // مقدار ستون user_type برای این نوع کاربر
@Getter
@Setter
@NoArgsConstructor // سازنده پیش‌فرض مورد نیاز JPA
@ToString(callSuper = true) // فراخوانی متد toString کلاس والد (User) برای نمایش بهتر
// @EqualsAndHashCode(callSuper = true) // equals و hashCode از کلاس User به ارث می‌رسد و کافیست
public class Admin extends User {

    // --- فیلدهای اختصاصی Admin ---
    // طبق طراحی فعلی Admin فیلد اختصاصی خاصی ندارد.
    // تمام اطلاعات لازم در جدول `users` و نقش از طریق `user_type` مشخص می‌شود.
    // اگر در آینده نیاز به فیلد خاصی برای ادمین بود (مثلاً سطح دسترسی)،
    // می‌توانید آن را اینجا اضافه کنید:
    //
    // @Column(name = "access_level", length = 50)
    // private String accessLevel;


    // --- سازنده‌ها ---
    /**
     * سازنده سفارشی برای ایجاد آسان یک Admin جدید.
     * این سازنده، سازنده کلاس والد (User) را برای مقداردهی فیلدهای مشترک فراخوانی می‌کند.
     *
     * @param username نام کاربری
     * @param password رمز عبور (هش شده)
     * @param firstName نام
     * @param lastName نام خانوادگی
     * @param email ایمیل (اختیاری)
     * @param phoneNumber شماره تلفن (اختیاری)
     * @param avatarUrl آدرس آواتار (اختیاری)
     * @param enabled وضعیت فعال بودن حساب
     */
    public Admin(String username, String password, String firstName, String lastName,
                 String email, String phoneNumber, String avatarUrl, boolean enabled) {
        // فراخوانی سازنده مناسب کلاس والد (User)
        super(username, password, firstName, lastName, email, phoneNumber, avatarUrl, enabled);
        // در اینجا می‌توانید مقادیر پیش‌فرض برای فیلدهای اختصاصی Admin را ست کنید (اگر وجود داشت)
        // مثال: this.accessLevel = "SUPER_ADMIN";
    }

    // --- متدهای دیگر ---
    // متدهای equals و hashCode از کلاس User به ارث برده می‌شوند و کافی هستند،
    // زیرا برابری Entity ها معمولاً بر اساس ID تعریف می‌شود که در کلاس User پیاده‌سازی شده است.
    // همچنین متدهای UserDetails نیز از کلاس User به ارث می‌رسند.
}
