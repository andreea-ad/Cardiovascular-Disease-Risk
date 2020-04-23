package ro.uvt.asavoaei.andreea.cardiovascularapp.model;


import androidx.annotation.NonNull;

import java.util.Objects;

public class Disease {
    private String diseaseName;

    public Disease() {
    }

    public Disease(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public String getName() {
        return diseaseName;
    }

    public void setName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Disease disease = (Disease) o;
        return Objects.equals(diseaseName, disease.diseaseName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diseaseName);
    }

    @NonNull
    @Override
    public String toString() {
        return getName();
    }
}
