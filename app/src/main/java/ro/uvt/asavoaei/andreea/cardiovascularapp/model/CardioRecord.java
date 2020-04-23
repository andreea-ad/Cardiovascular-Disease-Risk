package ro.uvt.asavoaei.andreea.cardiovascularapp.model;

import java.util.HashMap;
import java.util.Objects;

public class CardioRecord {

    private String emailAddress;
    private int systolicBP;
    private int diastolicBP;
    private int pulse;
    private boolean alcoholUse;
    private int cholesterol;
    private int weight;
    private String recordingDate;
    private String recordingHour;
    private HashMap<String, String> medication;

    public CardioRecord() {
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public int getSystolicBP() {
        return systolicBP;
    }

    public void setSystolicBP(int systolicBP) {
        this.systolicBP = systolicBP;
    }

    public int getDiastolicBP() {
        return diastolicBP;
    }

    public void setDiastolicBP(int diastolicBP) {
        this.diastolicBP = diastolicBP;
    }

    public int getPulse() {
        return pulse;
    }

    public void setPulse(int pulse) {
        this.pulse = pulse;
    }

    public boolean isAlcoholUse() {
        return alcoholUse;
    }

    public void setAlcoholUse(boolean alcoholUse) {
        this.alcoholUse = alcoholUse;
    }

    public int getCholesterol() {
        return cholesterol;
    }

    public void setCholesterol(int cholesterol) {
        this.cholesterol = cholesterol;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getRecordingDate() {
        return recordingDate;
    }

    public void setRecordingDate(String recordingDate) {
        this.recordingDate = recordingDate;
    }

    public String getRecordingHour() {
        return recordingHour;
    }

    public void setRecordingHour(String recordingHour) {
        this.recordingHour = recordingHour;
    }

    public HashMap<String, String> getMedication() {
        return medication;
    }

    public void setMedication(HashMap<String, String> medication) {
        this.medication = medication;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardioRecord that = (CardioRecord) o;
        return systolicBP == that.systolicBP &&
                diastolicBP == that.diastolicBP &&
                pulse == that.pulse &&
                alcoholUse == that.alcoholUse &&
                cholesterol == that.cholesterol &&
                weight == that.weight &&
                emailAddress.equals(that.emailAddress) &&
                recordingDate.equals(that.recordingDate) &&
                recordingHour.equals(that.recordingHour) &&
                Objects.equals(medication, that.medication);
    }

    @Override
    public int hashCode() {
        return Objects.hash(emailAddress, systolicBP, diastolicBP, pulse, alcoholUse, cholesterol, weight, recordingDate, recordingHour, medication);
    }

    @Override
    public String toString() {
        return "CardioRecord{" +
                "emailAddress='" + emailAddress + '\'' +
                ", systolicBP=" + systolicBP +
                ", diastolicBP=" + diastolicBP +
                ", pulse=" + pulse +
                ", alcoholUse=" + alcoholUse +
                ", cholesterol=" + cholesterol +
                ", weight=" + weight +
                ", recordingDate='" + recordingDate + '\'' +
                ", recordingHour='" + recordingHour + '\'' +
                ", medication=" + medication +
                '}';
    }
}
