package com.example.moattravel.controller;
/*解決方法
 * ①安全チェック付き→強制デシリアライズを使用
 * getObject()では取得できないからOptional.emptyを使用。
 * 
 * 結果としてつながりはしたけど、これ問題ない…？
 * 
 * 
 * ②expandでpayment_intentを展開
 * 
 * 
 * metadataを確実に取得するため（→session.getMetadata()の取得）
 * 以下の追記
 * Session session = Session.retrieve(
    session.getId(),
    SessionRetrieveParams.builder()
        .addExpand("payment_intent")
        .build(),
    null
);
 * 原因（後程もう少しちゃんと調べたい…）
 * event.getDataObjectDeserializer().getObject() が 
 * Optional.empty(イベントの中身をJavaオブジェクトに変換できなかった) になる場合があり
 *Checkout Sessionを取得できなかったため。
 *
 *SDKの安全デシリアライズに失敗する場合がある理由
 *・APIバージョン差
 *・オブジェクト構造
 *・フィールド展開　など
 *
 *…WebhookのJSONは不完全なことがある
 *
 *ライブラリで解決できないの…？
 *ライブラリをフォークする必要がある…（メンテ地獄、バージョン更新不能で非推奨）
 *
 *〇公式が用意している逃げ道
 *
 *deserializeUnsafe()
 *
 *安全保証はしないけど
とりあえずオブジェクト作る

 * */



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
    public ResponseEntity<String> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        System.out.println("🔥 Webhook HIT");

        Stripe.apiKey = stripeApiKey;

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            System.out.println("❌ Signature verification failed");
            return new ResponseEntity<>("Webhook error", HttpStatus.BAD_REQUEST);
        }

        System.out.println("Stripe Event: " + event.getType());

        if ("checkout.session.completed".equals(event.getType())) {
            System.out.println("🔥 Calling StripeService");
            stripeService.processWebhook(payload, sigHeader);
        }

        return ResponseEntity.ok("Success");
    }
}