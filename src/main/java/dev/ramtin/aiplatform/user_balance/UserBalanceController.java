package dev.ramtin.aiplatform.user_balance;

import dev.ramtin.aiplatform.security.CustomUserDetails;
import dev.ramtin.aiplatform.transaction.Transaction;
import dev.ramtin.aiplatform.user_balance.dto.PurchaseCreditRequest;
import dev.ramtin.aiplatform.user_balance.service.UserBalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/balance")
@RequiredArgsConstructor
public class UserBalanceController {
    private final UserBalanceService userBalanceService;

    @GetMapping
    public ResponseEntity<UserBalance> getBalance() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String userId = userDetails.getId();

        UserBalance balance = userBalanceService.getBalanceByUserId(userId);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/purchase")
    public ResponseEntity<Transaction> purchaseCredit(@Valid @RequestBody PurchaseCreditRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String userId = userDetails.getUserId();

        Transaction transaction = userBalanceService.purchaseCredit(userId, request.getAmount(),
                request.getCurrency(), request.getReference());

        return ResponseEntity.ok(transaction);
    }
}