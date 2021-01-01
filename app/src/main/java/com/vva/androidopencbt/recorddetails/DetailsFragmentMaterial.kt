package com.vva.androidopencbt.recorddetails

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputLayout
import com.vva.androidopencbt.R
import com.vva.androidopencbt.RecordsViewModel
import com.vva.androidopencbt.db.CbdDatabase
import com.vva.androidopencbt.db.DbRecord
import kotlinx.android.synthetic.main.fragment_details_material.*

class DetailsFragmentMaterial: Fragment() {
    private lateinit var ll: LinearLayout
    private val viewModel: RecordsViewModel by activityViewModels()

    private lateinit var thoughtInputLayout: TextInputLayout
    private lateinit var rationalInputLayout: TextInputLayout
    private lateinit var situationInputLayout: TextInputLayout
    private lateinit var emotionsInputLayout: TextInputLayout
    private lateinit var feelingsInputLayout: TextInputLayout
    private lateinit var actionsInputLayout: TextInputLayout
    private lateinit var intensitySeekBar: Slider
    private lateinit var percentsTextView : TextView
    private lateinit var allOrNothingCheckBox: CheckBox
    private lateinit var overgeneralizingCheckBox: CheckBox
    private lateinit var filteringCheckBox: CheckBox
    private lateinit var disqualCheckBox: CheckBox
    private lateinit var jumpCheckBox: CheckBox
    private lateinit var magnMinCheckBox: CheckBox
    private lateinit var emoReasonCheckBox: CheckBox
    private lateinit var mustCheckBox: CheckBox
    private lateinit var labelingCheckBox: CheckBox
    private lateinit var personCheckBox: CheckBox

    private lateinit var deleteButton: Button
    private lateinit var saveButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        view.findViewById<Toolbar>(R.id.details_toolbar).apply {
            setupWithNavController(navController, appBarConfiguration)
            menu.forEach { menuItem ->
                menuItem.setOnMenuItemClickListener { item ->
                    if (item.itemId == R.id.menu_help) {
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setMessage(getText(R.string.dialog_help_text))
                        builder.setTitle("Справка")
                        builder.setPositiveButton("OK") { dialog, _ -> dialog.cancel() }
                        val dialog = builder.create()
                        dialog.show()
                        return@setOnMenuItemClickListener true
                    }
                    return@setOnMenuItemClickListener false
                }
            }
        }
    }

    /*
    val listener = View.OnClickListener { v ->
        (v as EditText).setSelection(v.text.length) //может как-то так? но в конец переходит не сразу, а со второго клика
    }*/

    //-----


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ll = inflater.inflate(R.layout.fragment_details_material, container, false) as LinearLayout
        val args = DetailsFragmentMaterialArgs.fromBundle(requireArguments())
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val detailsViewModel: DetailsViewModel by viewModels {
            DetailsViewModelFactory(args.recordKey, CbdDatabase.getInstance(requireActivity().application).databaseDao)
        }

        initControls()

        if(prefs.getBoolean("enable_percents",false))
        {
            percentsTextView.visibility = View.VISIBLE
            intensitySeekBar.labelBehavior = LabelFormatter.LABEL_GONE
            intensitySeekBar.addOnChangeListener {slider, value, fromUser ->  percentsTextView.text = "${value.toInt()}%"}
        }

        deleteButton.setOnClickListener {
            viewModel.deleteRecord(args.recordKey)
            findNavController().popBackStack()
        }

        saveButton.setOnClickListener {
            save(args.recordKey)
            findNavController().popBackStack()
        }

        if (args.recordKey > 0) {
            deleteButton.visibility = View.VISIBLE

            detailsViewModel.getRecord().observe(viewLifecycleOwner) { record ->
                proceedString(record.thoughts, "enable_thoughts", thoughtInputLayout)
                proceedString(record.rational, "enable_rational", rationalInputLayout)
                proceedString(record.emotions, "enable_emotions", emotionsInputLayout)
                proceedString(record.situation, "enable_situation", situationInputLayout)
                proceedString(record.feelings, "enable_feelings", feelingsInputLayout)
                proceedString(record.actions, "enable_actions", actionsInputLayout)

                //vyalichkin----попытки поставить курсор в конец
                /*
                thoughtInputLayout.editText?.setOnClickListener(listener);
                rationalInputLayout.editText?.setOnClickListener(listener);
                situationInputLayout.editText?.setOnClickListener(listener);
                emotionsInputLayout.editText?.setOnClickListener(listener);
                feelingsInputLayout.editText?.setOnClickListener(listener);
                actionsInputLayout.editText?.setOnClickListener(listener);
                */

                if (record.intensity != 0 || prefs.getBoolean("enable_intensity", true)) {
                    intensitySeekBar.value = record.intensity.toFloat()
                } else {
                    ll.findViewById<TextView>(R.id.tvDiscomfortLevel).visibility = View.GONE
                    intensitySeekBar.visibility = View.GONE
                }

                val dist = record.distortions
                if (record.distortions != 0 || prefs.getBoolean("enable_distortions", true)) {
                    allOrNothingCheckBox.isChecked = dist and DbRecord.ALL_OR_NOTHING != 0
                    overgeneralizingCheckBox.isChecked = dist and DbRecord.OVERGENERALIZING != 0
                    filteringCheckBox.isChecked = dist and DbRecord.FILTERING != 0
                    disqualCheckBox.isChecked = dist and DbRecord.DISQUAL_POSITIVE != 0
                    jumpCheckBox.isChecked = dist and DbRecord.JUMP_CONCLUSION != 0
                    magnMinCheckBox.isChecked = dist and DbRecord.MAGN_AND_MIN != 0
                    emoReasonCheckBox.isChecked = dist and DbRecord.EMOTIONAL_REASONING != 0
                    mustCheckBox.isChecked = dist and DbRecord.MUST_STATEMENTS != 0
                    labelingCheckBox.isChecked = dist and DbRecord.LABELING != 0
                    personCheckBox.isChecked = dist and DbRecord.PERSONALIZATION != 0
                } else {
                    hideDistortions()
                }
            }
        } else {
            deleteButton.visibility = View.GONE

            if (!prefs.getBoolean("enable_thoughts", true))
                thoughtInputLayout.visibility = View.GONE
            if (!prefs.getBoolean("enable_situation", true))
                situationInputLayout.visibility = View.GONE
            if (!prefs.getBoolean("enable_emotions", true))
                emotionsInputLayout.visibility = View.GONE
            if (!prefs.getBoolean("enable_rational", true))
                rationalInputLayout.visibility = View.GONE
            if (!prefs.getBoolean("enable_feelings", true))
                feelingsInputLayout.visibility = View.GONE
            if (!prefs.getBoolean("enable_actions", true))
                actionsInputLayout.visibility = View.GONE

            if (!prefs.getBoolean("enable_distortions", true)) {
                hideDistortions()
            }
        }

        return ll
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initControls() {
        thoughtInputLayout = ll.findViewById(R.id.thoughtInputLayout)
        rationalInputLayout = ll.findViewById(R.id.rationalInputLayout)
        emotionsInputLayout = ll.findViewById(R.id.emotionsInputLayout)
        situationInputLayout = ll.findViewById(R.id.situationsInputLayout)
        feelingsInputLayout = ll.findViewById(R.id.feelingsInputLayout)
        actionsInputLayout = ll.findViewById(R.id.actionsInputLayout)

        thoughtInputLayout.editText?.setOnTouchListener(scrollListener)
        rationalInputLayout.editText?.setOnTouchListener(scrollListener)
        emotionsInputLayout.editText?.setOnTouchListener(scrollListener)
        situationInputLayout.editText?.setOnTouchListener(scrollListener)
        feelingsInputLayout.editText?.setOnTouchListener(scrollListener)
        actionsInputLayout.editText?.setOnTouchListener(scrollListener)

        intensitySeekBar = ll.findViewById(R.id.intensitySeekBar)
        intensitySeekBar.setLabelFormatter { n -> String.format("%1d%%",n.toInt()) }

        allOrNothingCheckBox = ll.findViewById(R.id.allOrNothingCheckBox)
        overgeneralizingCheckBox = ll.findViewById(R.id.overgeneralizingCheckBox)
        filteringCheckBox = ll.findViewById(R.id.filteringCheckBox)
        disqualCheckBox = ll.findViewById(R.id.disqualCheckBox)
        jumpCheckBox = ll.findViewById(R.id.jumpCheckBox)
        magnMinCheckBox = ll.findViewById(R.id.magnMinCheckBox)
        emoReasonCheckBox = ll.findViewById(R.id.emoReasonCheckBox)
        mustCheckBox = ll.findViewById(R.id.mustCheckBox)
        labelingCheckBox = ll.findViewById(R.id.labelingCheckBox)
        personCheckBox = ll.findViewById(R.id.personCheckBox)

        deleteButton = ll.findViewById(R.id.deleteButton)
        saveButton = ll.findViewById(R.id.save_button)

        percentsTextView = ll.findViewById(R.id.percentsTextView)
    }

    private fun proceedString(field: String, prefs_name: String, editText: TextInputLayout) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        if (field.isNotEmpty() || prefs.getBoolean(prefs_name, true)) {
            editText.editText?.setText(field)
        } else {
            editText.visibility = View.GONE
        }
    }

    private fun save(id: Long) {
        val thoughts = thoughtInputLayout.editText?.text.toString()
        val rational = rationalInputLayout.editText?.text.toString()
        val situation = situationInputLayout.editText?.text.toString()
        val emotions = emotionsInputLayout.editText?.text.toString()
        val feelings = feelingsInputLayout.editText?.text.toString()
        val actions = actionsInputLayout.editText?.text.toString()
        val intensity = intensitySeekBar.value.toInt()

        var dist = 0x0
        if (allOrNothingCheckBox.isChecked) {
            dist = dist or DbRecord.ALL_OR_NOTHING
        }
        if (overgeneralizingCheckBox.isChecked) {
            dist = dist or DbRecord.OVERGENERALIZING
        }
        if (filteringCheckBox.isChecked) {
            dist = dist or DbRecord.FILTERING
        }
        if (disqualCheckBox.isChecked) {
            dist = dist or DbRecord.DISQUAL_POSITIVE
        }
        if (jumpCheckBox.isChecked) {
            dist = dist or DbRecord.JUMP_CONCLUSION
        }
        if (magnMinCheckBox.isChecked) {
            dist = dist or DbRecord.MAGN_AND_MIN
        }
        if (emoReasonCheckBox.isChecked) {
            dist = dist or DbRecord.EMOTIONAL_REASONING
        }
        if (mustCheckBox.isChecked) {
            dist = dist or DbRecord.MUST_STATEMENTS
        }
        if (labelingCheckBox.isChecked) {
            dist = dist or DbRecord.LABELING
        }
        if (personCheckBox.isChecked) {
            dist = dist or DbRecord.PERSONALIZATION
        }

        if (id > 0) {
            viewModel.updateRecord(id,
                    situation,
                    thoughts,
                    rational,
                    emotions,
                    dist,
                    feelings,
                    actions,
                    intensity)
        } else {
            viewModel.addRecord(DbRecord(
                    0L,
                    situation,
                    thoughts,
                    rational,
                    emotions,
                    dist,
                    feelings,
                    actions,
                    intensity
            ))
        }
    }

    private fun hideDistortions() {
        allOrNothingCheckBox.visibility = View.GONE
        overgeneralizingCheckBox.visibility = View.GONE
        filteringCheckBox.visibility = View.GONE
        disqualCheckBox.visibility = View.GONE
        jumpCheckBox.visibility = View.GONE
        magnMinCheckBox.visibility = View.GONE
        emoReasonCheckBox.visibility = View.GONE
        mustCheckBox.visibility = View.GONE
        labelingCheckBox.visibility = View.GONE
        personCheckBox.visibility = View.GONE
        ll.findViewById<TextView>(R.id.tvDistortions).visibility = View.GONE
    }

    private val scrollListener = View.OnTouchListener {
        view, event ->
            view.performClick()
            if (view.hasFocus()) {
                view.parent.requestDisallowInterceptTouchEvent(true)
                when ((event.action).and(MotionEvent.ACTION_MASK)) {
                    MotionEvent.ACTION_SCROLL -> {
                        view.parent.requestDisallowInterceptTouchEvent(false)
                        return@OnTouchListener true
                    }
                }
            }
        false
    }

    override fun onStop() {
        super.onStop()

        // Hide keyboard when back pressed
        (requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view?.rootView?.windowToken, 0)
    }
}