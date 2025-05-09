package com.example.healthlineadminapp;

public class Department {
    private String departmentName;
    private String doctorName;
    private int currentQueue;

    public Department() {
    }

    public Department(String departmentName, String doctorName, int currentQueue) {
        this.departmentName = departmentName;
        this.doctorName = doctorName;
        this.currentQueue = currentQueue;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public int getCurrentQueue() {
        return currentQueue;
    }

    public void setCurrentQueue(int currentQueue) {
        this.currentQueue = currentQueue;
    }
}
