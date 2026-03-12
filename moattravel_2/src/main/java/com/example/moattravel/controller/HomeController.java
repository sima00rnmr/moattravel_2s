package com.example.moattravel.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.moattravel.entity.House;
import com.example.moattravel.repository.HouseRepository;

//このファイルはコントローラとして扱うの宣言
@Controller
public class HomeController{
	private final HouseRepository houseRepository;
	
	public HomeController(HouseRepository houseRepository) {
		this.houseRepository = houseRepository;
	}
	//@GetMapping このURLにアクセスされたらこのメソッドを実行して
	// "～/～"はアドレスの指定
	@GetMapping("/")
	public String index(Model model) {
		List<House>newHouses =houseRepository.findTop10ByOrderByCreatedAtDesc();
		model.addAttribute("newHouses",newHouses);
		return "index";
	}
	
}

