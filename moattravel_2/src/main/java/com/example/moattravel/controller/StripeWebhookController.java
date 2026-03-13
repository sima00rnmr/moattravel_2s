package com.example.moattravel.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.moattravel.service.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;

@Controller
public class StripeWebhookController {

	private final StripeService stripeService;

	@Value("${stripe.api-key}")
	private String stripeApiKey;

	@Value("${stripe.webhook-secret}")
	private String webhookSecret;

	public StripeWebhookController(StripeService stripeService) {
		this.stripeService = stripeService;
	}

	@PostMapping("/stripe/webhook")
	public ResponseEntity<String> webhook(@RequestBody String payload,
			@RequestHeader("Stripe-Signature") String sigHeader) {
		//System.out.println("🔥 Webhook HIT");⑤
		/*System.out.println("🔥 Webhook received!");//検証用
		 * どうやらこれが原因っぽい
		 * 
		 * */

		Stripe.apiKey = stripeApiKey;

		Event event = null;

		try {
			event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
		} catch (SignatureVerificationException e) {
			return new ResponseEntity<>("Webhook error", HttpStatus.BAD_REQUEST);
		}
		if ("checkout.session.completed".equals(event.getType())) {
			//System.out.println("🔥 Calling StripeService");  //検証用　⑤
			stripeService.processWebhook(payload, sigHeader);
		}

		return new ResponseEntity<>("Success", HttpStatus.OK);
	}
}