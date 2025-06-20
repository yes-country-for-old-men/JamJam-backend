package com.jamjam.user.application;


import com.jamjam.global.exception.ApiException;
import com.jamjam.infra.jwt.dto.JwtUserDto;
import com.jamjam.user.application.dto.CustomUserDetails;
import com.jamjam.user.domain.repository.UserRepository;
import com.jamjam.user.exception.UserError;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException{

        JwtUserDto userData = userRepository.findByLoginId(loginId)
                .map(user -> JwtUserDto.builder()
                        .userId(user.getId())
                        .userEmail(user.getLoginId())
                        .password(user.getPassword())
                        .role(user.getRole().name())
                        .build()
                ).orElseThrow(() -> new ApiException(UserError.USER_NOT_FOUND));

        return new CustomUserDetails(userData);
    }
}
