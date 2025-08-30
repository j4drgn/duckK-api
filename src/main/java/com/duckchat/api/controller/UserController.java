package com.duckchat.api.controller;

import com.duckchat.api.dto.ApiResponse;
import com.duckchat.api.dto.UserInfoResponse;
import com.duckchat.api.dto.UserUpdateRequest;
import com.duckchat.api.entity.User;
import com.duckchat.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

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

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> updateUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserUpdateRequest updateRequest) {
        
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

        if (updateRequest.getNickname() != null) {
            user.updateNickname(updateRequest.getNickname());
        }

        if (updateRequest.getMbtiType() != null) {
            user.updateMbtiType(updateRequest.getMbtiType());
        }

        User updatedUser = userRepository.save(user);

        UserInfoResponse userInfoResponse = UserInfoResponse.builder()
                .id(updatedUser.getId())
                .email(updatedUser.getEmail())
                .nickname(updatedUser.getNickname())
                .profileImageUrl(updatedUser.getProfileImageUrl())
                .mbtiType(updatedUser.getMbtiType())
                .build();

        return ResponseEntity.ok(new ApiResponse<>(true, "사용자 정보가 성공적으로 업데이트되었습니다.", userInfoResponse));
    }
}
