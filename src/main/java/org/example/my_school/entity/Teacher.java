package org.example.my_school.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank; // برای specialization
import jakarta.validation.constraints.Size; // برای کنترل طول رشته‌ها
import lombok.*;

import java.util.HashSet; // برای روابط آینده
import java.util.Set;      // برای روابط آینده

/**
 * Entity نماینده نقش معلم (Teacher) در سیستم.
 * این کلاس از User ارث‌بری می‌کند و از استراتژی وراثت JOINED استفاده می‌کند.
 * جدول مربوطه در دیتابیس `teachers` نام دارد.
 */
@Entity
@Table(name = "teachers", indexes = { // ایندکس برای فیلد پرتکرار یا منحصر به فرد
        @Index(name = "idx_teacher_employee_id", columnList = "employee_id", unique = true)
})
@DiscriminatorValue("TEACHER") // مقدار ستون user_type برای این نوع کاربر
@Getter
@Setter
@NoArgsConstructor // سازنده پیش‌فرض مورد نیاز JPA
@ToString(callSuper = true, exclude = {"taughtClasses", "dailyRecords"}) // از toString والد استفاده کن و روابط رو فعلا حذف کن
@EqualsAndHashCode(callSuper = true, exclude = {"taughtClasses", "dailyRecords"}) // equals/hashCode از والد + روابط رو فعلا حذف کن
public class Teacher extends User {

    /**
     * شماره پرسنلی یا کد استخدامی معلم.
     * می‌تواند برای شناسایی منحصر به فرد معلم در سیستم مدرسه استفاده شود.
     */
    @Column(name = "employee_id", unique = true, length = 50) // منحصر به فرد بودن توصیه می‌شود
    @Size(max = 50) // برای ولیدیشن
    private String employeeId;

    /**
     * رشته تحصیلی یا تخصص اصلی معلم.
     * این فیلد نمی‌تواند خالی باشد (هم در دیتابیس هم در ولیدیشن).
     */
    @NotBlank(message = "تخصص نمی‌تواند خالی باشد") // ولیدیشن در سطح برنامه
    @Column(name = "specialization", nullable = false, length = 255) // اطمینان در سطح دیتابیس
    @Size(max = 255)
    private String specialization;

    // --- روابط (فعلا کامنت شده تا Entity های دیگر تعریف شوند) ---

    /**
     * کلاس‌هایی که این معلم تدریس می‌کند (رابطه یک-به-چند).
     * mappedBy باید به نام فیلد Teacher در Entity SchoolClass اشاره کند.
     * FetchType.LAZY برای بهینه‌سازی ضروری است.
     */

     @OneToMany(mappedBy = "teacher", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
     private Set<SchoolClass> taughtClasses = new HashSet<>();


    /**
     * رکوردهای روزانه‌ای که این معلم ثبت کرده است (رابطه یک-به-چند).
     * mappedBy باید به نام فیلد Teacher در Entity DailyRecord اشاره کند.
     */

     @OneToMany(mappedBy = "teacher", cascade =  {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, orphanRemoval = true, fetch = FetchType.LAZY)
     private Set<DailyRecord> dailyRecords = new HashSet<>();




    // --- سازنده‌ها ---

    /**
     * سازنده سفارشی برای ایجاد آسان یک Teacher جدید.
     *
     * @param username نام کاربری
     * @param password رمز عبور (هش شده)
     * @param firstName نام
     * @param lastName نام خانوادگی
     * @param email ایمیل
     * @param phoneNumber شماره تلفن
     * @param avatarUrl آدرس آواتار
     * @param enabled وضعیت فعال بودن
     * @param employeeId شماره پرسنلی
     * @param specialization تخصص (اجباری)
     */
    public Teacher(String username, String password, String firstName, String lastName,
                   String email, String phoneNumber, String avatarUrl, boolean enabled,
                   String employeeId, String specialization) {
        super(username, password, firstName, lastName, email, phoneNumber, avatarUrl, enabled);
        this.employeeId = employeeId;
        // اطمینان از اینکه specialization null نباشد (هرچند ولیدیشن هم داریم)
        if (specialization == null || specialization.isBlank()) {
            throw new IllegalArgumentException("تخصص معلم نمی‌تواند خالی باشد.");
        }
        this.specialization = specialization;
    }


    // --- متدهای دیگر ---
    // equals, hashCode و متدهای UserDetails از کلاس User به ارث می‌رسند.
    // در آینده اگر نیاز به منطق خاصی برای Teacher بود، اینجا اضافه می‌شود.

    // متدهای کمکی برای مدیریت روابط (در صورت فعال شدن روابط)

    public void addSchoolClass(SchoolClass schoolClass) {
        taughtClasses.add(schoolClass);
        schoolClass.setTeacher(this);
    }

    public void removeSchoolClass(SchoolClass schoolClass) {
        taughtClasses.remove(schoolClass);
        schoolClass.setTeacher(null);
    }

    public void addDailyRecord(DailyRecord dailyRecord) {
        dailyRecords.add(dailyRecord);
        dailyRecord.setTeacher(this);
    }

    public void removeDailyRecord(DailyRecord dailyRecord) {
        dailyRecords.remove(dailyRecord);
        dailyRecord.setTeacher(null);
    }

}