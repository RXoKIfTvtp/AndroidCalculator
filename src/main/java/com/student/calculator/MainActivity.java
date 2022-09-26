package com.student.calculator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class MainActivity extends AppCompatActivity {

	// Constants for saving and restoring screen and memory contents via Bundles
	private final static String SCREEN = "SCREEN";
	private final static String MEMORY = "MEMORY";

	// Regex patterns for valid numbers
	private final static String NUMBER_A = "^-?[0-9]+\\.?[0-9]*$";
	private final static String NUMBER_B = "^-?[0-9]*\\.[0-9]+$";

	// A handle to an EditText which is used as the screen
	private EditText tv = null;

	// The number stored in memory by the calculator
	private double memory = 0.0;

	/**
	 * A wrapper method to shorten the decimal separator
	 * @return The local decimal separator
	 */
	private static char getDecimalSeparator() {
		return DecimalFormatSymbols.getInstance().getDecimalSeparator();
	}

	/**
	 * A wrapper method to shorten the grouping separator
	 * @return The local grouping separator
	 */
	private static char getGroupingSeparator() {
		return DecimalFormatSymbols.getInstance().getGroupingSeparator();
	}

	/**
	 * Formats a double as a string with consideration for local
	 * @param v The double to format
	 * @return a formatted string for the given double
	 */
	private static String format(double v) {
		DecimalFormatSymbols s = new DecimalFormatSymbols();
		s.setDecimalSeparator(getDecimalSeparator());
		s.setGroupingSeparator(getGroupingSeparator());
		String fs = "###0.###############";
		DecimalFormat f = new DecimalFormat(fs, s);
		while (fs.endsWith("#")) {
			f = new DecimalFormat(fs, s);
			String t = f.format(v);
			if (!overflow(t)) {
				return t;
			}
			fs = fs.substring(0, fs.length() - 1);
		}
		return f.format(v);
	}

	/**
	 * Splits a string at the binary operator of an expression
	 * @param exp The expression to evaluate
	 * @return a String array with two elements or null if it is not a valid expression
	 */
	private static String[] split(String exp) {
		if (exp.contains("+") || exp.contains("*") || exp.contains("/") || exp.contains("^")) {
			String[] t = exp.split("[+*/^]");
			if (t.length == 2) {
				return t;
			}
		} else if (exp.contains("-")) {
			int n = exp.length() - exp.replace("-", "").length();
			if (n == 1) {
				return exp.split("-");
			} else if (n == 2 || n == 3) {
				int i, c = 0;
				for (i = 0; i < exp.length(); i++) {
					if (exp.charAt(i) == '-') {
						if (c++ == 1) {
							return new String[]{
									exp.substring(0, i),
									exp.substring(i + 1)
							};
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Determines if a String is a valid base 10 number.
	 * This method does not support other bases.
	 * @param v The String to evaluate
	 * @return true if the String is a valid base 10 number, otherwise false
	 */
	private static boolean isNumber(String v) {
		if (v == null || v.equals("")) {
			return false;
		}
		String t = v.replace(getGroupingSeparator() + "", "");
		t = t.replace(getDecimalSeparator() + "", ".");
		return t.matches(NUMBER_A) || t.matches(NUMBER_B);
	}

	/**
	 * Determines if a String is an evaluable expression
	 * @return true if the String is a valid expression, otherwise false
	 */
	private static boolean isExpression(String v) {
		String[] tokens = split(v);
		if (tokens != null && tokens.length == 2) {
			int i;
			for (i = 0; i < tokens.length; i++) {
				if (!isNumber(tokens[i])) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Returns the contents of the screen. This method will never return a null.
	 * @return The screen contents as a String
	 */
	private String getScreen() {
		if (tv == null) {
			return "";
		}
		Editable e = tv.getText();
		if (e == null) {
			return "";
		}
		return e.toString();
	}

	/**
	 * Sets the contents of the screen to the given string.
	 * If the string contains a valued number or expression it is checked for an overflow
	 * @param v The string to display on the screen
	 */
	private void setScreen(String v) {
		if (tv != null) {
			if (overflow(v)) {
				setScreen(string(R.string.error3));
				toast(string(R.string.error3msg));
			} else {
				tv.setText(v);
			}
		}
	}

	/**
	 * {@link #format(double)} Formats} a double before {@link #setScreen(String)} displaying} it on the screen
	 * @param v The double to display on the screen
	 * @see #format(double)
	 * @see #setScreen(String)
	 */
	private void setScreen(double v) {
		setScreen(format(v));
	}

	/**
	 * Creates and displays a toast message with the provided String as the message
	 * @param v The message to display
	 */
	private void toast(String v) {
		Toast.makeText(getApplicationContext(), v, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Retrieves a string from the strings.xml file.
	 * @param stringId The integer ID of the String retrieved using R.string.some_string
	 * @return A string with the given id or null if no string was found
	 */
	private String string(int stringId) {
		try {
			return getResources().getString(stringId);
		} catch (Resources.NotFoundException e) {
			return null;
		}
	}

	/**
	 * Determines if the provided number or expression overflows the maximum allowed number
	 * @param v The expression or number to evaluate
	 * @return true if it is a valid number or expression and overflows the maximum value, otherwise false
	 */
	private static boolean overflow(String v) {
		if (isExpression(v)) {
			String[] tokens = split(v);
			if (tokens != null) {
				int i;
				for (i = 0; i < tokens.length; i++) {
					tokens[i] = tokens[i].replaceAll("[^0-9]+", "");
					if (tokens[i].length() > 15) {
						return true;
					}
				}
			}
		} else if (isNumber(v)) {
			String t = v.replaceAll("[^0-9]+", "");
			if (t.length() > 15) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determines if the provided number or expression overflows the maximum allowed number
	 * @param v The expression or number to evaluate
	 * @return true if it is a valid number or expression and overflows the maximum value, otherwise false
	 * @see #overflow(String)
	 */
	private boolean overflow(double v) {
		return overflow(format(v));
	}

	/**
	 * Attempts to solve an expression currently on screen
	 * @param suppressErrors Does not display errors if true. If false error messages are displayed.
	 * @return true if the expression on screen is a valid number or has successfully been simplified.
	 */
	private boolean solve(boolean suppressErrors) {
		String s = getScreen();
		if (s.startsWith(getResources().getString(R.string.error))) {
			return false;
		}
		if (isNumber(s)) {
			return true;
		}
		if (isExpression(s)) {
			//Solvable expression
			String[] tokens = split(s);
			if (tokens != null) {
				String op = s.substring(tokens[0].length(), s.length() - tokens[1].length());
				double a, b;
				try {
					a = Double.parseDouble(tokens[0]);
					b = Double.parseDouble(tokens[1]);
				} catch (NumberFormatException e) {
					return false;// Swallow
				}
				double ret = 0.0d;
				if (op.equals("*")) {
					ret = a * b;
				} else if (op.equals("/")) {
					if (b == 0) {
						setScreen(string(R.string.error2));
						toast(string(R.string.error2msg));
						return false;
					} else {
						ret = a / b;
					}
				} else if (op.equals("-")) {
					ret = a - b;
				} else if (op.equals("+")) {
					ret = a + b;
				} else if (op.equals("^")) {
					ret = Math.pow(a, b);
				}
				if (overflow(ret)) {
					setScreen(string(R.string.error3));
					toast(string(R.string.error3msg));
				} else {
					setScreen(ret);
					scrollToEnd();
				}
				return true;
			}
		} else {
			// This happens if an operator button is pressed before a second operand is entered
			if (!suppressErrors) {
				setScreen(string(R.string.error1));
				toast(string(R.string.error1msg));
			}
		}
		return false;
	}

	/**
	 * Attempts to solve the equation on screen. If the equation on screen is not solvable
	 * the screen will display an error message
	 * @return true if the expression on screen is simplifiable otherwise false
	 */
	private boolean solve() {
		return solve(false);
	}

	/**
	 * Attempts to solve the equation on screen. If the equation on screen is not solvable
	 * the screen is not changed
	 * @return true if the expression on screen is simplifiable otherwise false
	 */
	private boolean trySolve() {
		return solve(true);
	}

	/**
	 * Scroll the screen to the right so that the right most entered character
	 */
	private void scrollToEnd() {
		tv.setSelection(tv.getText().length() - 1);
	}

	/**
	 * Converts the number on the screen to a Double or null if it can't be converted
	 * @return Double if screen contains a valid Double otherwise null
	 */
	private Double getNumber() {
		String t = getScreen();
		if (isNumber(t)) {
			try {
				return Double.parseDouble(t);
			} catch (NumberFormatException e) {
				// Swallow
			}
		}
		return null;
	}

	/**
	 * Determines if the screen is displaying an error message.
	 * This method is used to block the functionality of all keys except
	 * the clear button if an error occurs
	 * @return true if the screen contains the error string, otherwise false
	 */
	private boolean error() {
		return getScreen().toLowerCase().contains(string(R.string.error).toLowerCase());
	}

	/**
	 * Create activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setContentView(R.layout.activity_main_landscape);
		} else {
			setContentView(R.layout.activity_main_portrait);
		}
		tv = findViewById(R.id.editText);
		setScreen(0);
	}

	// Helper methods for onClick handlers

	/**
	 * Appended a string to the right side of the screen
	 * @param v The String to be appended to the screen
	 */
	private void calc_append(String v) {
		if (!error()) {
			if (v == null) {
				v = "";
			}
			String t = getScreen();
			if (v.equals("0") && (t.equals("-0") || t.equals("0"))) {
				return;
			}
			if (isNumber(v)) {
				if (t.equals("0") || t.endsWith("+0") || t.endsWith("-0") || t.endsWith("*0") || t.endsWith("/0") || t.endsWith("^0")) {
					t = t.substring(0, t.length() - 1);
				}
			}
			String tmp = t + v;
			if (overflow(tmp)) {
				setScreen(string(R.string.error3));
				toast(string(R.string.error3msg));
			} else {
				setScreen(tmp);
				scrollToEnd();
			}
		}
	}

	/**
	 * Attempts to simplify an existing expression before appending an operator to the left of the screen
	 * @param v The String to append
	 */
	private void calc_append_op(String v) {
		if (!error()) {
			trySolve();
			calc_append(v);
		}
	}

	/**
	 * Applies an addition or subtraction operator to the background memory value
	 */
	private void mem_op(Double v) {
		if (!error()) {
			solve();
			if (v != null) {
				memory += v;
			}
			setScreen(memory);
		}
	}

	// onClick handlers

	/**
	 * Clears the screen
	 */
	public void calc_clr(View view) {
		setScreen("0");
	}

	/**
	 * Remove right most character from calc
	 */
	public void calc_bck(View view) {
		if (!error()) {
			String t = getScreen();
			if (!t.contains(getResources().getString(R.string.error))) {
				t = t.substring(0, t.length() - 1);
				if (t.equals("")) {
					t = "0";
				}
				tv.setText(t);
			}
		}
	}

	/**
	 * A plain button press which appends a number to the right side of the screen
	 */
	public void calc_plain(View view) {
		if (!error()) {
			String btn = ((Button) view).getText().toString();
			calc_append(btn);
		}
	}

	/**
	 * A plain button press which appends a number to the screen
	 */
	public void calc_dec(View view) {
		if (!error()) {
			String s = getScreen();
			if (isExpression(s)) {
				String[] tokens = split(s);
				if (tokens != null && tokens[tokens.length - 1].contains(".")) {
					return;
				}
				if (!s.endsWith(".")) {
					calc_plain(view);
				}
			} else if (isNumber(s)) {
				if (!s.contains(".")) {
					calc_plain(view);
				} else if (s.equals("") || s.endsWith("+") || s.endsWith("-") || s.endsWith("*") || s.endsWith("/") || s.endsWith("^")) {
					// This leading zero is important
					// Without it, the number wont match the NUMBER pattern
					//calc_append("0");
					calc_plain(view);
				}
			}
		}
	}

	/**
	 * Appends an addition operator
	 */
	public void calc_add(View view) {
		if (!error()) {
			calc_append_op("+");
		}
	}

	/**
	 * Appends a subtraction operator
	 */
	public void calc_sub(View view) {
		if (!error()) {
			calc_append_op("-");
		}
	}

	/**
	 * Appends a multiplication operator
	 */
	public void calc_mul(View view) {
		if (!error()) {
			calc_append_op("*");
		}
	}

	/**
	 * Appends a division operator
	 */
	public void calc_div(View view) {
		if (!error()) {
			calc_append_op("/");
		}
	}

	/**
	 * Appends a power operator
	 */
	public void calc_pow(View view) {
		if (!error()) {
			calc_append_op("^");
		}
	}

	/**
	 * Attempts to solve an expression on screen if one exists
	 */
	public void calc_eql(View view) {
		if (!error()) {
			solve();
		}
	}

	/**
	 * Stores the value on screen to a background location
	 * If the screen contains a valid expression it is first simplified
	 * If the screen contains an invalid expression the background memory is not changed
	 */
	public void calc_ms(View view) {
		if (!error()) {
			solve();
			Double t = getNumber();
			if (t != null) {
				memory = t;
				setScreen(memory);
			}
		}
	}

	/**
	 * Clears the background memory
	 */
	public void calc_mc(View view) {
		if (!error()) {
			memory = 0;
			setScreen("0");
		}
	}

	/**
	 * Sets the screen to the current value of the background memory
	 */
	public void calc_mr(View view) {
		if (!error()) {
			setScreen(memory);
		}
	}

	/**
	 * Memory Plus
	 * Adds the current screen value to the background memory and sets the screen to the new background memory value
	 */
	public void calc_mp(View view) {
		if (!error()) {
			Double t = getNumber();
			if (t != null) {
				mem_op(t);
				setScreen(memory);
			}
		}
	}

	/**
	 * Memory Minus
	 * Subtracts the current screen value to the background memory and sets the screen to the new background memory value
	 */
	public void calc_mm(View view) {
		if (!error()) {
			Double t = getNumber();
			if (t != null) {
				mem_op(-t);
				setScreen(memory);
			}
		}
	}

	/**
	 * Calculates the decimal inverse of the current screen value
	 */
	public void calc_inv(View view) {
		if (!error()) {
			solve();
			Double t = getNumber();
			if (t != null) {
				setScreen(1 / t);
			}
		}
	}

	/**
	 * Divides the current screen value by 100 to get the decimal percent value
	 */
	public void calc_per(View view) {
		if (!error()) {
			solve();
			Double t = getNumber();
			if (t != null) {
				setScreen(t / 100);
			}
		}
	}

	/**
	 * Square roots the current screen value
	 */
	public void calc_sqt(View view) {
		if (!error()) {
			solve();
			Double t = getNumber();
			if (t != null) {
				setScreen(Math.sqrt(t));
			}
		}
	}

	/**
	 * Inverts the current screen values sign
	 */
	public void calc_sgn(View view) {
		if (!error()) {
			solve();
			Double t = getNumber();
			if (t != null) {
				setScreen(-t);
			}
		}
	}

	// Handle activity suspend

	/**
	 * Saves the current state of the calculator
	 */
	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SCREEN, getScreen());
		outState.putDouble(MEMORY, memory);
	}

	/**
	 * Restores the current state of the calculator
	 */
	@Override
	public void onRestoreInstanceState(@Nullable Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null) {
			tv.setText(savedInstanceState.getString(SCREEN, ""));
			memory = savedInstanceState.getDouble(MEMORY, 0);
		} else {
			tv.setText("");
			memory = 0;
		}
	}
}
