package com.tips.tipuous.data

import com.tips.tipuous.model.CountryTippingInfo

object TippingGuideDataSource {
    val countries =
        listOf(
            CountryTippingInfo(
                countryName = "United States",
                flagEmoji = "🇺🇸",
                suggestedTipRange = "15% - 25%",
                restaurantEtiquette = "Expected in sit-down restaurants. 18-20% is the standard for good service.",
                taxiEtiquette = "10% - 15% of the fare is customary.",
                hotelEtiquette = "$2-$5 per night for housekeeping; $1-$2 per bag for porters.",
                generalNotes = "Tipping is a major part of service worker income in the US.",
            ),
            CountryTippingInfo(
                countryName = "United Kingdom",
                flagEmoji = "🇬🇧",
                suggestedTipRange = "10% - 12.5%",
                restaurantEtiquette = "Check if a 'service charge' is already added. If not, 10-12% is standard.",
                taxiEtiquette = "Round up to the nearest pound for short trips; 10% for longer ones.",
                hotelEtiquette = "Optional, but £1-£2 for porters is appreciated.",
                generalNotes = "Tipping is less formal than in the US but increasingly common in cities.",
            ),
            CountryTippingInfo(
                countryName = "Japan",
                flagEmoji = "🇯🇵",
                suggestedTipRange = "0%",
                restaurantEtiquette = "No tipping. It can be seen as insulting or confusing.",
                taxiEtiquette = "No tipping required. Fares are clearly calculated.",
                hotelEtiquette = "No tipping. Exceptional service is included in the price.",
                generalNotes = "Politeness and a simple 'Arigato' are preferred over money.",
            ),
            CountryTippingInfo(
                countryName = "France",
                flagEmoji = "🇫🇷",
                suggestedTipRange = "Rounding up",
                restaurantEtiquette = "'Service Compris' (service included) is mandatory. Leaving small change (a few euros) is common.",
                taxiEtiquette = "Usually not expected, but rounding up is fine.",
                hotelEtiquette = "€1-€2 per bag for porters; housekeeping is optional.",
                generalNotes = "Waiters receive a full salary, so tips are truly extra 'gratitude'.",
            ),
            CountryTippingInfo(
                countryName = "Brazil",
                flagEmoji = "🇧🇷",
                suggestedTipRange = "10% (Included)",
                restaurantEtiquette = "A 10% 'serviço' is usually added to the bill automatically. You don't need to add more.",
                taxiEtiquette = "Not expected, but rounding up the fare is common.",
                hotelEtiquette = "A small tip for luggage assistance is appreciated.",
                generalNotes = "Always check the bill for '10%' before adding extra.",
            ),
            CountryTippingInfo(
                countryName = "Italy",
                flagEmoji = "🇮🇹",
                suggestedTipRange = "Rounding up / €1-€2",
                restaurantEtiquette = "Look for 'Coperto' (cover charge). A small extra tip is appreciated but not required.",
                taxiEtiquette = "Not expected, but rounding up to the nearest Euro is common.",
                hotelEtiquette = "€1-€2 per bag or per night for housekeeping.",
                generalNotes = "In tourist areas, service might be included, but it's less common than in France.",
            ),
            CountryTippingInfo(
                countryName = "Australia",
                flagEmoji = "🇦🇺",
                suggestedTipRange = "0% - 10%",
                restaurantEtiquette = "Not required. Leaving 10% for exceptional service in high-end places is becoming common.",
                taxiEtiquette = "Rounding up to the nearest dollar is standard.",
                hotelEtiquette = "Not expected.",
                generalNotes = "Minimum wages are high, so tipping is not a standard part of income.",
            ),
        ).sortedBy { it.countryName }
}
