package com.example.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.entity.Info;
import com.example.service.RecommendService;

import jakarta.servlet.http.HttpSession;

@Controller
public class RecommendController {

	@Autowired
	RecommendService recommendService;

	@GetMapping("/recommend")
	public String showRecommendPage(Model model, HttpSession session) {
		System.out.println("[RecommendController][/recommend]");

		Info info = (Info) session.getAttribute("mvo");

		// TODO : RecommendUser는 현재 랜덤으로 아무나 가져오는 매커니즘임.
		// 추후 이성추천 시스템과 필터링 시스템을 거쳐 가지고 오는 것으로 바뀌어야함.
		// TODO : 한 번 좋아요를 누르면 더 이상 추천에 뜨지 않음
		Info recommendUser = recommendService.getRecommendUsers(info);

		List<String> imageDatas = recommendService.getS3Photos(recommendUser);
		

		model.addAttribute("imageDatas", imageDatas);
		model.addAttribute("recommendUser", recommendUser);

		return "recommend/recommend";
	}
	
	@GetMapping("/recommendLike")
	public String like(HttpSession session, @RequestParam String oppUserName) {
		System.out.println("[RecommendController][/recommendLike]");
		
		Info info = (Info) session.getAttribute("mvo");
		
		// from, to 둘 다 저장
		recommendService.saveLikeInteraction(info.getUsername(), oppUserName, "like");
		
		/*
		 * oppUserName에게 좋아요를 받은 상태에서 좋아요를 누르면
		 * 1. matching이 됨. (매칭 확인)
		 * 2. 버튼을 누르면 채팅방을 팔 수 있음.
		 */
		Boolean isLikeMe = recommendService.isCheckLikeMe(info, oppUserName);
		
		/*
		 *  상대가 날 좋아하면 매칭을 잡아줌
		 *  상대 matchingUuid를 집어 넣음.
		 */
		if (isLikeMe) {
			recommendService.saveMatching(info, oppUserName);
			return "redirect:/matching?oppUserName="+oppUserName;
		}
		
		return "redirect:/recommend";
	}

	
	@GetMapping("/recommendDislike")
	public String dislike(HttpSession session, @RequestParam String oppUserName) {
		System.out.println("[RecommendController][/recommendDislike]");
		
		Info info = (Info) session.getAttribute("mvo");
		
		// from, to 둘 다 저장
		recommendService.saveLikeInteraction(info.getUsername(), oppUserName, "dislike");
		
		return "redirect:/recommend";
	}
	
	
	@GetMapping("/matching")
	public String showMatching(@RequestParam String oppUserName, Model model) {
		System.out.println("[RecommendController][/matching]");
		
		Info oppInfo = recommendService.getOppInfo(oppUserName);
		List<String> imageDatas = recommendService.getS3Photos(oppInfo);
		
		model.addAttribute("imageDatas", imageDatas);
		model.addAttribute("oppInfo", oppInfo);
		
		return "recommend/matching";
	}

	
	
}
