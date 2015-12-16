/*
 *     OpenCurrency, a very simple currency converter for Android
 *
 *     Copyright (C) 2015  Jan-Lukas Else (janlukas.else@gmail.com)
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package open.currency;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.afollestad.async.Action;
import com.afollestad.bridge.Bridge;

import org.json.JSONObject;

import jlelse.simpleui.SimpleActivity;

public class MainActivity extends SimpleActivity {

    private EditText currency1, currency2;
    private TextInputLayout tilCur1, tilCur2;
    private int cur1, cur2, lastEdited;
    private String[] currencyArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setToolbarEnabled(true);

        currencyArray = getResources().getStringArray(R.array.currencies);

        currency1 = (EditText) findViewById(R.id.currency1);
        currency2 = (EditText) findViewById(R.id.currency2);

        tilCur1 = (TextInputLayout) findViewById(R.id.tilCur1);
        tilCur2 = (TextInputLayout) findViewById(R.id.tilCur2);

        Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
        Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.currencies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        lastEdited = 1;
        currency1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                lastEdited = 1;
            }
        });
        currency2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                lastEdited = 2;
            }
        });

        spinner1.setAdapter(adapter);
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cur1 = position;
                tilCur1.setHint(currencyArray[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner2.setAdapter(adapter);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cur2 = position;
                tilCur2.setHint(currencyArray[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        setFabEnabled(true);
        setFabDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check_white_48dp));
        setFabListener(new View.OnClickListener() {

            Double inputValue = Double.valueOf("0");

            @Override
            public void onClick(View v) {
                currency1.setEnabled(false);
                currency2.setEnabled(false);
                try {
                    if (lastEdited == 1) {
                        inputValue = Double.valueOf(currency1.getText().toString());
                    } else {
                        inputValue = Double.valueOf(currency2.getText().toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (inputValue.equals(Double.valueOf("0"))) {
                    Snackbar.make(v, R.string.empty_value, Snackbar.LENGTH_LONG).show();
                    currency1.setEnabled(true);
                    currency2.setEnabled(true);
                } else if (cur1 == cur2) {
                    Snackbar.make(v, R.string.sam_cur, Snackbar.LENGTH_LONG).show();
                    currency1.setEnabled(true);
                    currency2.setEnabled(true);
                } else {
                    Action<Double> doCalculate = new Action<Double>() {
                        @NonNull
                        @Override
                        public String id() {
                            return "calculate";
                        }

                        @Nullable
                        @Override
                        protected Double run() throws InterruptedException {
                            if (lastEdited == 1) {
                                return calculate(currencyArray[cur1], currencyArray[cur2], inputValue);
                            } else {
                                return calculate(currencyArray[cur2], currencyArray[cur1], inputValue);
                            }
                        }

                        @Override
                        protected void done(@Nullable Double result) {
                            if (result != null) {
                                if (lastEdited == 1) {
                                    currency2.setText(String.valueOf(result));
                                } else {
                                    currency1.setText(String.valueOf(result));
                                }
                            }
                            currency1.setEnabled(true);
                            currency2.setEnabled(true);
                        }
                    };
                    doCalculate.execute();
                }
            }
        });
    }

    private double calculate(String currency, String desiredCurrency, double value) {
        double returnValue = Double.valueOf("0");
        try {
            JSONObject jsonObject = Bridge.get("http://api.fixer.io/latest?base=%s&symbols=%s", currency, desiredCurrency).asJsonObject();
            if (jsonObject != null) {
                jsonObject = jsonObject.getJSONObject("rates");
                returnValue = jsonObject.getDouble(desiredCurrency) * value;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }
}
