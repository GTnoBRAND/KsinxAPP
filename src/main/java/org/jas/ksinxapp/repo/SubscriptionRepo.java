package org.jas.ksinxapp.repo;

import org.jas.ksinxapp.model.SubscriptionModel;
import org.jas.ksinxapp.model.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepo extends JpaRepository<SubscriptionModel, Long> {
    Optional<SubscriptionModel> findByPaypalSubscriptionId(String paypalSubscriptionId);
    List<SubscriptionModel> findByUserId(Long userId);
    List<SubscriptionModel> findByUserIdAndStatus(Long userId, SubscriptionStatus status);
}
