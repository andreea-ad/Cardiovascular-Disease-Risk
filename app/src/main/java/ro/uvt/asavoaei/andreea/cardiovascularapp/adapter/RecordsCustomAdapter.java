package ro.uvt.asavoaei.andreea.cardiovascularapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ro.uvt.asavoaei.andreea.cardiovascularapp.R;
import ro.uvt.asavoaei.andreea.cardiovascularapp.model.CardioAndWeatherRecord;

public class RecordsCustomAdapter extends RecyclerView.Adapter<RecordsCustomAdapter.RecordsViewHolder> {
    private List<CardioAndWeatherRecord> cardioAndWeatherRecordsList;

    public RecordsCustomAdapter(List<CardioAndWeatherRecord> cardioAndWeatherRecordsList) {
        this.cardioAndWeatherRecordsList = cardioAndWeatherRecordsList;
    }

    /**
     * Create view holder and inflate to it the layout for the recycler view
     * @param parent
     * @param viewType
     * @return RecordsViewHolder
     */
    @NonNull
    @Override
    public RecordsCustomAdapter.RecordsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardio_record_recyclerview, parent, false);
        return new RecordsCustomAdapter.RecordsViewHolder(view);
    }

    /**
     * Populate the view holder with medical and weather data
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull RecordsCustomAdapter.RecordsViewHolder holder, int position) {
        CardioAndWeatherRecord cardioAndWeatherRecord = cardioAndWeatherRecordsList.get(position);

        holder.recordingDate.setText(cardioAndWeatherRecord.getRecordingDate() + ", " + cardioAndWeatherRecord.getRecordingHour());

        String bloodPressureStr = cardioAndWeatherRecord.getSystolicBP() + "/" + cardioAndWeatherRecord.getDiastolicBP() + " mmHg";
        holder.bloodPressure.setText(bloodPressureStr);

        String pulseStr = cardioAndWeatherRecord.getPulse() + " bpm";
        holder.pulse.setText(pulseStr);

        String cholesterolStr = cardioAndWeatherRecord.getCholesterol() + " mg/dl";
        holder.cholesterol.setText(cholesterolStr);

        holder.bmi.setText(String.valueOf(cardioAndWeatherRecord.getBMI()));

        String temperatureStr = cardioAndWeatherRecord.getTemperature() + " °C";
        holder.temperature.setText(temperatureStr);

        String humidityStr = cardioAndWeatherRecord.getHumidity() + " %";
        holder.humidity.setText(humidityStr);

        String pressureStr = cardioAndWeatherRecord.getPressure() + " mb";
        holder.pressure.setText(pressureStr);

        holder.nebulosity.setText(cardioAndWeatherRecord.getNebulosity());

        holder.pregnant.setChecked(cardioAndWeatherRecord.isPregnant());
        holder.pregnant.setClickable(false);

        holder.smoker.setChecked(cardioAndWeatherRecord.isSmoker());
        holder.smoker.setClickable(false);

    }

    @Override
    public int getItemCount() {
        return cardioAndWeatherRecordsList.size();
    }

    /**
     * Create and initialize the view holder elements
     */
    public class RecordsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView recordingDate, bloodPressure, pulse, cholesterol, bmi, temperature, humidity, pressure, nebulosity;
        CheckBox pregnant, smoker;

        RecordsViewHolder(@NonNull View itemView) {
            super(itemView);
            recordingDate = itemView.findViewById(R.id.recordingDateTv);
            bloodPressure = itemView.findViewById(R.id.bloodPressureValueTv);
            pulse = itemView.findViewById(R.id.pulseValueTv);
            cholesterol = itemView.findViewById(R.id.cholesterolValueTv);
            bmi = itemView.findViewById(R.id.bmiValueTv);
            temperature = itemView.findViewById(R.id.temperatureValueTv);
            humidity = itemView.findViewById(R.id.humidityValueTv);
            pressure = itemView.findViewById(R.id.pressureValueTv);
            nebulosity = itemView.findViewById(R.id.nebulosityValueTv);
            pregnant = itemView.findViewById(R.id.pregnantCb);
            smoker = itemView.findViewById(R.id.smokerCb);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
