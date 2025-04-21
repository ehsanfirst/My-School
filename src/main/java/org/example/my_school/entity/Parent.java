package org.example.my_school.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entity نماینده نقش والد (Parent) در سیستم.
 * این کلاس از User ارث‌بری می‌کند و از استراتژی وراثت JOINED استفاده می‌کند.
 * جدول مربوطه در دیتابیس `parents` نام دارد.
 * رابطه با دانش‌آموزان از طریق Entity واسط ParentStudentRelationship مدیریت می‌شود.
 */
@Entity
@Table(name = "parents") // نام جدول اختصاصی والدین در دیتابیس
@DiscriminatorValue("PARENT") // مقدار ستون user_type برای این نوع کاربر
@Getter
@Setter
@NoArgsConstructor // سازنده پیش‌فرض مورد نیاز JPA
// توجه: parentStudentRelationships از equals/hashCode و toString حذف شده
@ToString(callSuper = true, exclude = {"parentStudentRelationships"})
@EqualsAndHashCode(callSuper = true, exclude = {"parentStudentRelationships"})
public class Parent extends User {

    // --- فیلدهای اختصاصی Parent ---

    /**
     * شغل والد (اختیاری).
     */
    @Column(length = 100)
    @Size(max = 100)
    private String occupation;

    /**
     * آدرس والد (اختیاری).
     * شاید بهتر باشد آدرس به صورت یک Embeddable Object جداگانه مدل‌سازی شود
     * اگر شامل فیلدهای بیشتری (شهر، خیابان، کدپستی) باشد.
     */
    @Column(length = 512)
    @Size(max = 512)
    private String address;

    // نکته: phoneNumber در کلاس User قرار دارد و اینجا تکرار نمی‌شود.


    // --- روابط ---

    /**
     * رابطه یک-به-چند با Entity واسط ParentStudentRelationship.
     * این Entity واسط، رابطه چند-به-چند بین Parent و Student را به همراه نوع رابطه نگهداری می‌کند.
     * mappedBy به نام فیلد Parent در Entity ParentStudentRelationship اشاره دارد.
     * CascadeType.ALL: اگر والد حذف شود، رکوردهای رابطه مرتبط با او هم حذف می‌شوند.
     * orphanRemoval=true: اگر یک رابطه از این Set حذف شود، رکورد آن در دیتابیس هم حذف می‌شود.
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ParentStudentRelationship> parentStudentRelationships = new HashSet<>();


    // --- سازنده‌ها ---

    /**
     * سازنده سفارشی برای ایجاد آسان یک Parent جدید.
     *
     * @param username نام کاربری
     * @param password رمز عبور (هش شده)
     * @param firstName نام
     * @param lastName نام خانوادگی
     * @param email ایمیل
     * @param phoneNumber شماره تلفن
     * @param avatarUrl آدرس آواتار
     * @param enabled وضعیت فعال بودن
     * @param occupation شغل (اختیاری)
     * @param address آدرس (اختیاری)
     */
    public Parent(String username, String password, String firstName, String lastName,
                  String email, String phoneNumber, String avatarUrl, boolean enabled,
                  String occupation, String address) {
        super(username, password, firstName, lastName, email, phoneNumber, avatarUrl, enabled);
        this.occupation = occupation;
        this.address = address;
    }


    // --- متدهای کمکی برای مدیریت رابطه (اختیاری ولی مفید) ---

    /**
     * اضافه کردن یک رابطه جدید بین این والد و یک دانش‌آموز.
     * @param student دانش‌آموز
     * @param relationshipType نوع رابطه (مثلا 'پدر', 'مادر')
     */
    public void addStudentRelationship(Student student, String relationshipType) {
        ParentStudentRelationship relationship = new ParentStudentRelationship(this, student, relationshipType);
        this.parentStudentRelationships.add(relationship);
        // توجه: سمت Student هم باید به‌روز شود اگر رابطه دوطرفه مدیریت می‌شود
        // student.getParentRelationships().add(relationship);
    }

    /**
     * حذف یک رابطه مشخص با یک دانش‌آموز.
     * @param relationship رابطه ای که باید حذف شود.
     */
    public void removeStudentRelationship(ParentStudentRelationship relationship) {
        this.parentStudentRelationships.remove(relationship);
        // relationship.setParent(null); // orphanRemoval این کار را انجام می‌دهد
        // relationship.setStudent(null); // سمت دانش‌آموز هم باید null شود
    }

    // --- متدهای دیگر ---
    // equals, hashCode و متدهای UserDetails از کلاس User به ارث می‌رسند.
}
