package ru.hse.pensieve;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.sql.DataSource;

import jakarta.xml.bind.DatatypeConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.hse.pensieve.authentication.models.RegisterRequest;
import ru.hse.pensieve.authentication.service.AuthenticationService;
import ru.hse.pensieve.feed.service.FeedService;
import ru.hse.pensieve.models.ThemeImport;
import ru.hse.pensieve.posts.models.*;
import ru.hse.pensieve.posts.service.PostService;
import ru.hse.pensieve.profiles.models.ProfileRequest;
import ru.hse.pensieve.profiles.service.ProfileService;
import ru.hse.pensieve.subscriptions.models.SubscriptionRequest;
import ru.hse.pensieve.subscriptions.service.SubscriptionsService;
import ru.hse.pensieve.themes.models.ThemeRequest;
import ru.hse.pensieve.themes.service.ThemeService;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@SpringBootTest
@Disabled
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class TestDataInitializer {

    @Autowired
    private FeedService feedService;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private ThemeService themeService;
    @Autowired
    private PostService postService;
    @Autowired
    private SubscriptionsService subscriptionsService;
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CassandraTemplate cassandraTemplate;

    private UUID adminId;
    private final List<UUID> authors = new ArrayList<>();
    private List<UUID> themes = new ArrayList<>();
    private List<PostResponse> posts = new ArrayList<>();
    private final Random random = new Random();
    Map<UUID, String> themeImagePaths = new HashMap<>();

    @BeforeEach
    void setup() throws IOException {
        adminId = authenticationService
                .register(new RegisterRequest("admin", "admin@admin.com", hashWithSha256("admin123")))
                .join()
                .getId();
        authors.add(adminId);

        for (int i = 0; i < 30; i++) {
            UUID userId = authenticationService
                    .register(new RegisterRequest("user" + i, "user" + i + "@test.com", hashWithSha256("password" + i)))
                    .join()
                    .getId();
            authors.add(userId);
        }

        // profiles
        for (UUID userId : authors) {
            byte[] image = loadImage("cat.jpg");
            MultipartFile imageFile = toMultipartFile("cat.jpg", image);
            profileService.createProfile(new ProfileRequest(userId, imageFile, "description"));
        }

        // themes
        themes = createThemes();

        // posts
        posts = createPosts(themes);

        // subscriptions
        createSocialInteractions();
    }

    private List<UUID> createThemes() throws IOException {
        Path jsonPath = Paths.get("src/test/resources/themes.json");
        String content = Files.readString(jsonPath);
        ObjectMapper objectMapper = new ObjectMapper();
        List<ThemeImport> themeDtos = objectMapper.readValue(content, new TypeReference<>() {});

        List<UUID> themeIds = new ArrayList<>();
        for (ThemeImport dto : themeDtos) {
            ThemeRequest request = new ThemeRequest(authors.get(random.nextInt(authors.size())), dto.getTitle());
            UUID themeId = themeService.createTheme(request).getThemeId();
            themeImagePaths.put(themeId, dto.getImagePath());
            themeIds.add(themeId);
        }
        return themeIds;
    }

    private List<PostResponse> createPosts(List<UUID> themes) throws IOException {
        List<PostResponse> posts = new ArrayList<>();
        for (UUID themeId : themes) {
            for (int i = 0; i < 3; i++) {
                UUID authorId = authors.get(random.nextInt(authors.size()));
                String imagePath = themeImagePaths.get(themeId);
                byte[] image = loadImage(imagePath);
                MultipartFile imageFile = toMultipartFile(imagePath, image);
                PostRequest request = new PostRequest("Post content " + i, imageFile, authorId, themeId);
                PostResponse post = postService.savePost(request);
                posts.add(post);
            }
        }
        return posts;
    }

    private void createSocialInteractions() {
        // лайки
        for (int i = 0; i < 100; i++) {
            UUID userId = authors.get(random.nextInt(authors.size()));
            UUID postId = posts.get(random.nextInt(posts.size())).getPostId();
            postService.likePost(new LikeRequest(userId, postId));
        }

        // комментарии
        for (int i = 0; i < 100; i++) {
            UUID userId = authors.get(random.nextInt(authors.size()));
            UUID postId = posts.get(random.nextInt(posts.size())).getPostId();
            postService.leaveComment(new CommentRequest(postId, userId, "Some comment"));
        }

        // подписки
        for (int i = 0; i < 200; i++) {
            UUID subscriber = authors.get(random.nextInt(authors.size()));
            UUID target = authors.get(random.nextInt(authors.size()));
            if (!subscriber.equals(target)) {
                subscriptionsService.subscribe(new SubscriptionRequest(subscriber, target));
            }
        }
    }

    private byte[] loadImage(String filename) {
        try {
            return Files.readAllBytes(
                    Paths.get("src/test/resources/images/" + filename)
            );
        } catch (IOException e) {
            throw new RuntimeException("Error loading test image", e);
        }
    }

    private MultipartFile toMultipartFile(String filename, byte[] content) {
        return new MockMultipartFile("file", filename, "image/jpeg", content);
    }

    private String hashWithSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return DatatypeConverter.printHexBinary(hashBytes).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    @Test
    void dummy() {
        Assertions.assertTrue(true);
    }
}