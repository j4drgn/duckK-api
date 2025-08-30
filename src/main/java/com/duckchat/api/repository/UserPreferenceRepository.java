package com.duckchat.api.repository;

import com.duckchat.api.entity.User;
import com.duckchat.api.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    List<UserPreference> findByUser(User user);
    List<UserPreference> findByUserAndPreferenceType(User user, String preferenceType);
}
