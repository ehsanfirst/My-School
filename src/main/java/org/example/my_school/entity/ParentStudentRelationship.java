package org.example.my_school.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Objects;

/**
 * Entity نماینده جدول واسط `students_parents`.
 * این جدول رابطه چند-به-چند بین Parent و Student را به همراه نوع رابطه ذخیره می‌کند.
 */
@Entity
@Table(name = "students_parents", uniqueConstraints = {
        // اطمینان از اینکه یک والد و یک دانش‌آموز فقط یک بار با هم رابطه داشته باشند
        @UniqueConstraint(columnNames = {"parent_user_id", "student_user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"parent", "student"}) // جلوگیری از حلقه بی‌نهایت در toString
// Equals و hashCode باید بر اساس parent و student تعریف شوند چون هویت رابطه را مشخص می‌کنند
// Lombok ممکن است پیاده‌سازی ساده‌ای ارائه دهد، پیاده‌سازی دستی دقیق‌تر است
// @EqualsAndHashCode(exclude = {"id", "parent", "student"}) // دقت شود!
public class ParentStudentRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // استفاده از کلید اصلی مجزا (Surrogate Key) معمولا ساده‌تر است

    /**
     * والدی که در این رابطه قرار دارد.
     * رابطه چند-به-یک با Parent.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_user_id", nullable = false) // نام ستون FK در جدول students_parents
    private Parent parent;

    /**
     * دانش‌آموزی که در این رابطه قرار دارد.
     * رابطه چند-به-یک با Student.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_user_id", nullable = false) // نام ستون FK در جدول students_parents
    private Student student;

    /**
     * نوع رابطه (مثلاً 'پدر'، 'مادر'، 'قیم قانونی').
     * این فیلد نمی‌تواند خالی باشد.
     */
    @NotBlank(message = "نوع رابطه نمی‌تواند خالی باشد")
    @Column(name = "relationship_type", nullable = false, length = 50)
    private String relationshipType; // شاید بهتر باشد از Enum استفاده شود


    // --- سازنده ---
    public ParentStudentRelationship(Parent parent, Student student, String relationshipType) {
        this.parent = parent;
        this.student = student;
        // ولیدیشن پایه
        if (relationshipType == null || relationshipType.isBlank()) {
            throw new IllegalArgumentException("نوع رابطه نمی‌تواند خالی باشد.");
        }
        this.relationshipType = relationshipType;
    }


    // --- پیاده سازی دقیق equals و hashCode ---
    // دو رکورد رابطه زمانی برابرند که والد و دانش‌آموز یکسانی داشته باشند
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParentStudentRelationship that = (ParentStudentRelationship) o;
        // مقایسه بر اساس موجودیت‌های والد و دانش‌آموز
        // اطمینان حاصل کنید که equals در Parent و Student به درستی (بر اساس ID) پیاده شده باشد
        return Objects.equals(parent, that.parent) &&
                Objects.equals(student, that.student);
    }

    @Override
    public int hashCode() {
        // هش‌کد بر اساس والد و دانش‌آموز
        return Objects.hash(parent, student);
    }
}
