package com.safemedi.app.sefemedi.domain.medication.repository

import com.safemedi.app.sefemedi.domain.medication.entity.MedicationRecord
import com.safemedi.app.sefemedi.domain.medication.entity.Prescription
import com.safemedi.app.sefemedi.domain.medication.entity.PrescriptionDrug
import com.safemedi.app.sefemedi.domain.medication.entity.PrescriptionDrugTime
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface PrescriptionRepository : JpaRepository<Prescription, Long> {
    fun findByUserIdOrderByCreatedAtDescIdDesc(
        userId: Long,
        pageable: Pageable,
    ): Slice<Prescription>

    fun findByIdAndUserId(
        id: Long,
        userId: Long,
    ): Prescription?
}

interface PrescriptionDrugRepository : JpaRepository<PrescriptionDrug, Long> {
    @Query(
        """
        select pd.prescription.id as prescriptionId, count(pd.id) as drugCount
        from PrescriptionDrug pd
        where pd.prescription.id in :prescriptionIds
        group by pd.prescription.id
        """
    )
    fun countByPrescriptionIds(
        @Param("prescriptionIds") prescriptionIds: Collection<Long>,
    ): List<PrescriptionDrugCountProjection>

    @Query(
        """
        select pd
        from PrescriptionDrug pd
        left join fetch pd.drug
        where pd.prescription.id = :prescriptionId
        order by pd.id asc
        """
    )
    fun findDetailsByPrescriptionId(
        @Param("prescriptionId") prescriptionId: Long,
    ): List<PrescriptionDrug>
}

interface PrescriptionDrugCountProjection {
    val prescriptionId: Long
    val drugCount: Long
}

interface PrescriptionDrugTimeRepository : JpaRepository<PrescriptionDrugTime, Long> {
    @Query(
        """
        select pdt
        from PrescriptionDrugTime pdt
        where pdt.prescriptionDrug.id in :prescriptionDrugIds
        order by pdt.takeTime asc, pdt.id asc
        """
    )
    fun findByPrescriptionDrugIds(
        @Param("prescriptionDrugIds") prescriptionDrugIds: Collection<Long>,
    ): List<PrescriptionDrugTime>
}

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
