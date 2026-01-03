package multitier.trans.controllers.rest;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Payments", description = "API for processing Stripe payments")
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PaymentRestController {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.public.key}")
    private String stripePublicKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Data
    public static class CreatePaymentRequest {
        private Long amount; // Amount in cents
        private String currency;
        private String description;
    }

    @Data
    public static class PaymentResponse {
        private String clientSecret;
        private String paymentIntentId;
        private String publicKey;
    }

    @Operation(
        summary = "Get Stripe public key",
        description = "Returns the Stripe public key for frontend initialization"
    )
    @ApiResponse(responseCode = "200", description = "Public key returned successfully")
    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("publicKey", stripePublicKey);
        return ResponseEntity.ok(config);
    }

    @Operation(
        summary = "Create a payment intent",
        description = "Creates a Stripe PaymentIntent and returns the client secret for frontend payment processing"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment intent created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Stripe API error"
        )
    })
    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody CreatePaymentRequest request) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(request.getAmount())
                .setCurrency(request.getCurrency() != null ? request.getCurrency() : "eur")
                .setDescription(request.getDescription())
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                )
                .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            PaymentResponse response = new PaymentResponse();
            response.setClientSecret(paymentIntent.getClientSecret());
            response.setPaymentIntentId(paymentIntent.getId());
            response.setPublicKey(stripePublicKey);

            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(
        summary = "Confirm payment status",
        description = "Retrieves the status of a payment intent"
    )
    @GetMapping("/status/{paymentIntentId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            Map<String, Object> status = new HashMap<>();
            status.put("id", paymentIntent.getId());
            status.put("status", paymentIntent.getStatus());
            status.put("amount", paymentIntent.getAmount());
            status.put("currency", paymentIntent.getCurrency());

            return ResponseEntity.ok(status);
        } catch (StripeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
