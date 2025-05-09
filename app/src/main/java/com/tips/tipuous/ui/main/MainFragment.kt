package com.tips.tipuous.ui.main

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.tips.tipuous.BuildConfig
import com.tips.tipuous.R
import com.tips.tipuous.databinding.FragmentMainBinding

class MainFragment : Fragment(R.layout.fragment_main) {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentMainBinding.bind(view)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this.viewLifecycleOwner

        binding.version.text = getString(R.string.version, BuildConfig.VERSION_NAME)

        setUpObservers()
        setUpListeners()
    }

    private fun setUpListeners() {
        binding.shareIcon.setOnClickListener {
            requireContext().handleShareClick(viewModel.formatBillWithTip())
        }
    }

    private fun Context.handleShareClick(info: String) {
        val sendIntent = Intent(
            Intent.ACTION_SEND
        ).apply {
            putExtra(Intent.EXTRA_TEXT, info)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)

        startActivity(shareIntent)
    }

    private fun setUpObservers() {
        binding.billEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                /* Do nothing */
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.i("MainFragment", "Value: ${s.toString()}")
                s?.let {
                    if (it.length >= 0 && it.toString().isNotEmpty()) {
                        try {
                            viewModel.setBill(it.toString().toDouble())
                            viewModel.calculateTip()
                        } catch (e: Exception) {
                            Log.e(
                                "MainFragment",
                                "Failed to calculate number due to an error: \n${e.message}"
                            )
                        }
                    } else {
                        clearValues()
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                /* Do nothing */
            }
        })

        binding.customTipSlider.addOnChangeListener { _, value, _ ->
            // Responds to when slider's value is changed
            Log.i("tipValue", "$value")
            with(viewModel) {
                updateCustomValue(value.toInt())
                calculateTip()
            }
        }

        binding.splitSlider.addOnChangeListener { _, value, _ ->
            with(viewModel) {
                updateSplit(value)
                calculateTip()
            }
        }

        binding.infoSplit.setOnClickListener {
            val alert: AlertDialog = AlertDialog.Builder(requireContext())
                .setTitle("Splitting Information")
                .setMessage("Split tips are rounded and may not be exact.")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .create()

            alert.show()
        }
    }

    private fun clearValues() {
        viewModel.clearValues()
        viewModel.updateTotal(getString(R.string.em_dash))
    }
}
