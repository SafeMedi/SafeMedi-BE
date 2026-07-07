package com.safemedi.app.sefemedi.domain.medication.service

import com.safemedi.app.sefemedi.domain.family.repository.FamilyRepository
import com.safemedi.app.sefemedi.domain.medication.dto.DailyMedicationRecordItemResponse
import com.safemedi.app.sefemedi.domain.medication.dto.MedicationRecordQueryResponse
import com.safemedi.app.sefemedi.domain.medication.dto.MedicationRecordQueryType
import com.safemedi.app.sefemedi.domain.medication.dto.MedicationRecordSummaryResponse
import com.safemedi.app.sefemedi.domain.medication.dto.PeriodMedicationRecordGroupResponse
import com.safemedi.app.sefemedi.domain.medication.dto.PeriodMedicationRecordItemResponse
import com.safemedi.app.sefemedi.domain.medication.entity.MedicationRecord
import com.safemedi.app.sefemedi.domain.medication.entity.MedicationStatus
import com.safemedi.app.sefemedi.domain.medication.repository.MedicationRecordRepository
import com.safemedi.app.sefemedi.domain.user.entity.User
import com.safemedi.app.sefemedi.domain.user.repository.UserRepository
import com.safemedi.app.sefemedi.global.error.BusinessException
import com.safemedi.app.sefemedi.global.error.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAdjusters

@Service
class MedicationRecordQueryService(
    private val userRepository: UserRepository,
    private val familyRepository: FamilyRepository,
    private val medicationRecordRepository: MedicationRecordRepository,
) {
    @Transactional(readOnly = true)
    fun findRecords(
        socialId: String,
        type: String?,
        date: String?,
        familyId: Long?,
    ): MedicationRecordQueryResponse {
        val user = userRepository.findBySocialId(socialId)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
        val userId = requireUserId(user)
        val queryType = parseType(type)
        val parsedDate = parseDate(date)
        val period = resolvePeriod(
            type = queryType,
            date = parsedDate,
        )
        val target = resolveTargetUser(
            userId = userId,
            familyId = familyId,
        )
        val records = medicationRecordRepository.findRecordsForPeriod(
            userId = target.userId,
            startAt = period.startDate.atStartOfDay(),
            endAt = period.endDate.plusDays(1).atStartOfDay(),
        )

        return when (queryType) {
            MedicationRecordQueryType.DAILY -> createDailyResponse(
                type = queryType,
                date = parsedDate,
                familyId = familyId,
                relation = target.relation,
                records = records,
            )
            MedicationRecordQueryType.WEEK,
            MedicationRecordQueryType.MONTH -> createPeriodResponse(
                type = queryType,
                period = period,
                familyId = familyId,
                relation = target.relation,
                records = records,
            )
        }
    }

    private fun createDailyResponse(
        type: MedicationRecordQueryType,
        date: LocalDate,
        familyId: Long?,
        relation: String?,
        records: List<MedicationRecord>,
    ): MedicationRecordQueryResponse {
        return MedicationRecordQueryResponse(
            type = type,
            date = date,
            familyId = familyId,
            relation = relation,
            summary = records.toSummary(),
            records = records.map { it.toDailyItem() },
        )
    }

    private fun createPeriodResponse(
        type: MedicationRecordQueryType,
        period: QueryPeriod,
        familyId: Long?,
        relation: String?,
        records: List<MedicationRecord>,
    ): MedicationRecordQueryResponse {
        return MedicationRecordQueryResponse(
            type = type,
            periodStartDate = period.startDate,
            periodEndDate = period.endDate,
            familyId = familyId,
            relation = relation,
            summary = records.toSummary(),
            dailyRecords = records.toDailyGroups(),
        )
    }

    private fun resolveTargetUser(
        userId: Long,
        familyId: Long?,
    ): QueryTarget {
        if (familyId == null) {
            return QueryTarget(
                userId = userId,
                relation = null,
            )
        }

        val family = familyRepository.findByIdAndUser_Id(
            id = familyId,
            userId = userId,
        ) ?: throw BusinessException(ErrorCode.FAMILY_ACCESS_DENIED)

        if (!family.isAllowMyInfo) {
            throw BusinessException(ErrorCode.FAMILY_ACCESS_DENIED)
        }

        return QueryTarget(
            userId = family.connectedUser.id ?: throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR),
            relation = family.relation,
        )
    }

    private fun parseType(
        value: String?,
    ): MedicationRecordQueryType {
        val trimmed = value?.trim()
        if (trimmed.isNullOrBlank()) {
            throw BusinessException(ErrorCode.STATISTICS_DATE_REQUIRED)
        }

        return try {
            MedicationRecordQueryType.valueOf(trimmed.uppercase())
        } catch (_: IllegalArgumentException) {
            throw BusinessException(ErrorCode.INVALID_ENUM_VALUE)
        }
    }

    private fun parseDate(
        value: String?,
    ): LocalDate {
        val trimmed = value?.trim()
        if (trimmed.isNullOrBlank()) {
            throw BusinessException(ErrorCode.STATISTICS_DATE_REQUIRED)
        }

        return try {
            LocalDate.parse(trimmed)
        } catch (_: DateTimeParseException) {
            throw BusinessException(ErrorCode.STATISTICS_INVALID_DATE_FORMAT)
        }
    }

    private fun resolvePeriod(
        type: MedicationRecordQueryType,
        date: LocalDate,
    ): QueryPeriod {
        return when (type) {
            MedicationRecordQueryType.DAILY -> QueryPeriod(
                startDate = date,
                endDate = date,
            )
            MedicationRecordQueryType.WEEK -> QueryPeriod(
                startDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                endDate = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)),
            )
            MedicationRecordQueryType.MONTH -> QueryPeriod(
                startDate = date.withDayOfMonth(1),
                endDate = date.withDayOfMonth(date.lengthOfMonth()),
            )
        }
    }

    private fun MedicationRecord.toDailyItem(): DailyMedicationRecordItemResponse {
        return DailyMedicationRecordItemResponse(
            recordId = id ?: throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR),
            prescriptionTitle = prescription.title,
            medicationNames = listOf(prescriptionDrugTime.prescriptionDrug.drugName),
            scheduledTime = scheduledAt.toLocalTime().format(TIME_FORMATTER),
            takenTime = takenAt?.toLocalTime()?.format(TIME_FORMATTER),
            status = status.name,
        )
    }

    private fun List<MedicationRecord>.toDailyGroups(): List<PeriodMedicationRecordGroupResponse> {
        return groupBy { it.scheduledAt.toLocalDate() }
            .toSortedMap()
            .map { (date, records) ->
                PeriodMedicationRecordGroupResponse(
                    date = date,
                    totalCount = records.size,
                    takenCount = records.countTaken(),
                    fraction = records.toSummary().fraction,
                    items = records.map { it.toPeriodItem() },
                )
            }
    }

    private fun MedicationRecord.toPeriodItem(): PeriodMedicationRecordItemResponse {
        return PeriodMedicationRecordItemResponse(
            recordId = id ?: throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR),
            prescriptionTitle = prescription.title,
            scheduledTime = scheduledAt.toLocalTime().format(TIME_FORMATTER),
            status = status.name,
        )
    }

    private fun List<MedicationRecord>.toSummary(): MedicationRecordSummaryResponse {
        val totalCount = size
        val takenCount = countTaken()
        return MedicationRecordSummaryResponse(
            totalCount = totalCount,
            takenCount = takenCount,
            fraction = "$takenCount/$totalCount",
        )
    }

    private fun List<MedicationRecord>.countTaken(): Int {
        return count { it.status == MedicationStatus.SUCCESS }
    }

    private fun requireUserId(
        user: User,
    ): Long {
        return user.id ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
    }

    private data class QueryTarget(
        val userId: Long,
        val relation: String?,
    )

    private data class QueryPeriod(
        val startDate: LocalDate,
        val endDate: LocalDate,
    )

    private companion object {
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
