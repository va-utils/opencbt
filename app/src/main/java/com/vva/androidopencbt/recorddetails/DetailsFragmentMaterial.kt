package com.vva.androidopencbt.recorddetails

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
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
import com.vva.androidopencbt.*
import com.vva.androidopencbt.db.CbdDatabase
import com.vva.androidopencbt.db.DbRecord
import org.joda.time.DateTime

class DetailsFragmentMaterial: Fragment() {
    private lateinit var ll: LinearLayout
    private val viewModel: RecordsViewModel by activityViewModels()
    private lateinit var database: CbdDatabase
    private lateinit var args: DetailsFragmentMaterialArgs
    private val detailsViewModel: DetailsViewModel by viewModels {
        DetailsViewModelFactory(args.recordKey, database.databaseDao)
    }

    private lateinit var dateTimeInputLayout: TextInputLayout;
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

    private lateinit var suggestTextView : TextView; //for 0.6 version
    private val mustWords = arrayOf("должн","нужно","обязан");
    private val overgenWords = arrayOf("всегда", "вечно", "как обычно","никогда");
    private val labelsWords = arrayOf("дебил","мудак","плох","урод","неудачни","никчемн","долбо","идиот","кретин","лох","лузер","козёл","козел","стерва","сука","говн","гавн");
    private val jumpconWords = arrayOf("счита", "думают", "думает", "будет", "будут");
    private val personWords = arrayOf("из-за меня", "потому что я", "моя вина", "я виноват");
    private val emotionWords = arrayOf("чувствую","кажется");
    private val allornothWords = arrayOf("полный","полная","конец","пиздец","кошмар");

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
        database = CbdDatabase.getInstance(requireActivity().application)
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        id = args.recordKey

        initControls()
        suggestTextView.visibility = View.GONE;

        if(prefs.getBoolean("enable_discret_percents",false) && id == 0L)
        {
           when(prefs.getString("discret_value","10%"))
           {
               "5%"  -> intensitySeekBar.stepSize = 5F
               "10%" -> intensitySeekBar.stepSize = 10F
               "20%" ->intensitySeekBar.stepSize = 20F
           }
        }

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

        //for 0.6 version
        if(prefs.getBoolean("enable_suggest",false)) {
            thoughtInputLayout.editText?.addTextChangedListener {
                if (it != null) {
                    val str: String = it.toString().toLowerCase();
                    if(!str.isEmpty()) {
                        var result: String = getString(R.string.dist_help_welcome);

                        var distFinded: Boolean = false;

                        val text = str.split(' ');
                        if (containsWord(text, mustWords)) {
                            distFinded = true;
                            result += getString(R.string.dist_must_statement) + "\n";
                        }

                        if (containsWord(text, overgenWords)) {
                            distFinded = true;
                            result += getString(R.string.dist_overgeneralizing) + "\n";
                        }

                        if (containsWord(text, labelsWords)) {
                            distFinded = true;
                            result += getString(R.string.dist_labeling) + "\n";
                        }

                        if (containsWord(text, jumpconWords)) {
                            distFinded = true;
                            result += getString(R.string.dist_jump_conclusion) + "\n";
                        }

                        if (containsWord(text, personWords)) {
                            distFinded = true;
                            result += getString(R.string.dist_personalistion) + "\n";
                        }

                        if (containsWord(text, emotionWords)) {
                            distFinded = true;
                            result += getString(R.string.dist_emotional_reasoning) + "\n";
                        }

                        if (containsWord(text, allornothWords)) {
                            distFinded = true;
                            result += getString(R.string.dist_all_or_nothing);
                        }

                        if (distFinded) {
                            suggestTextView.visibility = View.VISIBLE;
                            suggestTextView.text = result;
                        } else {
                            suggestTextView.visibility = View.GONE;
                        }
                    }
                }
            }
        }

        if (id > 0) {

            //detailsViewModel.setRecordDate(detailsViewModel.currentRecord!!.datetime);
            deleteButton.visibility = View.VISIBLE

            detailsViewModel.getRecord().observe(viewLifecycleOwner) { record ->
                detailsViewModel.currentRecord = record
                proceedString(record.thoughts, "enable_thoughts", thoughtInputLayout)
                proceedString(record.rational, "enable_rational", rationalInputLayout)
                proceedString(record.emotions, "enable_emotions", emotionsInputLayout)
                proceedString(record.situation, "enable_situation", situationInputLayout)
                proceedString(record.feelings, "enable_feelings", feelingsInputLayout)
                proceedString(record.actions, "enable_actions", actionsInputLayout)

                detailsViewModel.setRecordDate(record.datetime)

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

        dateTimeInputLayout = ll.findViewById(R.id.dateTimeInputLayout);
        detailsViewModel.recordDate.observe(viewLifecycleOwner) {
            dateTimeInputLayout.editText?.setText(it.getDateTimeString())
        }

        dateTimeInputLayout.editText?.setOnClickListener {
            val date = detailsViewModel.recordDateTime
            setDateTime(date);
        }

        dateTimeInputLayout.setEndIconOnClickListener {
            if(args.recordKey>0)
                detailsViewModel.setRecordDate(detailsViewModel.currentRecord!!.datetime)
            else
                detailsViewModel.setRecordDate(DateTime())
        }

        thoughtInputLayout = ll.findViewById(R.id.thoughtInputLayout)
        rationalInputLayout = ll.findViewById(R.id.rationalInputLayout)
        emotionsInputLayout = ll.findViewById(R.id.emotionsInputLayout)
        situationInputLayout = ll.findViewById(R.id.situationsInputLayout)
        feelingsInputLayout = ll.findViewById(R.id.feelingsInputLayout)
        actionsInputLayout = ll.findViewById(R.id.actionsInputLayout)

        thoughtInputLayout.editText?.setOnTouchListener(scrollListener)
        thoughtInputLayout.setEndIconOnClickListener {clearConfirm(thoughtInputLayout.editText)};

        rationalInputLayout.editText?.setOnTouchListener(scrollListener)
        rationalInputLayout.setEndIconOnClickListener{clearConfirm(rationalInputLayout.editText)};

        emotionsInputLayout.editText?.setOnTouchListener(scrollListener)
        emotionsInputLayout.setEndIconOnClickListener{clearConfirm(emotionsInputLayout.editText)};

        situationInputLayout.editText?.setOnTouchListener(scrollListener)
        situationInputLayout.setEndIconOnClickListener{clearConfirm(situationInputLayout.editText)};

        feelingsInputLayout.editText?.setOnTouchListener(scrollListener)
        feelingsInputLayout.setEndIconOnClickListener{clearConfirm(feelingsInputLayout.editText)};

        actionsInputLayout.editText?.setOnTouchListener(scrollListener)
        actionsInputLayout.setEndIconOnClickListener{clearConfirm(actionsInputLayout.editText)};

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

        suggestTextView = ll.findViewById(R.id.tvSuggest); //for 0.6 version



    }

    private fun clearConfirm(et : EditText?) {
        //Toast.makeText(requireContext(),view.parent,Toast.LENGTH_LONG).show();
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.clear_title).
        setMessage(R.string.clear_confirm).
        setNegativeButton(R.string.remove) { _: DialogInterface, _: Int ->
            et?.text?.clear();
        }
                .setNeutralButton(R.string.cancel)
                { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                }.show()
    }

    private fun containsWord(input : List<String>, words : Array<String>) : Boolean
    {
        for(s in input)
        {
            for(w in words)
            {
                if(s.startsWith(w))
                    return true;
            }
        }
        return false;
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
                    record.intensity, detailsViewModel.recordDateTime)
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
                detailsViewModel.recordDateTime
               // if (id == 0L) detailsViewModel.recordDateTime else detailsViewModel.currentRecord?.datetime ?: detailsViewModel.recordDateTime
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

    @SuppressLint("SuspiciousIndentation")
    private fun makeHelpDialog(): AlertDialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
                        builder.setMessage(getText(R.string.dialog_help_text))
                        builder.setTitle("Справка")
                        builder.setPositiveButton("OK") { dialog, _ -> dialog.cancel() }
        return builder.create()
    }

    fun setDateTime(dt: DateTime)// : DateTime
    {
        var year : Int = 0
        var mon : Int = 0
        var day : Int = 0
        var hour : Int = 0
        var min : Int = 0

        val recordTpListener = TimePickerDialog.OnTimeSetListener { _ , h, m  ->
            hour = h;  min = m
            detailsViewModel.setRecordDate(DateTime(year,mon,day,hour,min))
        }

        val recordDpListener = DatePickerDialog.OnDateSetListener { tp: DatePicker, y, m, d  ->
            tp.maxDate = DateTime().millis
            year=y; mon = m+1; day = d;
            TimePickerDialog(requireContext(), recordTpListener, dt.hourOfDay, dt.minuteOfHour, true).show();
        }

        var dialog = DatePickerDialog(requireContext(), recordDpListener, dt.year, dt.monthOfYear-1, dt.dayOfMonth);
        dialog.datePicker.maxDate = dt.millis
        dialog.show();
    }
}