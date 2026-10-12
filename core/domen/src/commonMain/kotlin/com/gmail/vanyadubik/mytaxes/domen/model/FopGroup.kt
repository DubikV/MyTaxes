package com.gmail.vanyadubik.mytaxes.domen.model

/**
 * Single tax groups of an individual entrepreneur (FOP) in Ukraine.
 *
 * Groups 1 and 2 pay a fixed monthly amount that does not depend on the income.
 * Group 3 pays a percentage of the income converted to UAH.
 * The figures below are for 2026 (living minimum 3 328 UAH, minimum wage 8 000 UAH); update them when the law changes.
 */
enum class FopGroup(
    val id: String,
    val singleTaxPercent: Double = 0.0,
    val militaryLevyPercent: Double = 0.0,
    val fixedSingleTax: Double = 0.0,
    val fixedMilitaryLevy: Double = 0.0,
    val annualIncomeLimit: Double,
) {
    GROUP_1(
        id = "group1",
        fixedSingleTax = 332.80, // 10% of the living minimum
        fixedMilitaryLevy = 800.0, // 10% of the minimum wage
        annualIncomeLimit = 1_336_000.0,
    ),
    GROUP_2(
        id = "group2",
        fixedSingleTax = 1_600.0, // 20% of the minimum wage
        fixedMilitaryLevy = 800.0,
        annualIncomeLimit = 6_672_000.0,
    ),
    GROUP_3_5(
        id = "group3_5",
        singleTaxPercent = 5.0,
        militaryLevyPercent = 1.0,
        annualIncomeLimit = 9_336_000.0,
    ),
    GROUP_3_3(
        id = "group3_3",
        singleTaxPercent = 3.0, // VAT payer
        militaryLevyPercent = 1.0,
        annualIncomeLimit = 9_336_000.0,
    );

    val esv: Double get() = ESV_MONTHLY

    val isFixed: Boolean get() = fixedSingleTax > 0.0

    companion object {
        /** Single social contribution (ESV): 22% of the minimum wage, paid monthly by every group. */
        const val ESV_MONTHLY = 1_760.0

        fun fromId(id: String?) = entries.firstOrNull { it.id == id } ?: GROUP_3_5
    }
}
