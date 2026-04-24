package org.jas.ksinxapp.repo;

import org.jas.ksinxapp.model.Subscription;
import org.jas.ksinxapp.model.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepo extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByPaypalSubscriptionId(String paypalSubscriptionId);
    List<Subscription> findByUserId(Long userId);
    List<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);
}
