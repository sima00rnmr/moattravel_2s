package com.example.moattravel.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

	
	/*@Beanアノテーション
	 * そのメソッドの戻り値が
	 * DIコンテナに登録されるようになる
	 * ここでは、
	 * securityFilterChain()およびpasswordEncoder()メソッドの戻り値
	 * が登録されるから
	 * セキュリティ設定やパスワードの
	 * ハッシュアルゴリズムを適用するようになる
	 * 
	 * */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		/*だれにどのページのアクセスを許可するの？を指定している
		 * 利用者に管理者画面が見えちゃダメだものね…
		 * 
		 * hasRole…エンティティに入れたアレ
		 * （このロールを持っている人はこれって出来る）
		 * ロール毎に設定ができる
		 * */
		
		http.authorizeHttpRequests((requests) -> requests
				.requestMatchers("/css/**", "/images/**", "/js/**", "/strage/**","/signup/**","/").permitAll() //全てのユーザにアクセスを許可するURL
				.requestMatchers("/admin/**").hasRole("ADMIN")//管理者にのみアクセスを許可するURL
				.anyRequest().authenticated() //上記以外のURLはログインが必要（会員または管理者のどちらでもOK）
		)

				.formLogin((form) -> form
						.loginPage("/login") //ログインページのURL
						.loginProcessingUrl("/login") //ログインフォームの送信先URL
						.defaultSuccessUrl("/?loggedIn")//ログイン成功時のリダイレクト先URL
						.failureUrl("/login?error") //ログイン失敗時のリダイレクト先
						.permitAll())

				.logout((logout) -> logout
						.logoutSuccessUrl("/?loggedOut") //ログアウト時のダイレクト先URL
						.permitAll());
		return http.build();

	}

	/*　※BCrypt 
	 * パスワード用のハッシュ値を生成してくれる
	 * 強力なハッシュアルゴリズム
	 * 
	 * ここでは…
	 * BCryptPasswordEncoderクラスのインスタンスを返す
	 * */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}