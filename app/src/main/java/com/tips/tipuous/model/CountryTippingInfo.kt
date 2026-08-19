package com.tips.tipuous.model

/**
 * Data model for tipping standards and etiquette in a specific country.
 */
data class CountryTippingInfo(
    val countryName: String,
    val flagEmoji: String,
    val suggestedTipRange: String,
    val restaurantEtiquette: String,
    val taxiEtiquette: String,
    val hotelEtiquette: String,
    val generalNotes: String,
)
