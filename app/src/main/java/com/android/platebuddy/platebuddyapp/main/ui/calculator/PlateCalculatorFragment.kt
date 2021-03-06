package com.android.platebuddy.platebuddyapp.main.ui.calculator

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.platebuddy.platebuddyapp.R
import com.android.platebuddy.platebuddyapp.main.hideKeyboard
import com.android.platebuddy.platebuddyapp.models.PlateResult
import kotlinx.android.synthetic.main.fragment_plate_calculator.*
import kotlinx.android.synthetic.main.fragment_plate_calculator.view.*


class PlateCalculatorFragment : Fragment() {

    private lateinit var viewModel: PlateCalculatorViewModel
    private val viewAdapter = PlateWeightsAdapter()
    private lateinit var viewManager: RecyclerView.LayoutManager

    companion object {
        const val PLATES_STATE_KEY = "plates"
        fun newInstance() = PlateCalculatorFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(PlateCalculatorViewModel::class.java)
        viewModel.getPlateResult().observe(this, Observer<PlateResult> { plateResult ->
            viewAdapter.plates = plateResult?.plates ?: emptyList()
            weightToLiftEditTxt.text.clear()
            view?.hideKeyboard()
        })
        viewAdapter.plates = savedInstanceState?.getParcelable<PlateResult>(PLATES_STATE_KEY)?.plates ?: emptyList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_plate_calculator, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(PLATES_STATE_KEY, viewModel.getPlateResult().value)
        super.onSaveInstanceState(outState)
    }

    private fun setupView() {
        viewManager = LinearLayoutManager(context)
        setupWeightToLiftEditText()
        setupBtnCalculate()
        setupPlateResultRelativeaLayout()
        updateCalculateButtonEnabled()
    }

    private fun setupPlateResultRelativeaLayout() {
        relativeLayoutPlates.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    private fun setupBtnCalculate() {
        btnCalculate.setOnClickListener {
            sendCalculateEvent()
        }
    }

    private fun sendCalculateEvent() {
        val weightToLift = weightToLiftEditTxt.text.toString().toFloatOrNull() ?: return
        val barWeight = getBarWeight()
        val plateResult = viewModel.calculatePlateResult(barWeight, weightToLift)
        viewModel.setPlateResult(plateResult)
    }

    private fun setupWeightToLiftEditText() {
        weightToLiftEditTxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                updateCalculateButtonEnabled()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })

        weightToLiftEditTxt.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    sendCalculateEvent()
                    true
                }
                else -> false
            }
        }
    }


    private fun updateCalculateButtonEnabled() {
        val txt = weightToLiftEditTxt.weightToLiftEditTxt.text
        btnCalculate.isEnabled = txt != null && !txt.toString().isEmpty()
    }

    private fun getBarWeight(): Float {
        val currentBarWeightId = barWeightRadioGrp.checkedRadioButtonId
        if (currentBarWeightId == R.id.barWeight45RadioBtn) {
            return 45.0f
        }
        return 35.0f
    }
}