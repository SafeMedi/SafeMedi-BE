package com.safemedi.app.sefemedi.domain.medication.repository

import com.safemedi.app.sefemedi.domain.medication.entity.MedicationRecord
import com.safemedi.app.sefemedi.domain.medication.entity.Prescription
import com.safemedi.app.sefemedi.domain.medication.entity.PrescriptionDrug
import com.safemedi.app.sefemedi.domain.medication.entity.PrescriptionDrugTime
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface PrescriptionRepository : JpaRepository<Prescription, Long>

interface PrescriptionDrugRepository : JpaRepository<PrescriptionDrug, Long>

interface PrescriptionDrugTimeRepository : JpaRepository<PrescriptionDrugTime, Long>

interface MedicationRecordRepository : JpaRepository<MedicationRecord, Long> {
    @Query(
        """
        select mr
        from MedicationRecord mr
        join fetch mr.prescription p
        join fetch mr.prescriptionDrugTime pdt
        join fetch pdt.prescriptionDrug pd
        where mr.user.id = :userId
          and mr.scheduledAt >= :startAt
          and mr.scheduledAt < :endAt
        order by mr.scheduledAt asc, p.id asc, pd.id asc
        """
    )
    fun findTodaySchedules(
        @Param("userId") userId: Long,
        @Param("startAt") startAt: LocalDateTime,
        @Param("endAt") endAt: LocalDateTime,
    ): List<MedicationRecord>
}
