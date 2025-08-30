package com.duckchat.api.controller;

import com.duckchat.api.dto.*;
import com.duckchat.api.entity.User;
import com.duckchat.api.repository.UserRepository;
import com.duckchat.api.security.JwtTokenProvider;
import com.duckchat.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

        UserInfoResponse userInfoResponse = UserInfoResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .mbtiType(user.getMbtiType())
                .build();

        return ResponseEntity.ok(new ApiResponse<>(true, "사용자 정보를 성공적으로 가져왔습니다.", userInfoResponse));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody SignupRequest request) {
        try {
            authService.signup(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "회원가입이 완료되었습니다.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "로그인이 완료되었습니다.", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@RequestBody TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        
        if (!tokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "유효하지 않은 리프레시 토큰입니다.", null));
        }
        
        String email = tokenProvider.getEmail(refreshToken);
        String newAccessToken = tokenProvider.createAccessToken(email);
        
        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
        
        return ResponseEntity.ok(new ApiResponse<>(true, "토큰이 성공적으로 갱신되었습니다.", tokenResponse));
    }
}
