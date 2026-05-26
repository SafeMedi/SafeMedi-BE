package com.safemedi.app.sefemedi.domain.medication.analyzer

interface PrescriptionAnalyzer {
    fun setNext(next: PrescriptionAnalyzer): PrescriptionAnalyzer
    fun analyze(context: PrescriptionContext): PrescriptionContext
}

abstract class AbstractPrescriptionAnalyzer : PrescriptionAnalyzer {
    private var nextAnalyzer: PrescriptionAnalyzer? = null

    override fun setNext(next: PrescriptionAnalyzer): PrescriptionAnalyzer {
        nextAnalyzer = next
        return next
    }

    override fun analyze(context: PrescriptionContext): PrescriptionContext {
        doAnalyze(context)
        return nextAnalyzer?.analyze(context) ?: context
    }

    protected abstract fun doAnalyze(context: PrescriptionContext)
}
