package com.sparta.cucumber.controller;

import com.sparta.cucumber.dto.JwtResponse;
import com.sparta.cucumber.dto.UserRequestDto;
import com.sparta.cucumber.service.UserService;
import com.sparta.cucumber.utils.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UserRestController {

    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    public final UserService userService;

    @PostMapping("/api/signup")
    public ResponseEntity<?> signup(@RequestBody UserRequestDto userDTO) throws Exception {
        System.out.println(userDTO.toString());
        userService.signup(userDTO);
        authenticate(userDTO.getName(), userDTO.getPassword());
        final UserDetails userDetails = userDetailsService.loadUserByUsername(userDTO.getName());
        final String token = jwtTokenUtil.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token, userDetails.getUsername()));
    }

    @PostMapping("/api/signin")
    public ResponseEntity<?> signin(@RequestBody UserRequestDto userDTO) throws Exception {
        System.out.println(userDTO.toString());
        userService.signin(userDTO);
        authenticate(userDTO.getName(), userDTO.getPassword());
        final UserDetails userDetails = userDetailsService.loadUserByUsername(userDTO.getName());
        final String token = jwtTokenUtil.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token, userDetails.getUsername()));
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
