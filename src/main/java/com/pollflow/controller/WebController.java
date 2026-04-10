package com.pollflow.controller;

import com.pollflow.dto.*;
import com.pollflow.entity.User;
import com.pollflow.entity.Vote;
import com.pollflow.repository.UserRepository;
import com.pollflow.repository.VoteRepository;
import com.pollflow.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {
    private final PollService pollService;
    private final CategoryService categoryService;
    private final AdminService adminService;
    private final UserService userService;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final VoteRepository voteRepository;

    @GetMapping("/")
    public String home(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isLoggedIn = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
            if (isLoggedIn) {
                try {
                    UserDTO user = userService.getProfile();
                    model.addAttribute("user", user);
                    long unreadCount = notificationService.getUnreadCount();
                    model.addAttribute("unreadCount", unreadCount);
                } catch (Exception e) {
                    model.addAttribute("unreadCount", 0L);
                }
            }
            model.addAttribute("isLoggedIn", isLoggedIn);
        } catch (Exception e) {
            model.addAttribute("isLoggedIn", false);
        }
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return "redirect:/login";
            }
            
            UserDTO user = userService.getProfile();
            model.addAttribute("user", user);
            
            if (user.getRole().equals("VERIFICATION_ADMIN") || user.getRole().equals("POLL_ADMIN")) {
                return "redirect:/admin";
            }
            
            return "redirect:/polls";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/login")
    public String loginPage(Model model, @RequestParam(required = false) String error, 
                           @RequestParam(required = false) String message,
                           @RequestParam(required = false) String redirect) {
        if (error != null) {
            String errorMsg = message != null ? message : "Invalid email or password";
            model.addAttribute("error", errorMsg);
        }
        model.addAttribute("redirectUrl", redirect);
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@RequestParam String fullName,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam(required = false) String mobile,
                               @RequestParam(required = false) String gender,
                               @RequestParam(required = false) Integer age,
                               @RequestParam(required = false) String region,
                               @RequestParam(required = false) String city,
                               @RequestParam(required = false) String postalCode,
                               @RequestParam(required = false) String religion,
                               @RequestParam(required = false) String address,
                               @RequestParam(required = false) String maritalStatus,
                               Model model) {
        try {
            RegisterRequest request = new RegisterRequest();
            request.setFullName(fullName);
            request.setEmail(email);
            request.setPassword(password);
            request.setMobile(mobile);
            request.setGender(gender);
            request.setAge(age);
            request.setRegion(region);
            request.setCity(city);
            request.setPostalCode(postalCode);
            request.setReligion(religion);
            request.setAddress(address);
            request.setMaritalStatus(maritalStatus);
            authService.register(request);
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/polls")
    public String polls(
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String pollType,
            @RequestParam(required = false) String search,
            Model model) {
        List<PollDTO> polls;
        
        polls = pollService.getPollsSorted(sortBy, category, pollType, search);
        
        model.addAttribute("polls", polls);
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isLoggedIn = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
            model.addAttribute("isLoggedIn", isLoggedIn);
            
            if (isLoggedIn) {
                UserDTO user = userService.getProfile();
                model.addAttribute("user", user);
                try {
                    long unreadCount = notificationService.getUnreadCount();
                    model.addAttribute("unreadCount", unreadCount);
                } catch (Exception e) {
                    model.addAttribute("unreadCount", 0L);
                }
            }
        } catch (Exception e) {
            model.addAttribute("isLoggedIn", false);
        }
        
        List<CategoryDTO> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedSort", sortBy);
        model.addAttribute("selectedPollType", pollType);
        model.addAttribute("searchQuery", search);
        
        return "polls";
    }

    @GetMapping("/profile")
    public String profile(@RequestParam(required = false) String success,
                         @RequestParam(required = false) String error,
                         Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return "redirect:/login";
            }
            
            try {
                UserDTO user = userService.getProfile();
                model.addAttribute("user", user);
            } catch (Exception e) {
                System.out.println("Error getting user profile: " + e.getMessage());
            }
            
            model.addAttribute("isLoggedIn", true);
            
            if ("true".equals(success)) {
                model.addAttribute("success", "Profile updated successfully!");
            }
            if (error != null) {
                model.addAttribute("error", error);
            }
            
            try {
                long unreadCount = notificationService.getUnreadCount();
                model.addAttribute("unreadCount", unreadCount);
            } catch (Exception e) {
                model.addAttribute("unreadCount", 0L);
            }
            
            try {
                UserDTO userDTO = userService.getProfile();
                List<Vote> recentVotes = voteRepository.findByUserIdOrderByVotedAtDesc(userDTO.getId(), PageRequest.of(0, 5)).getContent();
                
                model.addAttribute("recentVotes", recentVotes);
            } catch (Exception e) {
                model.addAttribute("recentVotes", List.of());
            }
            
            return "profile";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login";
        }
    }
    
    @PostMapping("/profile")
    public String updateProfile(@RequestParam String fullName,
                               @RequestParam(required = false) String mobile,
                               @RequestParam(required = false) String gender,
                               @RequestParam(required = false) Integer age,
                               @RequestParam(required = false) String region,
                               @RequestParam(required = false) String city,
                               @RequestParam(required = false) String postalCode,
                               @RequestParam(required = false) String religion,
                               @RequestParam(required = false) String address,
                               @RequestParam(required = false) String maritalStatus,
                               Model model) {
        try {
            userService.updateProfile(fullName, mobile, gender, age, region, city, postalCode, religion, address, maritalStatus);
            return "redirect:/profile?success=true";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            try {
                UserDTO user = userService.getProfile();
                model.addAttribute("user", user);
            } catch (Exception ex) {
                // ignore
            }
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("editMode", true);
            return "profile";
        }
    }
    
    @GetMapping("/favourites")
    public String favourites(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return "redirect:/login";
            }
            
            UserDTO user = null;
            try {
                user = userService.getProfile();
            } catch (Exception e) {
                System.out.println("Error getting user: " + e.getMessage());
            }
            
            List<PollDTO> favorites = pollService.getFavorites();
            
            if (user != null) {
                model.addAttribute("user", user);
            }
            model.addAttribute("favorites", favorites);
            model.addAttribute("isLoggedIn", true);
            
            try {
                long unreadCount = notificationService.getUnreadCount();
                model.addAttribute("unreadCount", unreadCount);
            } catch (Exception e) {
                model.addAttribute("unreadCount", 0L);
            }
            
            return "favourites";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return "redirect:/login";
            }
            
            UserDTO currentUser = userService.getProfile();
            AnalyticsDTO analytics = adminService.getDashboardAnalytics();
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("analytics", analytics);
            
            if (currentUser.getRole().equals("VERIFICATION_ADMIN")) {
                try {
                    List<UserDTO> pendingUsers = adminService.getPendingUsers();
                    List<UserDTO> allUsers = adminService.getAllUsers();
                    model.addAttribute("pendingUsers", pendingUsers != null ? pendingUsers : List.of());
                    model.addAttribute("allUsers", allUsers != null ? allUsers : List.of());
                } catch (Exception e) {
                    System.out.println("Error loading user data: " + e.getMessage());
                    model.addAttribute("pendingUsers", List.of());
                    model.addAttribute("allUsers", List.of());
                }
            } else if (currentUser.getRole().equals("POLL_ADMIN")) {
                try {
                    List<PollDTO> polls = pollService.getAllPolls();
                    List<CategoryDTO> categories = categoryService.getAllCategories();
                    model.addAttribute("polls", polls != null ? polls : List.of());
                    model.addAttribute("categories", categories != null ? categories : List.of());
                    model.addAttribute("topPolls", analytics.getTopPolls() != null ? analytics.getTopPolls() : List.of());
                } catch (Exception e) {
                    System.out.println("Error loading poll data: " + e.getMessage());
                    model.addAttribute("polls", List.of());
                    model.addAttribute("categories", List.of());
                }
            }
            
            return "admin";
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception in admin controller: " + e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/admin/analytics")
    public String adminAnalytics(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return "redirect:/login";
            }
            
            UserDTO currentUser = userService.getProfile();
            AnalyticsDTO analytics = adminService.getDashboardAnalytics();
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("analytics", analytics);
            
            if (currentUser.getRole().equals("POLL_ADMIN")) {
                model.addAttribute("topPolls", analytics.getTopPolls() != null ? analytics.getTopPolls() : List.of());
            }
            
            return "admin-analytics";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login";
        }
    }

    @GetMapping("/admin/profile")
    public String adminProfile(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return "redirect:/login";
            }
            
            UserDTO currentUser = userService.getProfile();
            model.addAttribute("user", currentUser);
            model.addAttribute("currentUser", currentUser);
            
            return "admin-profile";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login";
        }
    }

    @GetMapping("/admin/poll/create")
    public String createPollPage(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return "redirect:/login";
            }
            
            UserDTO currentUser = userService.getProfile();
            
            if (!currentUser.getRole().equals("POLL_ADMIN")) {
                return "redirect:/admin";
            }
            
            List<CategoryDTO> categories = categoryService.getAllCategories();
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("categories", categories != null ? categories : List.of());
            
            return "admin-create-poll";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login";
        }
    }

    @GetMapping("/admin/poll/edit/{id}")
    public String editPollPage(@PathVariable Long id, Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return "redirect:/login";
            }
            
            UserDTO currentUser = userService.getProfile();
            List<CategoryDTO> categories = categoryService.getAllCategories();
            PollDTO poll = pollService.getPollByIdForAdmin(id);
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("categories", categories != null ? categories : List.of());
            model.addAttribute("poll", poll);
            
            return "admin-edit-poll";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin";
        }
    }

    @GetMapping("/admin/users")
    public String adminUsers(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return "redirect:/login";
            }
            
            UserDTO currentUser = userService.getProfile();
            List<UserDTO> allUsers = adminService.getAllUsers();
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("users", allUsers != null ? allUsers : List.of());
            
            return "admin-users";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        SecurityContextHolder.clearContext();
        return "redirect:/login?logout=true";
    }
}
