package com.example.moattravel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

//このファイルはコントローラとして扱うの宣言
@Controller
public class HomeController{
	//@GetMapping このURLにアクセスされたらこのメソッドを実行して
	// "～/～"はアドレスの指定
	@GetMapping("/")
	public String index() {
		return "index";
		
	}
	
}

