package com.tips.tipuous

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun inputNumberIntoAmount() {

        //onView(withId(R.id.enter_amount)).perform(typeText("12.99"))
    }

    @Test
    fun inputNumberIntoOtherAmount() {
        //onView(withId(R.id.other_amount)).perform(typeText("25"))
    }

    @Test
    fun inputNumberIntoAmountAndSelectFivePercent() {
        inputNumberIntoAmount()
        //onView(withId(R.id.button_five_percent)).perform(click())
        onView(withText("$0.65")).check(matches(isDisplayed()))
        onView(withText("$13.64")).check(matches(isDisplayed()))
    }

    @Test
    fun inputNumberIntoAmountAndSelectTenPercent() {
        inputNumberIntoAmount()
        //onView(withId(R.id.button_ten_percent)).perform(click())
        onView(withText("$1.30")).check(matches(isDisplayed()))
        onView(withText("$14.29")).check(matches(isDisplayed()))
    }

    @Test
    fun inputNumberIntoAmountAndSelectFifteenPercent() {
        inputNumberIntoAmount()
        //onView(withId(R.id.button_fifteen_percent)).perform(click())
        onView(withText("$1.95")).check(matches(isDisplayed()))
        onView(withText("$14.94")).check(matches(isDisplayed()))
    }

    @Test
    fun inputNumberIntoAmountAndSelectTwentyPercent() {
        inputNumberIntoAmount()
        //onView(withId(R.id.button_twenty_percent)).perform(click())
        onView(withText("$2.60")).check(matches(isDisplayed()))
        onView(withText("$15.59")).check(matches(isDisplayed()))
    }

    @Test
    fun inputNumberIntoOtherAmountAndIntoOtherTipAmount() {
        inputNumberIntoAmount()
        inputNumberIntoOtherAmount()
        //onView(withId(R.id.button_calculate)).perform(click())
        onView(withText("$3.25")).check(matches(isDisplayed()))
        onView(withText("$16.24")).check(matches(isDisplayed()))
    }
}
