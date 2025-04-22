package org.example.my_school.repository;

import org.example.my_school.entity.DailyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyRecordRepository extends JpaRepository<DailyRecord, Long> {
}
