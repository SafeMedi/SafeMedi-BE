package com.safemedi.app.sefemedi.domain.medication.entity

import jakarta.persistence.*
import java.time.LocalTime

@Entity
@Table(name = "prescription_time")
class PrescriptionTime(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id")
    val prescription: Prescription,

    @Column(name = "take_time")
    var takeTime: LocalTime
)
