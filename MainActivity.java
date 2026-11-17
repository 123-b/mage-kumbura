package lk.vimansha.magekumbura;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.content.SharedPreferences;
import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends Activity {
    private SharedPreferences prefs;
    private LinearLayout list;
    private TextView totalExpense, totalIncome, profit;
    private double expenses = 0, income = 0;
    private final NumberFormat money = NumberFormat.getNumberInstance(new Locale("en", "LK"));

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("mage_kumbura", MODE_PRIVATE);
        expenses = Double.longBitsToDouble(prefs.getLong("expenses", Double.doubleToLongBits(0)));
        income = Double.longBitsToDouble(prefs.getLong("income", Double.doubleToLongBits(0)));
        buildScreen();
        refresh();
    }

    private TextView text(String s, int size, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(size);
        if (bold) t.setTypeface(null, Typeface.BOLD);
        t.setPadding(0, 8, 0, 8);
        return t;
    }
    private EditText field(String hint, int type) {
        EditText e = new EditText(this);
        e.setSingleLine(true); e.setHint(hint); e.setInputType(type);
        return e;
    }
    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(24, 24, 24, 32);
        page.setBackgroundColor(0xFFF4F8F1);
        scroll.addView(page);

        TextView title = text("🌾 Mage Kumbura", 27, true);
        title.setTextColor(0xFF245C35); page.addView(title);
        page.addView(text("වී වගාවේ ආදායම් හා වියදම් සටහන්", 16, false));

        LinearLayout summary = new LinearLayout(this);
        summary.setOrientation(LinearLayout.VERTICAL);
        summary.setPadding(16, 12, 16, 12);
        summary.setBackgroundColor(0xFFFFFFFF);
        totalExpense = text("", 18, true);
        totalIncome = text("", 18, true);
        profit = text("", 20, true);
        summary.addView(totalExpense); summary.addView(totalIncome); summary.addView(profit);
        page.addView(summary);

        page.addView(text("නව වියදමක් ඇතුළත් කරන්න", 20, true));
        EditText expenseName = field("වියදම (උදා: පොහොර, කුලී)", 1);
        EditText expenseAmount = field("මුදල (රු.)", 2);
        page.addView(expenseName); page.addView(expenseAmount);
        Button addExpense = new Button(this); addExpense.setText("වියදම සුරකින්න");
        page.addView(addExpense);
        addExpense.setOnClickListener(v -> {
            String name = expenseName.getText().toString().trim();
            double amount = number(expenseAmount.getText().toString());
            if (name.isEmpty() || amount <= 0) { toast("වියදම සහ නිවැරදි මුදල ඇතුළත් කරන්න"); return; }
            expenses += amount; save();
            addEntry("වියදම: " + name + " — රු. " + money.format(amount));
            expenseName.setText(""); expenseAmount.setText(""); refresh();
        });

        page.addView(text("නව ආදායමක් ඇතුළත් කරන්න", 20, true));
        EditText incomeName = field("ආදායම (උදා: වී අස්වැන්න)", 1);
        EditText incomeAmount = field("මුදල (රු.)", 2);
        page.addView(incomeName); page.addView(incomeAmount);
        Button addIncome = new Button(this); addIncome.setText("ආදායම සුරකින්න");
        page.addView(addIncome);
        addIncome.setOnClickListener(v -> {
            String name = incomeName.getText().toString().trim();
            double amount = number(incomeAmount.getText().toString());
            if (name.isEmpty() || amount <= 0) { toast("ආදායම සහ නිවැරදි මුදල ඇතුළත් කරන්න"); return; }
            income += amount; save();
            addEntry("ආදායම: " + name + " — රු. " + money.format(amount));
            incomeName.setText(""); incomeAmount.setText(""); refresh();
        });

        page.addView(text("සටහන්", 20, true));
        list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL);
        page.addView(list);
        Button reset = new Button(this); reset.setText("සියලු එකතුව ශුන්‍ය කරන්න");
        page.addView(reset);
        reset.setOnClickListener(v -> new android.app.AlertDialog.Builder(this)
            .setTitle("තහවුරු කරන්න")
            .setMessage("සියලු ආදායම් සහ වියදම් එකතු මකා දමන්නද?")
            .setNegativeButton("නැහැ", null)
            .setPositiveButton("ඔව්", (d,w) -> {
                expenses = 0; income = 0; prefs.edit().clear().apply();
                list.removeAllViews(); refresh(); toast("දත්ත මකා දැමුවා");
            }).show());
        setContentView(scroll);
    }
    private double number(String s) {
        try { return Double.parseDouble(s.trim().replace(",", "")); }
        catch (Exception e) { return 0; }
    }
    private void save() {
        prefs.edit().putLong("expenses", Double.doubleToLongBits(expenses))
            .putLong("income", Double.doubleToLongBits(income)).apply();
    }
    private void refresh() {
        totalExpense.setText("මුළු වියදම: රු. " + money.format(expenses));
        totalIncome.setText("මුළු ආදායම: රු. " + money.format(income));
        double p = income - expenses;
        profit.setText((p >= 0 ? "ලාභය: රු. " : "අලාභය: රු. ") + money.format(Math.abs(p)));
        profit.setTextColor(p >= 0 ? 0xFF20723A : 0xFFB3261E);
    }
    private void addEntry(String s) {
        TextView item = text("• " + s, 15, false);
        item.setBackgroundColor(0xFFE8F0E5);
        item.setPadding(12, 12, 12, 12);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 5, 0, 5); list.addView(item, lp);
    }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}
