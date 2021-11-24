package com.tips.tipuous

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.databinding.DataBindingUtil
import com.tips.tipuous.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    val tips = Tips(0.0, 0.0, 0.0)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }

        binding = DataBindingUtil.setContentView<ActivityMainBinding?>(this, R.layout.activity_main).apply {
            composeView.setContent {
                BasicsCodelabTheme {
                    // A surface container using the 'background' color from the theme
                    Surface() {
                        Greeting()
                    }
                }
                Spacer(modifier = Modifier.height(150.dp))
            }
        }

        binding.tips = tips

        binding.buttonFivePercent.setOnClickListener {
            if (checkForEmptyField()) {
                populateFields(it, 5.0)
            }
        }

        binding.buttonTenPercent.setOnClickListener {
            if (checkForEmptyField()) {
                populateFields(it, 10.0)
            }
        }
        binding.buttonFifteenPercent.setOnClickListener {
            if (checkForEmptyField()) {
                populateFields(it, 15.0)
            }
        }
        binding.buttonTwentyPercent.setOnClickListener {
            if (checkForEmptyField()) {
                populateFields(it, 20.0)
            }
        }

        binding.buttonCalculate.setOnClickListener {
            if (binding.otherAmount.text.isNotEmpty()) {
                if (checkForEmptyField()) {
                    val customPercentage = binding.otherAmount.text.toString().toDouble()
                    populateFields(it, customPercentage)
                }
            } else Toast.makeText(this, R.string.toast_input_error, Toast.LENGTH_SHORT).show()
        }

    }

    private fun populateFields(view: View, percent: Double) {
        val tip = calculateTip(percent)
        val total = calculateTotal(tip)
        applyViewChanges(tip, total)
        hideSoftKeyboard(view)
    }

    private fun calculateTotal(tip: Double): Double {
        val billAmount = binding.enterAmount.text.toString().toDouble()
        return billAmount + tip
    }

    private fun calculateTip(percent: Double): Double {
        return binding.enterAmount.text.toString().toDouble() * (percent / 100.0)
    }

    private fun applyViewChanges(tip: Double, total: Double) {
        binding.apply {
            tipAmount.text = "$%.2f".format(tip)
            grandTotal.text = "$%.2f".format(total)
        }
    }

    private fun hideSoftKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun checkForEmptyField(): Boolean {
        if (binding.enterAmount.text.isEmpty()) {
            Toast.makeText(this, R.string.toast_input_error, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}

@Composable
private fun Greeting() {
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(fontSize = 24.sp, text = "Bill Amount")
    }

}
