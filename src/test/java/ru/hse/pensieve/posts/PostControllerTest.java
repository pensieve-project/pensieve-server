package ru.hse.pensieve.posts;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.hse.pensieve.authentication.service.JwtService;
import ru.hse.pensieve.posts.models.*;
import ru.hse.pensieve.posts.routes.PostController;
import ru.hse.pensieve.posts.service.PostService;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = PostController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreatePost() throws Exception {
        UUID themeId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        String text = "Test post";

        ClassPathResource imageResource = new ClassPathResource("images/cat.jpg");

        MockMultipartFile photoFile = new MockMultipartFile(
                "photo",
                "cat.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                imageResource.getInputStream()
        );

        PostResponse mockResponse = new PostResponse();
        mockResponse.setPostId(UUID.randomUUID());
        mockResponse.setText(text);
        mockResponse.setThemeId(themeId);
        mockResponse.setAuthorId(authorId);
        mockResponse.setTimeStamp(java.time.Instant.now());
        mockResponse.setLikesCount(0);
        mockResponse.setCommentsCount(0);

        when(postService.savePost(Mockito.any())).thenReturn(mockResponse);

        mockMvc.perform(multipart("/posts")
                        .file(photoFile)
                        .param("themeId", themeId.toString())
                        .param("authorId", authorId.toString())
                        .param("text", text))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text", is(text)))
                .andExpect(jsonPath("$.authorId", is(authorId.toString())))
                .andExpect(jsonPath("$.themeId", is(themeId.toString())));
    }

    @Test
    public void testGetPostsByAuthor() throws Exception {
        UUID authorId = UUID.randomUUID();
        PostResponse response = new PostResponse();
        response.setText("Some post");

        when(postService.getPostsByAuthor(authorId)).thenReturn(List.of(response));

        mockMvc.perform(get("/posts/by-author")
                        .param("authorId", authorId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text", is("Some post")));
    }

    @Test
    public void testGetPostsByTheme() throws Exception {
        UUID themeId = UUID.randomUUID();
        PostResponse response = new PostResponse();
        response.setText("Some post");

        when(postService.getPostsByTheme(themeId)).thenReturn(List.of(response));

        mockMvc.perform(get("/posts/by-theme")
                        .param("themeId", themeId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text", is("Some post")));
    }

    @Test
    public void testLikePost() throws Exception {
        LikeRequest request = new LikeRequest(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/posts/like")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void testUnlikePost() throws Exception {
        LikeRequest request = new LikeRequest(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/posts/unlike")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void testHasUserLikedPost() throws Exception {
        LikeRequest request = new LikeRequest(UUID.randomUUID(), UUID.randomUUID());

        when(postService.hasUserLikedPost(Mockito.any())).thenReturn(true);

        mockMvc.perform(get("/posts/liked")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void testGetLikesCount() throws Exception {
        UUID postId = UUID.randomUUID();

        when(postService.getLikesCount(postId)).thenReturn(42);

        mockMvc.perform(get("/posts/likes-count")
                        .param("postId", postId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));
    }

    @Test
    public void testComment() throws Exception {
        UUID postId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        String text = "Comment text";
        CommentRequest request = new CommentRequest(postId, authorId, text);

        CommentResponse response = new CommentResponse(postId, UUID.randomUUID(), authorId, text);

        when(postService.leaveComment(Mockito.any())).thenReturn(response);

        mockMvc.perform(post("/posts/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId", is(postId.toString())))
                .andExpect(jsonPath("$.authorId", is(authorId.toString())))
                .andExpect(jsonPath("$.text", is(text)));
    }

    @Test
    public void testComments() throws Exception {
        UUID postId = UUID.randomUUID();

        CommentResponse response = new CommentResponse(postId, UUID.randomUUID(), UUID.randomUUID(), "Some text");

        when(postService.getPostComments(postId)).thenReturn(List.of(response));

        mockMvc.perform(get("/posts/comments")
                        .param("postId", postId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].postId", is(postId.toString())))
                .andExpect(jsonPath("$[0].text", is("Some text")));
    }

    @Test
    public void testGetCommentsCount() throws Exception {
        UUID postId = UUID.randomUUID();

        when(postService.getCommentsCount(postId)).thenReturn(42);

        mockMvc.perform(get("/posts/comments-count")
                        .param("postId", postId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));
    }
}
