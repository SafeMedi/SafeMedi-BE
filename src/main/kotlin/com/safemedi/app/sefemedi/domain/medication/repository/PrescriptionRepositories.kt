package com.safemedi.app.sefemedi.domain.medication.repository

import com.safemedi.app.sefemedi.domain.medication.entity.MedicationRecord
import com.safemedi.app.sefemedi.domain.medication.entity.Prescription
import com.safemedi.app.sefemedi.domain.medication.entity.PrescriptionDrug
import com.safemedi.app.sefemedi.domain.medication.entity.PrescriptionDrugTime
import org.springframework.data.jpa.repository.JpaRepository

interface PrescriptionRepository : JpaRepository<Prescription, Long>

interface PrescriptionDrugRepository : JpaRepository<PrescriptionDrug, Long>

interface PrescriptionDrugTimeRepository : JpaRepository<PrescriptionDrugTime, Long>

interface MedicationRecordRepository : JpaRepository<MedicationRecord, Long>
