package open.currency

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatSpinner
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import com.afollestad.bridge.Bridge

class MainActivity : AppCompatActivity() {

	private val currency1 by lazy { findViewById(R.id.currency1) as TextInputEditText? }
	private val currency2 by lazy { findViewById(R.id.currency2) as TextInputEditText? }
	private val tilCur1 by lazy { findViewById(R.id.tilCur1) as TextInputLayout? }
	private val tilCur2 by lazy { findViewById(R.id.tilCur2) as TextInputLayout? }
	private val spinner1 by lazy { findViewById(R.id.spinner1) as AppCompatSpinner? }
	private val spinner2 by lazy { findViewById(R.id.spinner2) as AppCompatSpinner? }
	private val convertBtn by lazy { findViewById(R.id.convertBtn) as Button? }
	private var cur1: Int = 0
	private var cur2: Int = 0
	private var lastEdited: Int = 0
	private val currencyArray by lazy { resources.getStringArray(R.array.currencies) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val adapter = ArrayAdapter.createFromResource(this, R.array.currencies, android.R.layout.simple_spinner_item)
		adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)

		lastEdited = 1
		currency1?.addTextChangedListener(object : TextWatcher {
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun afterTextChanged(s: Editable) {
				lastEdited = 1
			}
		})
		currency2?.addTextChangedListener(object : TextWatcher {
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun afterTextChanged(s: Editable) {
				lastEdited = 2
			}
		})

		spinner1?.adapter = adapter
		spinner1?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
			override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
				cur1 = position
				tilCur1?.hint = currencyArray[position]
			}

			override fun onNothingSelected(parent: AdapterView<*>) {}
		}
		spinner2?.adapter = adapter
		spinner2?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
			override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
				cur2 = position
				tilCur2?.hint = currencyArray[position]
			}

			override fun onNothingSelected(parent: AdapterView<*>) {}
		}

		convertBtn?.setOnClickListener { v ->
			currency1?.isEnabled = false
			currency2?.isEnabled = false

			val inputValue = if (lastEdited == 1) currency1?.text?.toString()?.toDoubleOrNull() ?: 0.toDouble()
			else currency2?.text?.toString()?.toDoubleOrNull() ?: 0.toDouble()

			if (inputValue == 0.toDouble()) {
				Snackbar.make(v, R.string.empty_value, Snackbar.LENGTH_LONG).show()
				currency1?.isEnabled = true
				currency2?.isEnabled = true
			} else if (cur1 == cur2) {
				Snackbar.make(v, R.string.sam_cur, Snackbar.LENGTH_LONG).show()
				currency1?.isEnabled = true
				currency2?.isEnabled = true
			} else {
				if (lastEdited == 1) calculate(currency2, currencyArray[cur1], currencyArray[cur2], inputValue)
				else calculate(currency1, currencyArray[cur2], currencyArray[cur1], inputValue)
			}
		}
	}

	private fun calculate(editText: EditText?, currency: String, desiredCurrency: String, value: Double) {
		Bridge.get("http://api.fixer.io/latest?base=%s&symbols=%s", currency, desiredCurrency)
				.asAsonObject { _, ason, _ ->
					val returnValue = ason?.getDouble("rates." + desiredCurrency) ?: 0.toDouble()
					editText?.setText((returnValue * value).toString())
					currency1?.isEnabled = true
					currency2?.isEnabled = true
				}
	}

}