package org.example.my_school.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*; // برای ولیدیشن
import lombok.*;
import org.hibernate.annotations.CreationTimestamp; // اگرچه در User هست، برای نمایش اضافه کردم
import org.hibernate.annotations.UpdateTimestamp;   // اگرچه در User هست، برای نمایش اضافه کردم

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entity نماینده نقش دانش‌آموز (Student) در سیستم.
 * این کلاس از User ارث‌بری می‌کند و از استراتژی وراثت JOINED استفاده می‌کند.
 * جدول مربوطه در دیتابیس `students` نام دارد.
 */
@Entity
@Table(name = "students", indexes = { // ایندکس برای فیلدهای پرتکرار یا منحصر به فرد
        @Index(name = "idx_student_student_code", columnList = "student_code", unique = true)
})
@DiscriminatorValue("STUDENT") // مقدار ستون user_type برای این نوع کاربر
@Getter
@Setter
@NoArgsConstructor // سازنده پیش‌فرض مورد نیاز JPA
// روابط از equals/hashCode/toString حذف شده‌اند
@ToString(callSuper = true, exclude = {"parentRelationships", "enrollments", "dailyRecords"})
@EqualsAndHashCode(callSuper = true, exclude = {"parentRelationships", "enrollments", "dailyRecords"})
public class Student extends User {

    // --- فیلدهای اختصاصی Student ---

    /**
     * شماره دانش‌آموزی یا کد شناسایی منحصر به فرد دانش‌آموز در مدرسه.
     */
    @Column(name = "student_code", unique = true, length = 50) // یکتایی توصیه می‌شود
    @Size(max = 50)
    private String studentCode;

    /**
     * پایه تحصیلی دانش‌آموز (مثلاً 'دهم تجربی', 'پنجم ب').
     * نمی‌تواند خالی باشد.
     */
    @NotBlank(message = "پایه تحصیلی نمی‌تواند خالی باشد")
    @Column(name = "grade_level", nullable = false, length = 50)
    @Size(max = 50)
    private String gradeLevel;

    /**
     * آخرین معدل محاسبه شده برای دانش‌آموز.
     * نباید خالی باشد و باید بین ۰ تا ۲۰ باشد.
     */
    @NotNull(message = "آخرین معدل نمی‌تواند خالی باشد")
    @Min(value = 0, message = "معدل نمی‌تواند کمتر از ۰ باشد")
    @Max(value = 20, message = "معدل نمی‌تواند بیشتر از ۲۰ باشد")
    @Column(name = "last_average", nullable = false, precision = 4, scale = 2) // precision و scale برای دقت اعشار
    private BigDecimal lastAverage;

    /**
     * تاریخ تولد دانش‌آموز.
     */
    @Past(message = "تاریخ تولد باید در گذشته باشد") // ولیدیشن تاریخ
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /**
     * توضیحات اضافی در مورد شرایط خاص دانش‌آموز (عاطفی، جسمانی و...).
     * این فیلد می‌تواند خالی باشد و محتوای آن خصوصی است.
     * از نوع TEXT در دیتابیس استفاده می‌شود اگر طول زیادی لازم باشد.
     */
    @Lob // برای نگاشت به نوع داده بزرگ متنی (مثل TEXT در PostgreSQL)
    @Column(name = "description")
    private String description; // می‌تواند طولانی باشد


    // --- روابط ---

    /**
     * رابطه یک-به-چند با Entity واسط ParentStudentRelationship.
     * نشان‌دهنده والدینی است که به این دانش‌آموز مرتبط هستند.
     * mappedBy به نام فیلد Student در Entity ParentStudentRelationship اشاره دارد.
     */
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ParentStudentRelationship> parentRelationships = new HashSet<>();

    /**
     * کلاس‌هایی که این دانش‌آموز در آنها ثبت‌نام کرده است (رابطه چند-به-چند).
     * JPA به صورت خودکار جدول واسط (enrollments یا student_classes) را مدیریت می‌کند.
     * FetchType.LAZY برای بهینه‌سازی ضروری است.
     * CascadeType: معمولاً در روابط ManyToMany، Cascade کمتری استفاده می‌شود (مثلا فقط PERSIST و MERGE).
     *             حذف یک دانش‌آموز معمولاً نباید باعث حذف کلاس شود و برعکس.
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable( // تعریف صریح جدول واسط و ستون‌هایش (بهترین روش)
            name = "student_classes", // نام جدول واسط در دیتابیس (مطابق دیاگرام شما)
            joinColumns = @JoinColumn(name = "student_user_id", referencedColumnName = "id"), // ستون FK برای این Entity (Student)
            inverseJoinColumns = @JoinColumn(name = "class_id", referencedColumnName = "id") // ستون FK برای طرف مقابل (SchoolClass)
    )
    private Set<SchoolClass> enrolledClasses = new HashSet<>(); // نام فیلد را enrolledClasses گذاشتم که واضح‌تر باشد

    /*
     * رکوردهای روزانه‌ای که برای این دانش‌آموز ثبت شده است (رابطه یک-به-چند).
     * mappedBy باید به نام فیلد Student در Entity DailyRecord اشاره کند.
     * (Entity DailyRecord باید بعدا تعریف شود)
     */

     @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
     private Set<DailyRecord> dailyRecords = new HashSet<>();



    // --- سازنده‌ها ---

    /**
     * سازنده سفارشی برای ایجاد آسان یک Student جدید.
     *
     * @param username نام کاربری
     * @param password رمز عبور (هش شده)
     * @param firstName نام
     * @param lastName نام خانوادگی
     * @param email ایمیل
     * @param phoneNumber شماره تلفن
     * @param avatarUrl آدرس آواتار
     * @param enabled وضعیت فعال بودن
     * @param studentCode شماره دانش‌آموزی
     * @param gradeLevel پایه تحصیلی (اجباری)
     * @param lastAverage آخرین معدل (اجباری)
     * @param dateOfBirth تاریخ تولد (اختیاری)
     * @param description توضیحات خاص (اختیاری)
     */
    public Student(String username, String password, String firstName, String lastName,
                   String email, String phoneNumber, String avatarUrl, boolean enabled,
                   String studentCode, String gradeLevel, BigDecimal lastAverage,
                   LocalDate dateOfBirth, String description) {
        super(username, password, firstName, lastName, email, phoneNumber, avatarUrl, enabled);
        this.studentCode = studentCode;

        // ولیدیشن مقادیر اجباری
        if (gradeLevel == null || gradeLevel.isBlank()) {
            throw new IllegalArgumentException("پایه تحصیلی نمی‌تواند خالی باشد.");
        }
        this.gradeLevel = gradeLevel;

        if (lastAverage == null) {
            throw new IllegalArgumentException("آخرین معدل نمی‌تواند خالی باشد.");
        }
        if (lastAverage.compareTo(BigDecimal.ZERO) < 0 || lastAverage.compareTo(new BigDecimal("20")) > 0) {
            throw new IllegalArgumentException("معدل باید بین ۰ و ۲۰ باشد.");
        }

        this.lastAverage = lastAverage;

        this.dateOfBirth = dateOfBirth;
        this.description = description;
    }


    // --- متدهای کمکی برای مدیریت روابط (اختیاری ولی مفید) ---

    public void addParentRelationship(Parent parent, String relationshipType) {
        ParentStudentRelationship relationship = new ParentStudentRelationship(parent, this, relationshipType);
        this.parentRelationships.add(relationship);
        // parent.getParentStudentRelationships().add(relationship); // اگر سمت دیگر هم مدیریت می‌شود
    }

    public void removeParentRelationship(ParentStudentRelationship relationship) {
        this.parentRelationships.remove(relationship);
        // relationship.setStudent(null); // orphanRemoval این کار را انجام می‌دهد
        // relationship.setParent(null);
    }


    public void addDailyRecord(DailyRecord dailyRecord) {
        dailyRecords.add(dailyRecord);
        dailyRecord.setStudent(this);
    }

    public void removeDailyRecord(DailyRecord dailyRecord) {
        dailyRecords.remove(dailyRecord);
        dailyRecord.setStudent(null);
    }

    /**
     * اضافه کردن دانش‌آموز به یک کلاس (و مدیریت دوطرفه رابطه).
     * @param schoolClass کلاسی که دانش‌آموز به آن اضافه می‌شود.
     */
    public void addClass(SchoolClass schoolClass) {
        if (schoolClass != null) {
            this.enrolledClasses.add(schoolClass);
            // اطمینان از اینکه مجموعه students در schoolClass نال نباشد
            if (schoolClass.getStudents() != null) {
                schoolClass.getStudents().add(this); // اضافه کردن دانش آموز به لیست کلاس
            }
        }
    }

    /**
     * حذف دانش‌آموز از یک کلاس (و مدیریت دوطرفه رابطه).
     * @param schoolClass کلاسی که دانش‌آموز از آن حذف می‌شود.
     */
    public void removeClass(SchoolClass schoolClass) {
        if (schoolClass != null) {
            this.enrolledClasses.remove(schoolClass);
            if (schoolClass.getStudents() != null) {
                schoolClass.getStudents().remove(this); // حذف دانش آموز از لیست کلاس
            }
        }
    }


    // --- متدهای دیگر ---
    // equals, hashCode و متدهای UserDetails از کلاس User به ارث می‌رسند.
}
