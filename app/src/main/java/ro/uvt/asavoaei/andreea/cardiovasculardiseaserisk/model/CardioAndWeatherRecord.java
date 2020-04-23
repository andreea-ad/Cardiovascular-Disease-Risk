package ro.uvt.asavoaei.andreea.cardiovasculardiseaserisk.model;

import java.util.Objects;

public class CardioAndWeatherRecord {
    private int systolicBP;
    private int diastolicBP;
    private int pulse;
    private int cholesterol;
    private int BMI;
    private boolean pregnant;
    private boolean smoker;
    private double temperature;
    private int humidity;
    private double pressure;
    private String nebulosity;
    private String recordingDate;
    private String recordingHour;

    public CardioAndWeatherRecord() {
    }

    public CardioAndWeatherRecord(int systolicBP, int diastolicBP, int pulse, int cholesterol, int BMI, boolean pregnant, boolean smoker, double temperature, int humidity, double pressure, String nebulosity, String recordingDate, String recordingHour) {
        this.systolicBP = systolicBP;
        this.diastolicBP = diastolicBP;
        this.pulse = pulse;
        this.cholesterol = cholesterol;
        this.BMI = BMI;
        this.pregnant = pregnant;
        this.smoker = smoker;
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
        this.nebulosity = nebulosity;
        this.recordingDate = recordingDate;
        this.recordingHour = recordingHour;
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

    public int getCholesterol() {
        return cholesterol;
    }

    public void setCholesterol(int cholesterol) {
        this.cholesterol = cholesterol;
    }

    public int getBMI() {
        return BMI;
    }

    public void setBMI(int BMI) {
        this.BMI = BMI;
    }

    public boolean isPregnant() {
        return pregnant;
    }

    public void setPregnant(boolean pregnant) {
        this.pregnant = pregnant;
    }

    public boolean isSmoker() {
        return smoker;
    }

    public void setSmoker(boolean smoker) {
        this.smoker = smoker;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public String getNebulosity() {
        return nebulosity;
    }

    public void setNebulosity(String nebulosity) {
        this.nebulosity = nebulosity;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardioAndWeatherRecord that = (CardioAndWeatherRecord) o;
        return systolicBP == that.systolicBP &&
                diastolicBP == that.diastolicBP &&
                pulse == that.pulse &&
                cholesterol == that.cholesterol &&
                BMI == that.BMI &&
                pregnant == that.pregnant &&
                smoker == that.smoker &&
                Double.compare(that.temperature, temperature) == 0 &&
                humidity == that.humidity &&
                Double.compare(that.pressure, pressure) == 0 &&
                nebulosity.equals(that.nebulosity) &&
                recordingDate.equals(that.recordingDate) &&
                recordingHour.equals(that.recordingHour);
    }

    @Override
    public int hashCode() {
        return Objects.hash(systolicBP, diastolicBP, pulse, cholesterol, BMI, pregnant, smoker, temperature, humidity, pressure, nebulosity, recordingDate, recordingHour);
    }

    @Override
    public String toString() {
        return "CardioAndWeatherRecord{" +
                "systolicBP=" + systolicBP +
                ", diastolicBP=" + diastolicBP +
                ", pulse=" + pulse +
                ", cholesterol=" + cholesterol +
                ", BMI=" + BMI +
                ", pregnant=" + pregnant +
                ", smoker=" + smoker +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", pressure=" + pressure +
                ", nebulosity='" + nebulosity + '\'' +
                ", recordingDate='" + recordingDate + '\'' +
                ", recordingHour='" + recordingHour + '\'' +
                '}';
    }
}
