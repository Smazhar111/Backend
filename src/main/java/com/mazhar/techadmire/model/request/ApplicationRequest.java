package com.mazhar.techadmire.model.request;

import jakarta.validation.constraints.NotBlank;

public class ApplicationRequest {

    @NotBlank
    private String studentName;

    @NotBlank
    private String universityName;

    @NotBlank
    private String universityCourse;

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getUniversityName() {
        return universityName;
    }

    public void setUniversityName(String universityName) {
        this.universityName = universityName;
    }

    public String getUniversityCourse() {
        return universityCourse;
    }

    public void setUniversityCourse(String universityCourse) {
        this.universityCourse = universityCourse;
    }
}
