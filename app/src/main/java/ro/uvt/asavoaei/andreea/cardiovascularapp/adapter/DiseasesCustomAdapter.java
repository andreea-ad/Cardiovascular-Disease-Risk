package ro.uvt.asavoaei.andreea.cardiovascularapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ro.uvt.asavoaei.andreea.cardiovascularapp.R;

public class DiseasesCustomAdapter extends RecyclerView.Adapter<DiseasesCustomAdapter.DiseasesViewHolder> {
    private List<String> diseasesList;

    public DiseasesCustomAdapter(List<String> diseasesList) {
        this.diseasesList = diseasesList;
    }

    public List<String> getDiseasesList() {
        return diseasesList;
    }

    public void setDiseasesList(List<String> diseasesList) {
        this.diseasesList = diseasesList;
    }

    @NonNull
    @Override
    public DiseasesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.disease_item_recyclerview, viewGroup, false);
        return new DiseasesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiseasesViewHolder diseasesViewHolder, int i) {
        String currentDisease = diseasesList.get(i);
        diseasesViewHolder.diseaseName.setText(currentDisease);
    }

    @Override
    public int getItemCount() {
        return diseasesList.size();
    }

    static class DiseasesViewHolder extends RecyclerView.ViewHolder {
        TextView diseaseName;

        DiseasesViewHolder(@NonNull View itemView) {
            super(itemView);
            diseaseName = itemView.findViewById(R.id.itemNameTv);
        }

    }


}