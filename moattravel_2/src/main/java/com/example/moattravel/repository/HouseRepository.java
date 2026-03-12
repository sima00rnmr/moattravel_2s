package com.example.moattravel.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.moattravel.entity.House;

public interface HouseRepository extends JpaRepository<House, Integer> {
	/*ど忘れ…
	 * インターフェースはメソッドの詳細を書かないから;で終わって良い
	 * ので｛｝の形式を使わない
	 * */
	public Page<House> findByNameLike(String keyword, Pageable pageable);
	
	public Page<House> findByNameLikeOrAddressLike(String nameKeyword, String addressKeyword, Pageable pageable);
	public Page<House> findByAddressLike(String area, Pageable pageable);
	public Page<House> findByPriceLessThanEqual(Integer price, Pageable pageable);
	public Page<House> findByNameLikeOrAddressLikeOrderByCreatedAtDesc(String nameKeyword, String addressKeyword, Pageable pageable);
	public Page<House> findByNameLikeOrAddressLikeOrderByPriceAsc(String nameKeyword, String addressKeyword, Pageable pageable);
	public Page<House> findByAddressLikeOrAddressLikeOrderByCreatedAtDesc(String area, Pageable pageable);
	public Page<House> findByAddressLikeOrAddressLikeOrderByPriceAsc(String area, Pageable pageable);
	public Page<House> findByPriceLessThanEqualOrderByCreatedAtDesc(Integer price, Pageable pageable);
	public Page<House> findByPriceLessThanEqualOrderByPriceAsc(Integer price, Pageable pageable);
	public Page<House> findByCreatedDesc(Pageable pageable);
	public Page<House> finAllByOrderByPriceAsc(Pageable pageable);







}