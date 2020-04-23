package ro.uvt.asavoaei.andreea.cardiovascularapp.model;

import java.util.Objects;

public class WeatherRecord {
    private int id;
    private double latitude;
    private double longitude;
    private String city;
    private int humidity;
    private String nebulosity;
    private double temperature;
    private double windSpeed;
    private double pressure;
    private String recordingDate;
    private String recordingHour;

    public WeatherRecord() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public String getNebulosity() {
        return nebulosity;
    }

    public void setNebulosity(String nebulosity) {
        this.nebulosity = nebulosity;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
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

    public String toString() {
        return ">> Weather record:\n" + id + "  " + latitude + "  " + longitude + "  " + city + "  " + humidity + "  " + nebulosity + "  " + temperature + "  " + windSpeed + "  " + pressure + "  " + recordingDate + "  " + recordingHour;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, latitude, longitude, city, humidity, nebulosity, temperature, windSpeed, pressure, recordingDate, recordingHour);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeatherRecord that = (WeatherRecord) o;
        return id == that.id &&
                Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0 &&
                humidity == that.humidity &&
                Double.compare(that.temperature, temperature) == 0 &&
                Double.compare(that.windSpeed, windSpeed) == 0 &&
                Double.compare(that.pressure, pressure) == 0 &&
                city.equals(that.city) &&
                nebulosity.equals(that.nebulosity) &&
                recordingDate.equals(that.recordingDate) &&
                recordingHour.equals(that.recordingHour);
    }
}
