package com.urlshortener.naataurl.persistence.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.urlshortener.naataurl.persistence.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserName(String userName);

    User findByUserId(Long id);

    User findByUserEmail(String email);

    User findByConfirmationToken(String token);
}
