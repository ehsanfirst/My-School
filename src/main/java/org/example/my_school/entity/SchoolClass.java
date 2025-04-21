package org.example.my_school.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero; // برای capacity
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity نماینده یک کلاس درسی در مدرسه.
 * شامل اطلاعات کلاس، معلم مربوطه و دانش‌آموزان ثبت‌نام شده.
 */
@Entity
@Table(name = "school_classes", indexes = { // ایندکس برای جستجوی کلاس بر اساس نام یا معلم
        @Index(name = "idx_schoolclass_name", columnList = "name"),
        @Index(name = "idx_schoolclass_teacher_id", columnList = "teacher_id")
})
@Getter
@Setter
@NoArgsConstructor // سازنده پیش‌فرض مورد نیاز JPA
// روابط از equals/hashCode/toString حذف شده‌اند
@ToString(exclude = {"teacher", "students", "dailyRecords"})
@EqualsAndHashCode(of = "id") // برابری فقط بر اساس ID (ساده‌ترین راه برای Entity)
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * نام کلاس (مثلاً 'ریاضی دهم تجربی - گروه الف').
     * نمی‌تواند خالی باشد.
     */
    @NotBlank(message = "نام کلاس نمی‌تواند خالی باشد")
    @Column(nullable = false, length = 100) // طول مناسب برای نام کلاس
    private String name;

    /**
     * ظرفیت کلاس (حداکثر تعداد دانش‌آموز).
     * می‌تواند خالی باشد یا مقدار صفر و مثبت داشته باشد.
     */
    @PositiveOrZero(message = "ظرفیت کلاس نمی‌تواند منفی باشد")
    @Column
    private Integer capacity; // استفاده از Integer تا بتواند null باشد

    // --- روابط ---

    /**
     * معلمی که مسئول این کلاس است (رابطه چند-به-یک).
     * فرض می‌کنیم هر کلاس باید یک معلم داشته باشد (nullable = false).
     * FetchType.LAZY برای بهینه‌سازی.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", referencedColumnName = "id", nullable = false) // FK به جدول teachers (که user_id PK/FK آن است)
    @NotNull // برای ولیدیشن در سطح برنامه (اگرچه nullable=false هم هست)
    private Teacher teacher;

    /**
     * دانش‌آموزانی که در این کلاس ثبت‌نام کرده‌اند (رابطه چند-به-چند).
     * mappedBy به فیلد "enrolledClasses" در Entity Student اشاره می‌کند که
     * پیکربندی جدول واسط (@JoinTable) در آنجا انجام شده است.
     * CascadeType: PERSIST و MERGE مناسب هستند.
     */
    @ManyToMany(mappedBy = "enrolledClasses",
            fetch = FetchType.LAZY,
            cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private Set<Student> students = new HashSet<>();

    /*
     * رکوردهای روزانه‌ای که مربوط به این کلاس هستند (رابطه یک-به-چند).
     * mappedBy باید به نام فیلد schoolClass در Entity DailyRecord اشاره کند.
     * اگر کلاس حذف شود، رکوردهای روزانه مرتبط با آن هم حذف می‌شوند (CascadeType.ALL).
     * اگر رکوردی از این مجموعه حذف شود، از دیتابیس هم حذف می‌شود (orphanRemoval=true).
     * (Entity DailyRecord باید بعدا تعریف شود)
     */

     @OneToMany(mappedBy = "schoolClass", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
     private Set<DailyRecord> dailyRecords = new HashSet<>();


    // --- سازنده‌ها ---

    /**
     * سازنده سفارشی برای ایجاد آسان یک SchoolClass جدید.
     *
     * @param name نام کلاس (اجباری)
     * @param teacher معلم کلاس (اجباری)
     * @param capacity ظرفیت کلاس (اختیاری)
     */
    public SchoolClass(String name, Teacher teacher, Integer capacity) {
        // ولیدیشن ورودی‌های اجباری
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("نام کلاس نمی‌تواند خالی باشد.");
        }
        if (teacher == null) {
            throw new IllegalArgumentException("معلم کلاس نمی‌تواند خالی باشد.");
        }
        this.name = name;
        this.teacher = teacher;
        this.capacity = capacity;
    }


    // --- متدهای کمکی برای مدیریت روابط (اختیاری ولی مفید) ---

    public void addDailyRecord(DailyRecord dailyRecord) {
        dailyRecords.add(dailyRecord);
        dailyRecord.setSchoolClass(this);
    }

    public void removeDailyRecord(DailyRecord dailyRecord) {
        dailyRecords.remove(dailyRecord);
        dailyRecord.setSchoolClass(null);
    }

    /**
     * اضافه کردن یک دانش‌آموز به کلاس (و مدیریت دوطرفه رابطه).
     * @param student دانش‌آموزی که به کلاس اضافه می‌شود.
     */
    public void addStudent(Student student) {
        if (student != null) {
            this.students.add(student);
            // اطمینان از اینکه مجموعه enrolledClasses در student نال نباشد
            if (student.getEnrolledClasses() != null) {
                student.getEnrolledClasses().add(this); // اضافه کردن کلاس به لیست دانش آموز
            }
        }
    }

    /**
     * حذف یک دانش‌آموز از کلاس (و مدیریت دوطرفه رابطه).
     * @param student دانش‌آموزی که از کلاس حذف می‌شود.
     */
    public void removeStudent(Student student) {
        if (student != null) {
            this.students.remove(student);
            if (student.getEnrolledClasses() != null) {
                student.getEnrolledClasses().remove(this); // حذف کلاس از لیست دانش آموز
            }
        }
    }

    // --- متدهای دیگر ---
    // Equals و HashCode توسط Lombok (of="id") مدیریت می‌شود.
}
