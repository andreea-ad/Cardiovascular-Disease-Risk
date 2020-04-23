package ro.uvt.asavoaei.andreea.cardiovasculardiseaserisk.utils;

import com.github.mikephil.charting.formatter.ValueFormatter;

public class FloatValueFormatter extends ValueFormatter {

    @Override
    public String getFormattedValue(float value) {
        return "" + (int) value;
    }
}
