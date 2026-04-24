package org.jas.ksinxapp.repo;

import org.jas.ksinxapp.model.PaymentStatus;
import org.jas.ksinxapp.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepo extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByPaypalOrderId(String paypalOrderId);
    Optional<PaymentTransaction> findByPayPalCaptureId(String payPalCaptureId);
    List<PaymentTransaction> findByUserId(Long userId);
    List<PaymentTransaction> findByUserIdAndStatus(Long userId, PaymentStatus status);
}
