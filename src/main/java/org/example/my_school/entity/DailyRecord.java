package org.example.my_school.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*; // برای ولیدیشن
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity نماینده رکورد روزانه وضعیت یک دانش‌آموز در یک کلاس خاص.
 * شامل اطلاعات حضور/غیاب، نمره احتمالی و توضیحات معلم.
 */
@Entity
@Table(name = "daily_records", indexes = { // ایندکس برای جستجوی سریع رکوردها
        @Index(name = "idx_dailyrecord_student_date", columnList = "student_user_id, record_date"),
        @Index(name = "idx_dailyrecord_class_date", columnList = "class_id, record_date"),
        @Index(name = "idx_dailyrecord_teacher_date", columnList = "teacher_user_id, record_date")
})
@Getter
@Setter
@NoArgsConstructor // سازنده پیش‌فرض مورد نیاز JPA
// روابط از equals/hashCode/toString حذف شده‌اند چون می‌توانند باعث Lazy Loading شوند
@ToString(exclude = {"student", "schoolClass", "teacher"})
@EqualsAndHashCode(of = "id") // برابری فقط بر اساس ID
public class DailyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * تاریخ ثبت این رکورد.
     * نمی‌تواند خالی باشد و باید تاریخ فعلی یا گذشته باشد.
     */
    @NotNull(message = "تاریخ رکورد نمی‌تواند خالی باشد")
    @PastOrPresent(message = "تاریخ رکورد نمی‌تواند در آینده باشد")
    @Column(name = "record_date", nullable = false) // نام ستون می‌تواند record_date یا date باشد
    private LocalDate recordDate; // استفاده از LocalDate برای تاریخ بدون زمان

    /**
     * وضعیت حضور دانش‌آموز در این تاریخ و کلاس.
     * true = حاضر, false = غایب.
     * نمی‌تواند خالی باشد.
     */
    @NotNull(message = "وضعیت حضور نمی‌تواند خالی باشد")
    @Column(name = "is_present", nullable = false) // نام ستون در دیاگرام presented بود، is_present رایج‌تر است
    private Boolean isPresent; // یا presented

    /**
     * نمره کلاسی یا فعالیت روزانه (اختیاری).
     * باید بین ۰ تا ۲۰ باشد.
     */
    @Min(value = 0, message = "نمره نمی‌تواند کمتر از ۰ باشد")
    @Max(value = 20, message = "نمره نمی‌تواند بیشتر از ۲۰ باشد")
    @Column(precision = 4, scale = 2) // دقت اعشار برای نمره
    private BigDecimal grade; // می‌تواند null باشد

    /**
     * توضیحات یا یادداشت معلم در مورد وضعیت دانش‌آموز در این روز/کلاس.
     * می‌تواند خالی باشد.
     */
    @Lob // برای متن‌های طولانی
    @Column
    private String description; // نام ستون در دیاگرام هم description بود


    // --- روابط ManyToOne ---

    /**
     * دانش‌آموزی که این رکورد برای او ثبت شده است.
     * هر رکورد باید به یک دانش‌آموز تعلق داشته باشد.
     */
    @NotNull // ولیدیشن در سطح برنامه
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_user_id", referencedColumnName = "id", nullable = false) // FK به students.user_id
    private Student student;

    /**
     * کلاسی که این رکورد در آن ثبت شده است.
     * هر رکورد باید به یک کلاس تعلق داشته باشد.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", referencedColumnName = "id", nullable = false) // FK به school_classes.id
    private SchoolClass schoolClass;

    /**
     * معلمی که این رکورد را ثبت کرده است.
     * هر رکورد باید توسط یک معلم ثبت شده باشد.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_user_id", referencedColumnName = "id", nullable = false) // FK به teachers.user_id
    private Teacher teacher;


    // --- سازنده‌ها ---

    /**
     * سازنده سفارشی برای ایجاد آسان یک DailyRecord جدید.
     *
     * @param recordDate تاریخ رکورد (اجباری)
     * @param isPresent وضعیت حضور (اجباری)
     * @param student دانش‌آموز (اجباری)
     * @param schoolClass کلاس (اجباری)
     * @param teacher معلم (اجباری)
     * @param grade نمره (اختیاری)
     * @param description توضیحات (اختیاری)
     */
    public DailyRecord(LocalDate recordDate, Boolean isPresent, Student student,
                       SchoolClass schoolClass, Teacher teacher, BigDecimal grade, String description) {
        // ولیدیشن پارامترهای اجباری
        if (recordDate == null || isPresent == null || student == null || schoolClass == null || teacher == null) {
            throw new IllegalArgumentException("تاریخ، وضعیت حضور، دانش‌آموز، کلاس و معلم نمی‌توانند خالی باشند.");
        }
        if (grade != null && (grade.compareTo(BigDecimal.ZERO) < 0 || grade.compareTo(new BigDecimal("20")) > 0)) {
            throw new IllegalArgumentException("نمره باید بین ۰ و ۲۰ باشد.");
        }

        this.recordDate = recordDate;
        this.isPresent = isPresent;
        this.student = student;
        this.schoolClass = schoolClass;
        this.teacher = teacher;
        this.grade = grade;
        this.description = description;
    }

    // --- متدهای دیگر ---
    // Equals و HashCode توسط Lombok (of="id") مدیریت می‌شود.
}
