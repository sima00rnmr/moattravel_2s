const stripe =Stripe("pk_test_51T8wdf1WsqIpaESXlGB49Wb2wGZ4vhOEwfHcf5jxKPmaOvEAaeW8IQl4JSLsXvtbait2Qh5cRnT8rTyxS6JSv8za00hOfMw0OA");
const paymentButtom = document.querySelector('#paymentButton');

paymentButtom.addEventListener('click', () => {
	stripe.redirectToCheckout({
		sessionId:sessionId
	})
	
});
