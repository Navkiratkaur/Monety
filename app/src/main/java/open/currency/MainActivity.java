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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.desmond.asyncmanager.AsyncManager;
import com.desmond.asyncmanager.TaskRunnable;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    EditText currency1, currency2;
    int cur1, cur2;
    String[] curarray;
    Spinner spinner1, spinner2;
    ArrayAdapter<CharSequence> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        curarray = getResources().getStringArray(R.array.currencies);

        cur1 = 0;
        cur2 = 0;

        currency1 = (EditText) findViewById(R.id.currency1);
        currency2 = (EditText) findViewById(R.id.currency2);

        spinner1 = (Spinner) findViewById(R.id.spinner1);
        spinner2 = (Spinner) findViewById(R.id.spinner2);

        adapter = ArrayAdapter.createFromResource(this, R.array.currencies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner1.setAdapter(adapter);
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cur1 = position;
                currency1.setHint(curarray[position]);
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
                currency2.setHint(curarray[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        FloatingActionButton fabCalculate = (FloatingActionButton) findViewById(R.id.fab_calculate);
        fabCalculate.setOnClickListener(new View.OnClickListener() {

            Double c1value;

            @Override
            public void onClick(View v) {
                try {
                    c1value = Double.valueOf(currency1.getText().toString());
                } catch (Exception ignored) {
                    c1value = Double.valueOf("0");
                }
                if (c1value.equals(Double.valueOf("0"))) {
                    new AlertDialog.Builder(MainActivity.this).setMessage("Empty value!").show();
                } else if (curarray[cur1].equals(curarray[cur2])) {
                    new AlertDialog.Builder(MainActivity.this).setMessage("Same currency!").show();
                } else {
                    AsyncManager.runBackgroundTask(new TaskRunnable<Void, Double, Void>() {
                        @Override
                        public Double doLongOperation(Void aVoid) throws InterruptedException {
                            return calculate(curarray[cur1], curarray[cur2], c1value);
                        }

                        @Override
                        public void callback(Double aDouble) {
                            currency2.setText(aDouble.toString());
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public double calculate(String currency, String desiredCurrency, double value) {
        double returnValue = Double.valueOf("0");
        try {
            URL url = new URL("http://api.fixer.io/latest?base=" + currency);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            urlConnection.disconnect();
            returnValue = (new JSONObject(sb.toString()).getJSONObject("rates").getDouble(desiredCurrency)) * value;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }
}
