package com.mazhar.techadmire.service.impl;

import com.mazhar.techadmire.model.entity.Application;
import com.mazhar.techadmire.model.request.ApplicationRequest;
import com.mazhar.techadmire.repository.ApplicationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationServiceImpl {
    @Autowired
    ApplicationRepository applicationRepository;
    @Transactional
    public Application saveApplication(ApplicationRequest applicationRequest) {
        Application application = new Application();
        application.setStudentName(applicationRequest.getStudentName());
        application.setUniversityCourse(applicationRequest.getUniversityCourse());
        application.setUniversityName(applicationRequest.getUniversityName());
        applicationRepository.save(application);
        return application;
    }
}
