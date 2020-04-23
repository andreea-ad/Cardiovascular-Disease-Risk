package ro.uvt.asavoaei.andreea.cardiovascularapp.model;

import androidx.annotation.NonNull;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Objects;

public class UserProfile {
    private String emailAddress;
    private String firstname;
    private String lastname;
    private String dateOfBirth;
    private String location;
    private String gender;
    private int height;
    private boolean isSmoker;
    private boolean isPregnant;
    private HashMap<String, String> diseases;

    public UserProfile() {
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dob) {
        dob = dob.replaceAll("/", "-");
        this.dateOfBirth = dob;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String city) {
        city = Normalizer.normalize(city, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toUpperCase();
        this.location = city;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isSmoker() {
        return isSmoker;
    }

    public void setSmoker(boolean smoker) {
        isSmoker = smoker;
    }

    public boolean isPregnant() {
        return isPregnant;
    }

    public void setPregnant(boolean pregnant) {
        if (getGender().equals("Femeie")) {
            isPregnant = pregnant;
        } else {
            isPregnant = false;
        }
    }

    public HashMap<String, String> getDiseases() {
        return diseases;
    }

    public void setDiseases(HashMap<String, String> diseases) {
        this.diseases = diseases;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProfile that = (UserProfile) o;
        return height == that.height &&
                isSmoker == that.isSmoker &&
                isPregnant == that.isPregnant &&
                emailAddress.equals(that.emailAddress) &&
                firstname.equals(that.firstname) &&
                lastname.equals(that.lastname) &&
                dateOfBirth.equals(that.dateOfBirth) &&
                location.equals(that.location) &&
                gender.equals(that.gender) &&
                Objects.equals(diseases, that.diseases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(emailAddress, firstname, lastname, dateOfBirth, location, gender, height, isSmoker, isPregnant, diseases);
    }

    @NonNull
    @Override
    public String toString() {
        return "Prenume: " + firstname + " Nume: " + lastname;
    }
}
