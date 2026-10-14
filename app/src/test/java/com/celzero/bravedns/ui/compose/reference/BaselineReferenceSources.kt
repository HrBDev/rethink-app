/* Copyright 2026 RethinkDNS and its authors. Licensed under Apache-2.0. */
package com.celzero.bravedns.ui.compose.reference

/** Source pins, not a runtime-validation claim. Updated only with reviewed fixture source. */
internal object BaselineReferenceSources {
    val sponsor = BaselineReferenceSource(
        "app/src/full/res/layout/fragment_sponsor_success.xml",
        "194217acae20a8d206a9e10277b8a71eed5e099704a1271d034dd4a22ac581f3",
        "app/src/test/java/com/celzero/bravedns/ui/compose/reference/BaselineSponsorSuccessReference.kt",
        "009282ea71b69010b0474ac3a3231f7d08b87ed2482fd45e13ded3f7e35b8909",
        "769d740d48d03881a4d16ec590b9fdd18e2b5ea5bb16b77460162df6393a1a52"
    )
    val support = BaselineReferenceSource(
        "app/src/main/res/layout/activity_customer_support.xml",
        "d5f2f1cbd55c5b767ebbf5759e23b6360bfc518ed33edc80d5c5d86b3c898e1c",
        "app/src/test/java/com/celzero/bravedns/ui/compose/reference/BaselineSupportReference.kt",
        "aa80dacd1760ae8163bd0a01cb0fea920617193ea50a35a65235533b18b62e7a",
        "769d740d48d03881a4d16ec590b9fdd18e2b5ea5bb16b77460162df6393a1a52"
    )
    val orbot = BaselineReferenceSource(
        "app/src/full/res/layout/dialog_info_rules_layout.xml",
        "003bbf0ab1175cec5a36f616e5d6f210ff74589beb685a1255cd4226a47e502b",
        "app/src/test/java/com/celzero/bravedns/ui/compose/reference/BaselineOrbotInfoReference.kt",
        "1714de33a8be18f4f0e177f6fa283c1f67eb2e0b882b625d782249d2314b8d9c",
        "769d740d48d03881a4d16ec590b9fdd18e2b5ea5bb16b77460162df6393a1a52"
    )
    val reachability = BaselineReferenceSource(
        "app/src/main/res/layout/dialog_input_ips.xml",
        "bd5d274471390d394e000eb2aec563a34d39231dad308827c2c3c1b91c0e0275",
        "app/src/test/java/com/celzero/bravedns/ui/compose/reference/BaselineReachabilityReference.kt",
        "cbe833e669f20675374ceffe15a81e86222749ea9a1c572b76f33e012cbf15cb",
        "769d740d48d03881a4d16ec590b9fdd18e2b5ea5bb16b77460162df6393a1a52"
    )
    val statistics = BaselineReferenceSource(
        "app/src/main/res/layout/bottom_sheet_summary_stats_filter.xml",
        "ab90b462e28ecb71c2220efbb38090fd60ec54be5460ee4927081f3152329d3e",
        "app/src/test/java/com/celzero/bravedns/ui/compose/reference/BaselineStatisticsFilterReference.kt",
        "0bd2e83bb60563ac35af2f6d4099beb54c1b60caa16c272513bb06dfd68fda73",
        "769d740d48d03881a4d16ec590b9fdd18e2b5ea5bb16b77460162df6393a1a52"
    )
}
