package com.kabos.spotifydj.data.model

enum class RecommendParameter(val value: Double) {
    MinTempoRate(0.9),
    MaxTempoRate(1.1),
    MinDanceabilityRate(0.8),
    MaxDanceabilityRate(1.2),
    MinEnergyRate(1.0),
    MaxEnergyRate(1.0)
}
