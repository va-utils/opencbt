package com.vva.androidopencbt.recorddetails

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.transition.MaterialContainerTransform
import com.vva.androidopencbt.R
import com.vva.androidopencbt.RecordsViewModel
import com.vva.androidopencbt.db.CbdDatabase
import com.vva.androidopencbt.db.DbRecord
import com.vva.androidopencbt.themeColor
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.DateTime
import javax.inject.Inject

@AndroidEntryPoint
class DetailsFragmentMaterial: Fragment() {
    private lateinit var ll: LinearLayout
    private val viewModel: RecordsViewModel by activityViewModels()
    private val detailsViewModel: DetailsViewModel by activityViewModels()
    @Inject
    lateinit var database: CbdDatabase
    private lateinit var args: DetailsFragmentMaterialArgs
//    private val detailsViewModel: DetailsViewModel by viewModels {
//        DetailsViewModelFactory(args.recordKey, database.databaseDao)
//    }

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

    private var id: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.myNavHostFragment
            duration = resources.getInteger(R.integer.record_motion_duration).toLong()
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().themeColor(R.attr.colorSurface))
        }
        setHasOptionsMenu(true)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ll = inflater.inflate(R.layout.fragment_details, container, false) as LinearLayout
        args = DetailsFragmentMaterialArgs.fromBundle(requireArguments())
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        id = args.recordKey
        detailsViewModel.recordKey = id

        initControls()

        if(prefs.getBoolean("enable_percents",false)) {
            percentsTextView.visibility = View.VISIBLE
            intensitySeekBar.labelBehavior = LabelFormatter.LABEL_GONE
            intensitySeekBar.addOnChangeListener {
                _, value, _ ->
                percentsTextView.text = "${value.toInt()}%"
            }
        }

        deleteButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.details_fragment_remove_confirm_dialog_title).
                    setMessage(R.string.details_fragment_remove_confirm_dialog_message).
                    setNegativeButton(R.string.remove) { _: DialogInterface, _: Int ->
                        detailsViewModel.deleteRecordById(id)
                        findNavController().popBackStack()
                    }
                    .setNeutralButton(R.string.cancel)
                    { dialogInterface: DialogInterface, _: Int ->
                        dialogInterface.dismiss()
                    }.show()
        }

        saveButton.setOnClickListener {
            save()
            findNavController().popBackStack()
        }

        if (id > 0) {
            deleteButton.visibility = View.VISIBLE

            detailsViewModel.getRecord().observe(viewLifecycleOwner) { record ->
                detailsViewModel.currentRecord = record
                proceedString(record.thoughts, "enable_thoughts", thoughtInputLayout)
                proceedString(record.rational, "enable_rational", rationalInputLayout)
                proceedString(record.emotions, "enable_emotions", emotionsInputLayout)
                proceedString(record.situation, "enable_situation", situationInputLayout)
                proceedString(record.feelings, "enable_feelings", feelingsInputLayout)
                proceedString(record.actions, "enable_actions", actionsInputLayout)

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
            detailsViewModel.currentRecord = DbRecord()
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

        viewModel.askDetailsFragmentConfirm.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    if (detailsViewModel.isRecordHasChanged(getRecordFromInput())) {
                        MaterialAlertDialogBuilder(requireContext())
                                .setTitle(R.string.details_fragment_confirm_dialog_title)
                                .setMessage(R.string.details_fragment_confirm_dialog_message)
                                .setNegativeButton(R.string.exit) { dialogInterface: DialogInterface, _: Int ->
                                    dialogInterface.dismiss()
                                    viewModel.detailsFragmentRollbackChanges()
                                }
                                .setNeutralButton(R.string.cancel) { dialogInterface: DialogInterface, _: Int ->
                                    dialogInterface.dismiss()
                                    viewModel.detailsFragmentConfirmChangesCancel()
                                }
                                .show()
                    } else {
                        viewModel.detailsFragmentRollbackChanges()
                    }
                }
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

    private fun save() {
        val record = getRecordFromInput()

        if (id > 0) {
            detailsViewModel.updateRecord(id,
                    record.situation,
                    record.thoughts,
                    record.rational,
                    record.emotions,
                    record.distortions,
                    record.feelings,
                    record.actions,
                    record.intensity)
        } else {
            detailsViewModel.addRecord(record)
        }
    }

    private fun getRecordFromInput(): DbRecord {
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

        return DbRecord(
                id,
                situation,
                thoughts,
                rational,
                emotions,
                dist,
                feelings,
                actions,
                intensity,
                if (id == 0L) DateTime() else detailsViewModel.currentRecord?.datetime ?: DateTime()
        )
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
                when (event.action.and(MotionEvent.ACTION_MASK)) {
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
        detailsViewModel.currentRecord = null

        // Hide keyboard when back pressed
        (requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view?.rootView?.windowToken, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_newrecord, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_help -> {
                makeHelpDialog().show()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun makeHelpDialog(): AlertDialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
                        builder.setMessage(getText(R.string.dialog_help_text))
                        builder.setTitle("Справка")
                        builder.setPositiveButton("OK") { dialog, _ -> dialog.cancel() }
        return builder.create()
    }
}