package com.tripleng.shopappserver.repositories;

import com.tripleng.shopappserver.models.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
}
