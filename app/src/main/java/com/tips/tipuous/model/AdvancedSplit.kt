package com.tips.tipuous.model

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class AdvancedSplit(
    val people: List<Person> = emptyList(),
) : java.io.Serializable

@Serializable
data class Person(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val items: List<Item> = emptyList(),
) : java.io.Serializable

@Serializable
data class Item(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val amount: Double,
) : java.io.Serializable
