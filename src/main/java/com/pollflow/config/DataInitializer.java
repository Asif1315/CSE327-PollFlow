package com.pollflow.config;

import com.pollflow.entity.*;
import com.pollflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PollRepository pollRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    @Override
    public void run(String... args) {
        initializeCategories();
        initializeAdminUsers();
        initializePolls();
    }

    private void initializeCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> categories = Arrays.asList(
                Category.builder().name("Politics").icon("politics").displayOrder(1).build(),
                Category.builder().name("Sports").icon("sports").displayOrder(2).build(),
                Category.builder().name("Entertainment").icon("entertainment").displayOrder(3).build(),
                Category.builder().name("Technology").icon("technology").displayOrder(4).build(),
                Category.builder().name("Business").icon("business").displayOrder(5).build(),
                Category.builder().name("World News").icon("world").displayOrder(6).build(),
                Category.builder().name("National").icon("national").displayOrder(7).build(),
                Category.builder().name("Science").icon("science").displayOrder(8).build(),
                Category.builder().name("Health").icon("health").displayOrder(9).build(),
                Category.builder().name("Environment").icon("environment").displayOrder(10).build(),
                Category.builder().name("Education").icon("education").displayOrder(11).build(),
                Category.builder().name("Lifestyle").icon("lifestyle").displayOrder(12).build()
            );
            
            categoryRepository.saveAll(categories);
            System.out.println("Initialized 12 poll categories");
        }
    }

    private void initializeAdminUsers() {
        String encodedPassword = passwordEncoder.encode("admin123");
        
        User pollAdmin = userRepository.findByEmail("polladmin@pollflow.com").orElse(null);
        if (pollAdmin == null) {
            pollAdmin = User.builder()
                    .email("polladmin@pollflow.com")
                    .password(encodedPassword)
                    .fullName("Poll Admin")
                    .role(User.Role.POLL_ADMIN)
                    .status(User.UserStatus.APPROVED)
                    .build();
            userRepository.save(pollAdmin);
            System.out.println("Created Poll Admin");
        } else {
            pollAdmin.setPassword(encodedPassword);
            pollAdmin.setRole(User.Role.POLL_ADMIN);
            pollAdmin.setStatus(User.UserStatus.APPROVED);
            userRepository.save(pollAdmin);
            System.out.println("Reset Poll Admin password");
        }

        User verificationAdmin = userRepository.findByEmail("verification@pollflow.com").orElse(null);
        if (verificationAdmin == null) {
            verificationAdmin = User.builder()
                    .email("verification@pollflow.com")
                    .password(encodedPassword)
                    .fullName("Verification Admin")
                    .role(User.Role.VERIFICATION_ADMIN)
                    .status(User.UserStatus.APPROVED)
                    .build();
            userRepository.save(verificationAdmin);
            System.out.println("Created Verification Admin");
        } else {
            verificationAdmin.setPassword(encodedPassword);
            verificationAdmin.setRole(User.Role.VERIFICATION_ADMIN);
            verificationAdmin.setStatus(User.UserStatus.APPROVED);
            userRepository.save(verificationAdmin);
            System.out.println("Reset Verification Admin password");
        }
    }

    private void initializePolls() {
        if (pollRepository.count() > 0) {
            System.out.println("Polls already exist, skipping initialization");
            return;
        }

        List<Category> categories = categoryRepository.findAll();
        User pollAdmin = userRepository.findByEmail("polladmin@pollflow.com").orElse(null);
        
        if (pollAdmin == null) {
            System.out.println("Poll admin not found, skipping poll initialization");
            return;
        }

        List<PollData> pollDataList = getPollData();

        for (Category category : categories) {
            int pollsForCategory = 0;
            for (PollData pollData : pollDataList) {
                if (pollsForCategory >= 5) break;
                
                if (pollData.category.equalsIgnoreCase(category.getName()) || 
                    category.getName().equalsIgnoreCase("Politics") && pollsForCategory < 5) {
                    
                    Poll poll = Poll.builder()
                            .title(pollData.title)
                            .description(pollData.description)
                            .category(category)
                            .createdBy(pollAdmin)
                            .pollType(pollData.isTimeBased ? Poll.PollType.TIME_BASED : Poll.PollType.OPEN)
                            .endTime(pollData.isTimeBased ? LocalDateTime.now().plusDays(pollData.daysUntilEnd) : null)
                            .build();
                    
                    poll = pollRepository.save(poll);
                    
                    List<PollOption> options = new ArrayList<>();
                    for (String optText : pollData.options) {
                        PollOption option = PollOption.builder()
                                .poll(poll)
                                .optionText(optText)
                                .voteCount(0)
                                .build();
                        options.add(option);
                    }
                    poll.setOptions(options);
                    pollRepository.save(poll);
                    
                    pollsForCategory++;
                }
            }
        }
        
        System.out.println("Initialized sample polls");
    }

    private List<PollData> getPollData() {
        return Arrays.asList(
            // Politics (5 polls)
            new PollData("Politics", "Who will win the next election?", "Vote for your predicted winner", 
                Arrays.asList("Party A", "Party B", "Party C", "Undecided"), false, 0),
            new PollData("Politics", "Should voting age be lowered to 16?", "Express your opinion", 
                Arrays.asList("Yes", "No", "Not sure"), true, 7),
            new PollData("Politics", "Best political reform?", "Choose the most important reform", 
                Arrays.asList("Election reform", "Campaign finance", "Term limits", "Other"), true, 14),
            new PollData("Politics", "Approval of current government?", "Rate the government performance", 
                Arrays.asList("Excellent", "Good", "Fair", "Poor"), false, 0),
            new PollData("Politics", "Preferred voting system?", "Which system do you prefer", 
                Arrays.asList("First-past-the-post", "Proportional", "Ranked choice"), true, 21),
            
            // Sports (5 polls)
            new PollData("Sports", "Best football team?", "Vote for the top team", 
                Arrays.asList("Team A", "Team B", "Team C", "Team D"), false, 0),
            new PollData("Sports", "World Cup 2026 winner?", "Predict the winner", 
                Arrays.asList("Brazil", "Argentina", "France", "Germany", "Other"), true, 30),
            new PollData("Sports", "Best basketball player?", "Who is the GOAT?", 
                Arrays.asList("Michael Jordan", "LeBron James", "Kobe Bryant", "Stephen Curry"), false, 0),
            new PollData("Sports", "Should Olympics be more frequent?", "Your opinion on frequency", 
                Arrays.asList("Yes, every 2 years", "Keep every 4 years", "Every 8 years"), true, 10),
            new PollData("Sports", "Favorite Olympic sport?", "Which sport do you enjoy most", 
                Arrays.asList("Football", "Basketball", "Swimming", "Athletics"), false, 0),
            
            // Entertainment (5 polls)
            new PollData("Entertainment", "Best movie of 2025?", "Vote for the best film", 
                Arrays.asList("Movie A", "Movie B", "Movie C", "Movie D"), false, 0),
            new PollData("Entertainment", "Best streaming service?", "Which platform is best", 
                Arrays.asList("Netflix", "Amazon Prime", "Disney+", "HBO Max"), true, 7),
            new PollData("Entertainment", "Favorite music genre?", "What do you listen to most", 
                Arrays.asList("Pop", "Rock", "Hip-hop", "Classical", "Electronic"), false, 0),
            new PollData("Entertainment", "Best TV series this year?", "Top series vote", 
                Arrays.asList("Series A", "Series B", "Series C", "None"), true, 14),
            new PollData("Entertainment", "Should theaters survive?", "Future of movie theaters", 
                Arrays.asList("Yes, definitely", "Only premium", "No, streaming is future"), false, 0),
            
            // Technology (5 polls)
            new PollData("Technology", "Best smartphone brand?", "Your preferred brand", 
                Arrays.asList("Apple", "Samsung", "Google", "OnePlus"), false, 0),
            new PollData("Technology", "AI: Threat or opportunity?", "Your view on AI", 
                Arrays.asList("More opportunity", "More threat", "Balanced view"), true, 14),
            new PollData("Technology", "Favorite social media platform?", "Most used app", 
                Arrays.asList("Instagram", "Twitter/X", "TikTok", "Facebook"), false, 0),
            new PollData("Technology", "Remote work vs office?", "Preferred work style", 
                Arrays.asList("Remote", "Office", "Hybrid"), true, 7),
            new PollData("Technology", "Best programming language 2025?", "For aspiring developers", 
                Arrays.asList("Python", "JavaScript", "Java", "Rust", "Go"), false, 0),
            
            // Business (5 polls)
            new PollData("Business", "Best startup sector?", "Where would you invest", 
                Arrays.asList("AI/ML", "Healthcare", "Fintech", "Green Energy"), false, 0),
            new PollData("Business", "Economic outlook 2026?", "Your prediction", 
                Arrays.asList("Growth", "Stable", "Recession", "Unsure"), true, 21),
            new PollData("Business", "Preferred work schedule?", "What do you prefer", 
                Arrays.asList("9-5", "Flexible hours", "Shift work", "Freelance"), false, 0),
            new PollData("Business", "Best investment in 2025?", "Where to put money", 
                Arrays.asList("Stocks", "Real Estate", "Crypto", "Bonds"), true, 10),
            new PollData("Business", "Startup or corporate?", "Better career path", 
                Arrays.asList("Startup", "Corporate", "No preference"), false, 0),
            
            // World News (5 polls)
            new PollData("World News", "Most important global issue?", "Vote for the biggest concern", 
                Arrays.asList("Climate Change", "War/Conflict", "Pandemic", "Poverty"), false, 0),
            new PollData("World News", "Best global leader?", "Your opinion", 
                Arrays.asList("Leader A", "Leader B", "Leader C", "None"), true, 7),
            new PollData("World News", "Future of international travel?", "Prediction", 
                Arrays.asList("Fully recovered", "Partial recovery", "Restricted"), false, 0),
            new PollData("World News", "Most peaceful region?", "Where is safest", 
                Arrays.asList("Europe", "Asia", "Americas", "Africa"), true, 14),
            new PollData("World News", "Global cooperation level?", "Current state", 
                Arrays.asList("Improving", "Declining", "Stagnant"), false, 0),
            
            // National (5 polls)
            new PollData("National", "National priority?", "What should be #1", 
                Arrays.asList("Economy", "Healthcare", "Education", "Security"), false, 0),
            new PollData("National", "Infrastructure spending?", "Your view", 
                Arrays.asList("Increase", "Maintain", "Decrease"), true, 7),
            new PollData("National", "Best national policy?", "Most effective", 
                Arrays.asList("Policy A", "Policy B", "Policy C"), false, 0),
            new PollData("National", "Public service satisfaction?", "Rate services", 
                Arrays.asList("Very satisfied", "Satisfied", "Dissatisfied"), true, 14),
            new PollData("National", "National symbol?", "Most representative", 
                Arrays.asList("Flag", "Anthem", "Landmark", "Other"), false, 0),
            
            // Science (5 polls)
            new PollData("Science", "Mars colonization?", "Your opinion", 
                Arrays.asList("Essential", "Nice to have", "Not worth it"), true, 30),
            new PollData("Science", "Greatest scientific achievement?", "Vote for top", 
                Arrays.asList("Internet", "Vaccines", "Electricity", "AI"), false, 0),
            new PollData("Science", "Climate change solution?", "Best approach", 
                Arrays.asList("Renewable energy", "Carbon capture", "Nuclear", "Conservation"), true, 21),
            new PollData("Science", "Space exploration priority?", "Where to focus", 
                Arrays.asList("Mars", "Moon", "Asteroids", "Deep space"), false, 0),
            new PollData("Science", "Favorite science field?", "Most interesting", 
                Arrays.asList("Physics", "Biology", "Chemistry", "Astronomy"), false, 0),
            
            // Health (5 polls)
            new PollData("Health", "Best exercise routine?", "What works for you", 
                Arrays.asList("Cardio", "Strength", "Yoga", "Mixed"), false, 0),
            new PollData("Health", "Healthcare system?", "Your view", 
                Arrays.asList("Government provided", "Private", "Mixed"), true, 14),
            new PollData("Health", "Mental health priority?", "Important issue", 
                Arrays.asList("Very important", "Important", "Less important"), false, 0),
            new PollData("Health", "Diet preference?", "What's healthiest", 
                Arrays.asList("Vegan", "Keto", "Mediterranean", "Balanced"), true, 7),
            new PollData("Health", "Sleep importance?", "How many hours", 
                Arrays.asList("6-7 hours", "7-8 hours", "8+ hours"), false, 0),
            
            // Environment (5 polls)
            new PollData("Environment", "Environmental priority?", "Most urgent", 
                Arrays.asList("Air quality", "Water quality", "Biodiversity", "Waste"), false, 0),
            new PollData("Environment", "Electric vehicles?", "Your view", 
                Arrays.asList("Future is electric", "Hybrid better", "Keep gas"), true, 14),
            new PollData("Environment", "Recycling habits?", "Do you recycle", 
                Arrays.asList("Always", "Sometimes", "Never"), false, 0),
            new PollData("Environment", "Best green energy?", "Most sustainable", 
                Arrays.asList("Solar", "Wind", "Hydro", "Nuclear"), true, 21),
            new PollData("Environment", "Climate action urgency?", "How urgent", 
                Arrays.asList("Immediate", "Gradual", "Not urgent"), false, 0),
            
            // Education (5 polls)
            new PollData("Education", "Online vs in-person?", "Preferred format", 
                Arrays.asList("In-person", "Online", "Hybrid"), false, 0),
            new PollData("Education", "Most valuable skill?", "For future", 
                Arrays.asList("Coding", "Critical thinking", "Communication", "Data analysis"), true, 7),
            new PollData("Education", "Education funding priority?", "Where to invest", 
                Arrays.asList("K-12", "Higher Ed", "Vocational", "Tech training"), false, 0),
            new PollData("Education", "AI in classrooms?", "Your opinion", 
                Arrays.asList("Embrace it", "Limited use", "Avoid it"), true, 14),
            new PollData("Education", "Student loan forgiveness?", "Policy view", 
                Arrays.asList("Support", "Oppose", "Modify"), false, 0),
            
            // Lifestyle (5 polls)
            new PollData("Lifestyle", "Morning person or night owl?", "Your rhythm", 
                Arrays.asList("Morning", "Night", "Neither"), false, 0),
            new PollData("Lifestyle", "Vacation preference?", "Ideal trip", 
                Arrays.asList("Beach", "Mountain", "City", "Adventure"), true, 7),
            new PollData("Lifestyle", "Minimalism vs maximalism?", "Living style", 
                Arrays.asList("Minimalist", "Maximalist", "Balanced"), false, 0),
            new PollData("Lifestyle", "Cooking frequency?", "How often", 
                Arrays.asList("Daily", "Few times week", "Occasionally", "Never"), true, 14),
            new PollData("Lifestyle", "Social media impact?", "Effect on life", 
                Arrays.asList("Positive", "Negative", "Neutral"), false, 0)
        );
    }

    private static class PollData {
        String category;
        String title;
        String description;
        List<String> options;
        boolean isTimeBased;
        int daysUntilEnd;

        PollData(String category, String title, String description, List<String> options, boolean isTimeBased, int daysUntilEnd) {
            this.category = category;
            this.title = title;
            this.description = description;
            this.options = options;
            this.isTimeBased = isTimeBased;
            this.daysUntilEnd = daysUntilEnd;
        }
    }
}
