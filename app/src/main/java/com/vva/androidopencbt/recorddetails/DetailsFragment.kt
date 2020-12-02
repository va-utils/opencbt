package com.vva.androidopencbt.recorddetails

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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
import com.vva.androidopencbt.R
import com.vva.androidopencbt.RecordsViewModel
import com.vva.androidopencbt.db.CbdDatabase
import com.vva.androidopencbt.db.DbRecord

class DetailsFragment: Fragment() {
    private lateinit var ll: LinearLayout

    private val viewModel: RecordsViewModel by activityViewModels()

    private lateinit var thoughtEditText: EditText
    private lateinit var rationalEditText: EditText
    private lateinit var situationEditText: EditText
    private lateinit var emotionEditText: EditText
    private lateinit var feelingsEditText: EditText
    private lateinit var actionsEditText: EditText
    private lateinit var intensitySeekBar: SeekBar
    private lateinit var percentTextView: TextView
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

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ll = inflater.inflate(R.layout.fragment_record_details, container, false) as LinearLayout
        val arguments = DetailsFragmentArgs.fromBundle(requireArguments())
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val detailsViewModel: DetailsViewModel by viewModels {
            DetailsViewModelFactory(arguments.recordKey, CbdDatabase.getInstance(requireActivity().application).databaseDao)
        }

        // инициализация контроллов
        thoughtEditText = ll.findViewById<EditText>(R.id.thoughtEditText).apply {
            setOnClickListener {
                setEditText(it)
            }
        }
        rationalEditText = ll.findViewById<EditText>(R.id.rationalEditText).apply {
            setOnClickListener {
                setEditText(it)
            }
        }
        emotionEditText = ll.findViewById<EditText>(R.id.emotionEditText).apply {
            setOnClickListener {
                setEditText(it)
            }
        }
        situationEditText = ll.findViewById<EditText>(R.id.situationEditText).apply {
            setOnClickListener {
                setEditText(it)
            }
        }
        feelingsEditText = ll.findViewById<EditText>(R.id.feelingsEditText).apply {
            setOnClickListener {
                setEditText(it)
            }
        }
        actionsEditText = ll.findViewById<EditText>(R.id.actionsEditText).apply {
            setOnClickListener {
                setEditText(it)
            }
        }
        percentTextView = ll.findViewById(R.id.percentsTextView)

        intensitySeekBar = ll.findViewById<SeekBar>(R.id.intensitySeekBar).apply {
            setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                            p0?.progress?.let { percentTextView.text = "$it %" }
                        }

                        override fun onStartTrackingTouch(p0: SeekBar?) {
                        }

                        override fun onStopTrackingTouch(p0: SeekBar?) {
                        }
                    }
            )
        }

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

        deleteButton = ll.findViewById<Button>(R.id.deleteButton).apply {
            setOnClickListener {
                viewModel.deleteRecord(arguments.recordKey)
                findNavController().popBackStack()
            }
        }
        saveButton = ll.findViewById<Button>(R.id.save_button).apply {
            setOnClickListener {
                save(arguments.recordKey)
                findNavController().popBackStack()
            }
        }

        if (arguments.recordKey > 0) {
            deleteButton.visibility = View.VISIBLE

            detailsViewModel.getRecord().observe(viewLifecycleOwner) { record ->
                proceedString(record.thoughts, "enable_thoughts", thoughtEditText, R.id.nr_thoughtTextView)
                proceedString(record.rational, "enable_rational", rationalEditText, R.id.nr_rationalTextView)
                proceedString(record.emotions, "enable_emotions", emotionEditText, R.id.nr_emotionTextView)
                proceedString(record.situation, "enable_situation", situationEditText, R.id.nr_situationTextView)
                proceedString(record.feelings, "enable_feelings", feelingsEditText, R.id.nr_feelingsTextView)
                proceedString(record.actions, "enable_actions", actionsEditText, R.id.nr_actionsTextView)

                if (record.intensity != 0 || prefs.getBoolean("enable_intensity", true)) {
                    intensitySeekBar.progress = record.intensity
                    percentTextView.text = "${record.intensity} %"
                } else {
                    ll.findViewById<View>(R.id.nr_intensityTextView).visibility = View.GONE
                    intensitySeekBar.visibility = View.GONE
                    percentTextView.visibility = View.GONE
                }

                val dist = record.distortions
                if (record.distortions != 0 || prefs.getBoolean("enable_distortions", true)) {
                    //---сделать разбор dist и отобразить CheckBox-ы
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
                    ll.findViewById<View>(R.id.nr_distortionTextView).visibility = View.GONE
                }
            }
        } else {
            deleteButton.visibility = View.GONE
            // свежая запись, смотрим какие поля показывать
            if (!prefs.getBoolean("enable_thoughts", true)) {
                thoughtEditText.visibility = View.GONE
                ll.findViewById<View>(R.id.nr_thoughtTextView).visibility = View.GONE
            }

            if (!prefs.getBoolean("enable_situation", true)) {
                situationEditText.visibility = View.GONE
                ll.findViewById<View>(R.id.nr_situationTextView).visibility = View.GONE
            }

            if (!prefs.getBoolean("enable_emotions", true)) {
                emotionEditText.visibility = View.GONE
                ll.findViewById<View>(R.id.nr_emotionTextView).visibility = View.GONE
            }

            if (!prefs.getBoolean("enable_intensity", true)) {
                intensitySeekBar.visibility = View.GONE
                ll.findViewById<View>(R.id.nr_intensityTextView).visibility = View.GONE
                percentTextView.visibility = View.GONE
            }

            if (!prefs.getBoolean("enable_rational", true)) {
                rationalEditText.visibility = View.GONE
                ll.findViewById<View>(R.id.nr_rationalTextView).visibility = View.GONE
            }

            if (!prefs.getBoolean("enable_feelings", true)) {
                feelingsEditText.visibility = View.GONE
                ll.findViewById<View>(R.id.nr_feelingsTextView).visibility = View.GONE
            }

            if (!prefs.getBoolean("enable_actions", true)) {
                actionsEditText.visibility = View.GONE
                ll.findViewById<View>(R.id.nr_actionsTextView).visibility = View.GONE
            }

            if (!prefs.getBoolean("enable_distortions", true)) {
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
                ll.findViewById<View>(R.id.nr_distortionTextView).visibility = View.GONE
            }
        }

        return ll
    }

    private fun save(id: Long) {
        val thought = thoughtEditText.text.toString()
        val disput = rationalEditText.text.toString()
        val situation = situationEditText.text.toString()
        val emotion = emotionEditText.text.toString()
        val feelings = feelingsEditText.text.toString()
        val actions = actionsEditText.text.toString()
        val intensity: Int = intensitySeekBar.progress

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
                    thought,
                    disput,
                    emotion,
                    dist,
                    feelings,
                    actions,
                    intensity)
        } else {
            viewModel.addRecord(DbRecord(
                    0L,
                    situation,
                    thought,
                    disput,
                    emotion,
                    dist,
                    feelings,
                    actions,
                    intensity
            ))
        }
    }

    private fun proceedString(field: String?, prefs_name: String, editText: EditText, nrTv: Int) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        if (field?.isNotEmpty() == true || prefs.getBoolean(prefs_name, true)) {
            editText.setText(field)
        } else {
            ll.findViewById<View>(nrTv).visibility = View.GONE
            editText.visibility = View.GONE
        }
    }

    private fun setEditText(v: View) {
        val inflater = LayoutInflater.from(requireContext())
        val promptView = inflater.inflate(R.layout.prompt, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(promptView)
        val promptTextView = promptView.findViewById<TextView>(R.id.prompt_textview)
        val promptEditText = promptView.findViewById<EditText>(R.id.prompt_edittext)
        val currentEditText = v as EditText
        builder.setCancelable(true)

        when (v.getId()) {
            R.id.situationEditText -> promptTextView.setText(R.string.newrecord_text_situation)
            R.id.thoughtEditText -> promptTextView.setText(R.string.newrecord_text_thought)
            R.id.rationalEditText -> promptTextView.setText(R.string.newrecord_text_rational)
            R.id.emotionEditText -> promptTextView.setText(R.string.newrecord_text_emotions)
            R.id.feelingsEditText -> promptTextView.setText(R.string.newrecord_text_feelings)
            R.id.actionsEditText -> promptTextView.setText(R.string.newrecord_text_actions)
            else -> {
            }
        }

        if (currentEditText.text.toString().isNotEmpty()) {
            promptEditText.text = currentEditText.text
            promptEditText.setSelection(promptEditText.text.length)
        }
        builder.setPositiveButton("OK") { _, _ ->
            currentEditText.text = promptEditText.text
            currentEditText.clearFocus()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
        val dialog = builder.create()
        try {
            dialog.window!!.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        } catch (e: Exception) {
        }
        dialog.show()
    }
}