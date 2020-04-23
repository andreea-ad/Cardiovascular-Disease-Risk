package ro.uvt.asavoaei.andreea.cardiovasculardiseaserisk.model;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Medication {
    private String ATC;
    private String medicationName;
    private String medicationClass;

    public Medication() {

    }

    public Medication(String ATC, String medicationName, String medicationClass) {
        this.ATC = ATC;
        this.medicationName = medicationName;
        this.medicationClass = medicationClass;
    }

    public String getATC() {
        return ATC;
    }

    public void setATC(String ATC) {
        this.ATC = ATC;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getMedicationClass() {
        return medicationClass;
    }

    public void setMedicationClass(String medicationClass) {
        this.medicationClass = medicationClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Medication that = (Medication) o;
        return ATC.equals(that.ATC) &&
                medicationName.equals(that.medicationName) &&
                medicationClass.equals(that.medicationClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ATC, medicationName, medicationClass);
    }

    @NonNull
    @Override
    public String toString() {
        return getATC() + " - " + getMedicationClass() + " - " + getMedicationName();
    }
}
