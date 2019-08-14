package com.example.android.swill;

/**
 * Created by DAKONY on 2/14/2019.
 */

public class FindPost
{
    public String time,date,profileImage,description,destination,dateOfTravel,fullname;

    public FindPost()
    {

    }
    public FindPost(String time, String date, String profileImage, String description, String destination, String dateOfTravel, String fullname)
    {
        this.time = time;
        this.date = date;
        this.profileImage = profileImage;
        this.description = description;
        this.destination = destination;
        this.dateOfTravel = dateOfTravel;
        this.fullname = fullname;
    }



    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDateOfTravel() {
        return dateOfTravel;
    }

    public void setDateOfTravel(String dateOfTravel) {
        this.dateOfTravel = dateOfTravel;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
}
