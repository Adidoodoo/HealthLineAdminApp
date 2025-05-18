package com.example.healthlineadminapp;

public class Department {
    private String departmentName;
    private String doctorName;
    private int currentQueue;
    private boolean isOpen;

    public Department() {
    }

    public Department(String departmentName, String doctorName, int currentQueue, boolean isOpen) {
        this.departmentName = departmentName;
        this.doctorName = doctorName;
        this.currentQueue = currentQueue;
        this.isOpen = isOpen;
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

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }
}
