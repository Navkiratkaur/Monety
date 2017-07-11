package open.currency

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatSpinner
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.afollestad.bridge.Bridge

var mainActivityView: MainActivity.MainActivityView? = null

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		mainActivityView = mainActivityView?.apply { (this.parent as ViewGroup?)?.removeView(this) } ?: MainActivityView(this)
		setContentView(mainActivityView)
	}

	@SuppressLint("ViewConstructor")
	class MainActivityView(context: Context) : LinearLayout(context) {

		private val currency1 by lazy { findViewById<TextInputEditText>(R.id.currency1) as TextInputEditText? }
		private val currency2 by lazy { findViewById<TextInputEditText>(R.id.currency2) as TextInputEditText? }
		private val tilCur1 by lazy { findViewById<TextInputLayout>(R.id.tilCur1) as TextInputLayout? }
		private val tilCur2 by lazy { findViewById<TextInputLayout>(R.id.tilCur2) as TextInputLayout? }
		private val spinner1 by lazy { findViewById<AppCompatSpinner>(R.id.spinner1) as AppCompatSpinner? }
		private val spinner2 by lazy { findViewById<AppCompatSpinner>(R.id.spinner2) as AppCompatSpinner? }
		private val convertBtn by lazy { findViewById<Button>(R.id.convertBtn) as Button? }
		private val otherCur by lazy { findViewById<TextView>(R.id.otherCur) as TextView? }
		private var cur1: Int = 0
		private var cur2: Int = 0
		private var lastEdited: Int = 0
		private val currencyArray by lazy { resources.getStringArray(R.array.currencies) }

		init {
			LayoutInflater.from(context).inflate(R.layout.activity_main, this)

			val adapter = ArrayAdapter.createFromResource(context, R.array.currencies, android.R.layout.simple_spinner_item)
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
				} else calculate(currencyArray[cur1], currencyArray[cur2], inputValue)
			}
		}

		private fun calculate(currency: String, desiredCurrency: String, value: Double) {
			Bridge.get("https://api.fixer.io/latest?base=%s", currency)
					.asAsonObject { _, ason, _ ->
						val returnValue = ason?.getDouble("rates." + desiredCurrency) ?: 0.toDouble()
						val tempLastEdited = lastEdited
						(if (tempLastEdited == 1) currency2 else currency1)?.setText((returnValue * value).toString())
						lastEdited = tempLastEdited
						currency1?.isEnabled = true
						currency2?.isEnabled = true
						val otherCurText = context.getString(R.string.other_cur, currencyArray.filter { it != currency && it != desiredCurrency }
								.map { "${(ason?.getDouble("rates." + it) ?: 0.toDouble()) * value} <b>$it</b>" }
								.joinToString())
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
							otherCur?.text = Html.fromHtml(otherCurText, Html.FROM_HTML_MODE_LEGACY)
						} else {
							@Suppress("DEPRECATION")
							otherCur?.text = Html.fromHtml(otherCurText)
						}
					}
		}

	}

}