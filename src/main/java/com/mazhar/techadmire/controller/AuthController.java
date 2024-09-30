package com.mazhar.techadmire.controller;


import com.mazhar.techadmire.model.entity.Application;
import com.mazhar.techadmire.model.entity.ERole;
import com.mazhar.techadmire.model.entity.Role;
import com.mazhar.techadmire.model.entity.User;
import com.mazhar.techadmire.model.request.ApplicationRequest;
import com.mazhar.techadmire.model.request.LoginRequest;
import com.mazhar.techadmire.model.request.SignupRequest;
import com.mazhar.techadmire.model.response.ApplicationResponse;
import com.mazhar.techadmire.model.response.MessageResponse;
import com.mazhar.techadmire.model.response.UserInfoResponse;
import com.mazhar.techadmire.repository.RoleRepository;
import com.mazhar.techadmire.security.jwt.JwtUtils;
import com.mazhar.techadmire.service.impl.ApplicationServiceImpl;
import com.mazhar.techadmire.service.impl.UserDetailsImpl;
import com.mazhar.techadmire.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600, allowCredentials="true")
@RestController
@RequestMapping("/tech-admire/api/auth")
public class AuthController {
  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtUtils jwtUtils;
  @Autowired
  ApplicationServiceImpl applicationService;

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    Authentication authentication = authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

    ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

    String token = jwtCookie.getValue();

    List<String> roles = userDetails.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());

    return ResponseEntity.ok().header(HttpHeaders.COOKIE, jwtCookie.toString())
        .body(new UserInfoResponse(userDetails.getId(),
                                   userDetails.getUsername(),
                                   userDetails.getEmail(),
                                   roles, token));
  }

  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
    }

    // Create new user's account
    User user = new User(signUpRequest.getUsername(),
                         signUpRequest.getEmail(),
                         encoder.encode(signUpRequest.getPassword()));
    Set<Role> roles = new HashSet<>();

      Role userRole = roleRepository.findByName(ERole.ROLE_USER)
          .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
      roles.add(userRole);

    user.setRoles(roles);
    userRepository.save(user);

    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }

  @PostMapping("/save")
  public ResponseEntity<?> saveApplication(
          @RequestHeader(value = "Authorization", required = false) String authorization,
          @Valid @RequestBody ApplicationRequest applicationRequest) {

    if (authorization == null || !authorization.startsWith("Bearer ")) {
      return ResponseEntity.status(401).body(new ApplicationResponse(false, "Unauthorized"));
    }

    String token = authorization.substring(7); // Remove "Bearer " prefix

    // Validate the token
    if (!jwtUtils.validateJwtToken(token)) {
      return ResponseEntity.status(401).body(new ApplicationResponse(false, "Unauthorized"));
    }

    // Proceed with saving the application
    Application application = applicationService.saveApplication(applicationRequest);

    return ResponseEntity.ok(new ApplicationResponse(true, "Application submitted successfully " + application.getApplicationId()));
  }

}
