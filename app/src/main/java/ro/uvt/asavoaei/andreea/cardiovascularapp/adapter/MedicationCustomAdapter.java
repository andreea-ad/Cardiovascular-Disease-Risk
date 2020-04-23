package ro.uvt.asavoaei.andreea.cardiovascularapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ro.uvt.asavoaei.andreea.cardiovascularapp.R;

public class MedicationCustomAdapter extends RecyclerView.Adapter<MedicationCustomAdapter.MedicationViewHolder> {
    private List<String> medicationList;

    public MedicationCustomAdapter(List<String> medicationList) {
        this.medicationList = medicationList;
    }

    public void setMedicationList(List<String> medicationList) {
        this.medicationList = medicationList;
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.medication_item_recyclerview, viewGroup, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder medicationViewHolder, int i) {
        String currentDisease = medicationList.get(i);
        medicationViewHolder.medicationName.setText(currentDisease);

    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    static class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView medicationName;

        MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            medicationName = itemView.findViewById(R.id.itemNameTv);
        }

    }


}
