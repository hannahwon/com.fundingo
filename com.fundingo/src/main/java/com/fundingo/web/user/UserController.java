package com.fundingo.web.user;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fundingo.service.community.CommunityService;
import com.fundingo.service.domain.Community;
import com.fundingo.service.domain.Funding;
import com.fundingo.service.domain.Product;
import com.fundingo.service.domain.Project;
import com.fundingo.service.domain.User;
import com.fundingo.service.domain.UserReport;
import com.fundingo.service.funding.FundingService;
import com.fundingo.service.project.ProjectService;
import com.fundingo.service.sales.SalesService;
import com.fundingo.service.user.UserService;
import com.fundingo.Application;
import com.fundingo.common.Page;
import com.fundingo.common.Search;

@Controller
@RequestMapping("/user/*")
public class UserController {
	
	// Field
	@Autowired
	private UserService userService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private SalesService salesService;
	@Autowired
	private FundingService fundingService;
	@Autowired
	private CommunityService communityService;
	
	private static final Logger LOGGER = LogManager.getLogger(Application.class);

	
	// Constructor
	public UserController() {
		LOGGER.info(this.getClass()+"??? Default Constructor ??????");
	}
	
	
	// Method
	// ???????????? ????????? ??????
	@GetMapping( "/addUser" )
	public String addUser() throws Exception{
		
		LOGGER.info("/user/addUser : GET");
		
		return "/user/addUser";
	}
	
	
	// ???????????? ??????
	@PostMapping( "/addUser" )
	public String addUser( @ModelAttribute("user") User user ) throws Exception{
		
		LOGGER.info("/user/addUser : POST");
		
		userService.addUser(user);
		
		return "/user/login";
	}
	
	
	// ????????? ????????? ??????
	@GetMapping( "/login" )
	public String login() throws Exception{
		
		LOGGER.info("/user/login : GET");

		return "/user/login";
	}
	

	// ????????? ??????
	@PostMapping( "/login" )
	public String login( @ModelAttribute("user") User user, HttpSession session, Model model ) throws Exception{
		
		LOGGER.info("/user/login : POST");
		
		String result = "";
		User dbUser = userService.login(user.getEmail(), user.getPassword());
		LOGGER.info("dbUser : " + dbUser);
		
		if(dbUser == null) {
			result = "/user/login";
			
		}else if(user.getPassword().equals(dbUser.getPassword())) {
			
			dbUser.setLoginStatus("Y");
			userService.updateUser(dbUser);
			
			session.setAttribute("user", dbUser);
			session.setMaxInactiveInterval(60*60); //?????? ?????? ??????

			result = "main";
			
			// ???????????? ????????? ???????????? ?????????
			String[] type = {"themeEndIMI", "themeSuccessIMI", "themeOpen", "banner", "themeConcern"};
			Search search = new Search();
			search.setCurrentPage(1);
			search.setPageSize(10);
			for(int i=0; i<type.length; i++) {
													
				List<Project> temp = projectService.getPjtList(dbUser.getUserNo()	, type[i], search);		
				LOGGER.info(type[i] + "size :"+ temp.size());
				model.addAttribute(type[i], temp);
			}
			model.addAttribute("listType", type);
		}
		
		return result;
	}
	
	// ????????? ????????? ??? ????????????
	@GetMapping("/kakaoLogin/{kakao}")
	public String kakaoLogin(@PathVariable String kakao , HttpSession session, Model model) throws Exception {
		
		LOGGER.info("/user/kakaoLogin : GEt");
		
		String result = "";
		User dbUser = userService.kakaoLogin(kakao);
		LOGGER.info("dbUser : " + dbUser);
		
		if(dbUser == null) {
			result = "/user/addUser";
			
		}else {
			
			dbUser.setLoginStatus("Y");
			userService.updateUser(dbUser);
			
			session.setAttribute("user", dbUser);
			result = "main";
			
			// ???????????? ????????? ???????????? ?????????
			String[] type = {"themeEndIMI", "themeSuccessIMI", "themeOpen", "banner", "themeConcern"};
			Search search = new Search();
			search.setCurrentPage(1);
			search.setPageSize(10);
			for(int i=0; i<type.length; i++) {
													
				List<Project> temp = projectService.getPjtList(dbUser.getUserNo()	, type[i], search);		
				LOGGER.info(type[i] + "size :"+ temp.size());
				model.addAttribute(type[i], temp);
			}
			model.addAttribute("listType", type);
		}
			
		return result;
	}  
	
	
	// ????????????
	@GetMapping( "/logout/{userNo}" )
	public String logout( @PathVariable int userNo, HttpSession session, Model model ) throws Exception{
		
		LOGGER.info("/user/logout : GET");
		
		User dbUser = userService.getUser(userNo);
		dbUser.setLoginStatus("N");
		userService.updateUser(dbUser);
		
		session.invalidate(); //????????? ?????? ??? ??????
		dbUser = new User();

		// ???????????? ????????? ???????????? ?????????
		String[] type = {"themeEndIMI", "themeSuccessIMI", "themeOpen", "banner", "themeConcern"};
		Search search = new Search();
		search.setCurrentPage(1);
		search.setPageSize(10);
		for(int i=0; i<type.length; i++) {
												
			List<Project> temp = projectService.getPjtList(dbUser.getUserNo()	, type[i], search);		
			LOGGER.info(type[i] + "size :"+ temp.size());
			model.addAttribute(type[i], temp);
		}
		model.addAttribute("listType", type);
		
		return "main";
	}
	
	
	// ?????????/?????????????????? ????????? ??????
	@GetMapping("/getEmailPwd")
	public String getEmailPwd() throws Exception {

		LOGGER.info("/user/getEmailPwd : GET");

		return "/user/getEmailPwd";
	}
	
	
	// ?????????????????? ??????
	@GetMapping( "/getUser/{userNo}" )
	public String getUser( @PathVariable int userNo, Model model ) throws Exception {
		
		LOGGER.info("/user/getUser : GET");
		
		model.addAttribute("user", userService.getUser(userNo));
		
		return "/user/getUser";
	}
	
	
	// ?????????????????? ?????? ????????? ??????
	@GetMapping( "/updateUser/{userNo}" )
	public String updateUser( @PathVariable int userNo, Model model ) throws Exception{

		LOGGER.info("/user/updateUser : GET");
		
		model.addAttribute("user", userService.getUser(userNo));
		
		return "/user/updateUser";
	}
	
	
	// ?????????????????? ?????? ??????
	@PostMapping( "/updateUser" )
	public String updateUser( @ModelAttribute("user") User user, 
			                  @RequestParam(value="profile", required=false) MultipartFile file,   
			                  HttpSession session, HttpServletRequest request) throws Exception{

		LOGGER.info("/user/updateUser : POST");
		
//		LOGGER.info("file : " + file);
//		LOGGER.info("file.getSize() : " + file.getSize());
		
//		if(file.getSize() < 1) {
//			user.setProfileImg(userService.getUser(user.getUserNo()).getProfileImg());
//			LOGGER.info("user.setProfileImg : " + userService.getUser(user.getUserNo()).getProfileImg());
//			userService.updateUser(user);
//		
//		}else {
			String ImgPath = ClassLoader.getSystemResource("./").getPath().substring(1, ClassLoader.getSystemResource("./").
					  getPath().lastIndexOf("target"))+ "src/main/resources/static/img";
			ImgPath = java.net.URLDecoder.decode(ImgPath ,"UTF-8");
			
			// ?????? ?????? ??????
			UUID uuid = UUID.randomUUID();
			String s = Long.toString(uuid.getMostSignificantBits(), 36) + '-' + Long.toString(uuid.getLeastSignificantBits(), 36);
			
			String saveName = s + "_" + file.getOriginalFilename();
			LOGGER.info("saveName : " + saveName);
		  
			// ?????? ?????? ??????
			String fileFullPath = ImgPath + "/" + saveName;
			LOGGER.info("fileFullPath : " + fileFullPath);
			
			try {
				file.transferTo(new File(fileFullPath)); //???????????? ???????????? service?????? ??????
			} catch (Exception e) {
				e.printStackTrace();
			}  
		  
			user.setProfileImg(saveName);
			userService.updateUser(user);
//		}
		
		String sessionEmail = ((User)session.getAttribute("user")).getEmail();
		
		if(sessionEmail.equals(user.getEmail())){
			session.setAttribute("user", user);
		}
		
		Thread.sleep(1000);
		return "redirect:/user/getUser/"+user.getUserNo();
	}
	
	
	// ??????????????? ??????
	@GetMapping( "/getMyPage/{userNo}" )
	public String getMyPage( @PathVariable int userNo, Model model ) throws Exception{
		
		LOGGER.info("/user/getMyPage : GET");

		model.addAttribute("user", userService.getUser(userNo));
		
		return "/user/getMyPage";
	}
	
	
	// ????????? ??????
	@GetMapping( "/getProfile/{userNo}" )
	public String getProfile( @PathVariable int userNo, Model model ) throws Exception {
		
		LOGGER.info("/user/getProfile : GET");
		
//		Search search = new Search();
		
		model.addAttribute("user", userService.getUser(userNo));
//		model.addAttribute("search", search);
		
		return "/user/getProfile";
	}
	
	
	// ?????? ?????? ??????
	@GetMapping("/getFundList/{userNo}")
	public String getFundList(@PathVariable int userNo, Model model) throws Exception {

		LOGGER.info("/user/getFundList : GET");
		
		Search search = new Search();
	    if (search.getCurrentPage() == 0) {
	    	search.setCurrentPage(1);
	    }
	      
	    search.setPageSize(20);     

	    Map<String, Object> map = fundingService.getFundList(userNo, search);    

	    Page resultPage = new Page(search.getCurrentPage(), ((Integer) map.get("totalCount")).intValue(), 5,20);
	    System.out.println(resultPage);

	    model.addAttribute("fundList", map.get("fundList"));
	    model.addAttribute("resultPage", resultPage);
	    model.addAttribute("search", search);
		
		return "/funding/getFundList";
	}
	
	
	// ??????????????? ?????? ??????
	@GetMapping( "/getFavoriteList/{userNo}" )
	public String getFavoriteList( @PathVariable int userNo, Model model ) throws Exception {

		LOGGER.info("/user/getFavoriteList : GET");

		model.addAttribute("user", userService.getUser(userNo));
		model.addAttribute("fPjt", projectService.getFavoriteList(userNo));

		return "/user/getFavoriteList";
	}
	
	
	// ?????? ?????? ??????
	@GetMapping( "/getCouponList/{userNo}" )
	public String getCouponList( @PathVariable int userNo, Model model ) throws Exception {

		LOGGER.info("/user/getCouponList : GET");

//		Search search = new Search();
//		search.setCurrentPage(1);
//		search.setPageSize(9);

		model.addAttribute("couponList", salesService.getCouponList(userNo));
//		model.addAttribute("menu", "funding");
//		model.addAttribute("currentPage", search.getCurrentPage());
	
		return "/sales/getMyCouponList";
	}
	
	
	// ?????? ?????? ??????
	@GetMapping( "/getChatList/{userNo}" )
	public String getChatList( @PathVariable int userNo, Model model ) throws Exception {

		LOGGER.info("/user/getChatList : GET");

		model.addAttribute("user", userService.getUser(userNo));
		model.addAttribute("chat", projectService.getChatList(userNo));

		return "/user/getChatList";
	}
	
	
	// ???????????? ????????? ??????
	@GetMapping( "/deleteUser/{userNo}" )
	public String deleteUser( @PathVariable int userNo, Model model ) throws Exception{
		
		LOGGER.info("/user/deleteUser : GET");
		
		model.addAttribute("user", userService.getUser(userNo));
		
		return "/user/deleteUser";
	}
	
	
	// ???????????? ??????
	@PostMapping( "/deleteUser" )
	public String deleteUser( @ModelAttribute("user") User user, Model model , HttpSession session ) throws Exception {

		LOGGER.info("/user/deleteUser : POST");
		
		userService.deleteUser(user);
		session.invalidate(); //????????? ?????? ??? ??????
		
		User dbUser = new User();
		
		String[] type = {"themeEndIMI", "themeSuccessIMI", "themeOpen", "banner", "themeConcern"};
		Search search = new Search();
		search.setCurrentPage(1);
		search.setPageSize(10);
		for(int i=0; i<type.length; i++) {
												
			List<Project> temp = projectService.getPjtList(dbUser.getUserNo()	, type[i], search);		
			LOGGER.info(type[i] + "size :"+ temp.size());
			model.addAttribute(type[i], temp);
		}
		model.addAttribute("listType", type);
		
		return "main";
	}
	
	
	// ???????????? ?????? (????????????)
	@RequestMapping( "/getUserList" )
	public String getUserList( @ModelAttribute("search") Search search, Model model ) throws Exception{
		
		LOGGER.info("/user/getUserList : GET/POST");
		
		if(search.getCurrentPage() == 0 ){
			search.setCurrentPage(1);
		}
		search.setPageSize(20);
		
		Map<String , Object> map = userService.getUserList(search);
		
		Page resultPage = new Page( search.getCurrentPage(), ((Integer)map.get("totalCount")).intValue(), 5, 20);
		LOGGER.info("resultPage : " + resultPage);
		
		model.addAttribute("userMap", map);
		model.addAttribute("resultPage", resultPage);
		
		return "/user/getUserListAdmin";
	}
	
	
	// ?????????????????? ?????? (????????????)
	@GetMapping( "/getUserReportHistory/{userNo}" )
	public String getUserReportHistory( @ModelAttribute("search") Search search,
										@PathVariable("userNo") int userNo,
										Model model, HttpServletRequest request) throws Exception{
		
		LOGGER.info("/user/getUserReportHistory : GET");
		
		if(search.getCurrentPage() == 0 ){
			search.setCurrentPage(1);
		}
		search.setPageSize(20);
		
		model.addAttribute("targetUserNo", userService.getUserReportHistory(search, userNo).get(0).getTargetUserNo());
		model.addAttribute("userList", userService.getUserReportHistory(search, userNo));
		model.addAttribute("search", search);
		
		return "/user/getReportUserListAdmin";
	}
	
	
	// ????????????
	@PostMapping( "/addReportUser" )
	public String addReportUser( @ModelAttribute("userReport") UserReport userReport, Model model ) throws Exception{
					
		LOGGER.info("/user/addReportUser : POST");

		userService.addReportUser(userReport);
			
		model.addAttribute("community", communityService.getBoard(userReport.getPostNo()));
				
		return "/community/getBoard";
	}
	
	
	
	
}
