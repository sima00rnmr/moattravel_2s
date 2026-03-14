package com.example.moattravel.service;

import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.moattravel.form.ReservationRegisterForm;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;

@Service
public class StripeService {

	@Value("${stripe.api-key}")
	private String stripeApiKey;

	@Value("${stripe.webhook-secret}")
	private String webhookSecret;

	private final ReservationService reservationService;

	public StripeService(ReservationService reservationService) {
		this.reservationService = reservationService;
	}

	// =========================
	// Checkoutセッション作成
	// =========================
	public String createStripeSession(
			String houseName,
			ReservationRegisterForm form,
			HttpServletRequest request) {

		Stripe.apiKey = stripeApiKey;

		String requestUrl = request.getRequestURL().toString();

		SessionCreateParams params = SessionCreateParams.builder()
				.addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
				.addLineItem(
						SessionCreateParams.LineItem.builder()
								.setPriceData(
										SessionCreateParams.LineItem.PriceData.builder()
												.setProductData(
														SessionCreateParams.LineItem.PriceData.ProductData.builder()
																.setName(houseName)
																.build())
												.setUnitAmount((long) form.getAmount())
												.setCurrency("jpy")
												.build())
								.setQuantity(1L)
								.build())
				.setMode(SessionCreateParams.Mode.PAYMENT)
				.setSuccessUrl(requestUrl.replaceAll("/houses/[0-9]+/reservations/confirm", "")
						+ "/reservations?reserved")
				.setCancelUrl(requestUrl.replace("/reservations/confirm", ""))
				.setPaymentIntentData(
						SessionCreateParams.PaymentIntentData.builder()
								.putMetadata("houseId", form.getHouseId().toString())
								.putMetadata("userId", form.getUserId().toString())
								.putMetadata("checkinDate", form.getCheckinDate())
								.putMetadata("checkoutDate", form.getCheckoutDate())
								.putMetadata("numberOfPeople", form.getNumberOfPeople().toString())
								.putMetadata("amount", form.getAmount().toString())
								.build())
				.build();

		try {
			Session session = Session.create(params);
			return session.getId();
		} catch (StripeException e) {
			e.printStackTrace();
			return "";
		}
	}

	// =========================
	// Webhook処理
	// =========================
	public void processWebhook(String payload, String sigHeader) {

		try {

			System.out.println("🔥 StripeService START");

			Stripe.apiKey = stripeApiKey;

			Event event = Webhook.constructEvent(
					payload,
					sigHeader,
					webhookSecret);

			System.out.println("🔥 Event type = " + event.getType());

			if (!"checkout.session.completed".equals(event.getType())) {
				System.out.println("🔥 Not checkout.session.completed");
				return;
			}

			Optional<StripeObject> optional = event.getDataObjectDeserializer().getObject();

			System.out.println("🔥 Optional = " + optional);

			if (optional.isEmpty()) {
				System.out.println("🔥 Optional EMPTY");
				return;
			}

			Session session = (Session) optional.get();

			System.out.println("🔥 Session ID = " + session.getId());

			session = Session.retrieve(
					session.getId(),
					SessionRetrieveParams.builder()
							.addExpand("payment_intent")
							.build(),
					null);

			System.out.println("🔥 Session retrieved");

			if (session.getPaymentIntentObject() == null) {
				System.out.println("🔥 PaymentIntent NULL");
				return;
			}

			Map<String, String> metadata = session.getPaymentIntentObject().getMetadata();

			System.out.println("🔥 METADATA = " + metadata);

			reservationService.create(metadata);

			System.out.println("🔥 RESERVATION CREATED");

		} catch (SignatureVerificationException e) {

			System.out.println("❌ Invalid Stripe signature");

		} catch (Exception e) {

			System.out.println("❌ Stripe Error");
			e.printStackTrace();

		}
	}
}