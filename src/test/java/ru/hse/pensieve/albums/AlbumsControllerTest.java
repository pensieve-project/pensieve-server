package ru.hse.pensieve.albums;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.hse.pensieve.authentication.service.JwtService;
import ru.hse.pensieve.albums.models.*;
import ru.hse.pensieve.albums.routes.AlbumController;
import ru.hse.pensieve.albums.service.AlbumService;
import ru.hse.pensieve.posts.models.PostResponse;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AlbumController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
public class AlbumsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlbumService albumService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getUserAlbums() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID coAuthor1 = UUID.randomUUID();
        UUID coAuthor2 = UUID.randomUUID();
        UUID coAuthor3 = UUID.randomUUID();
        Set<UUID> coAuthors = Set.of(coAuthor1, coAuthor2, coAuthor3);

        UUID albumId = UUID.randomUUID();

        AlbumResponse mockResponse = new AlbumResponse();
        mockResponse.setUserId(userId);
        mockResponse.setCoAuthors(coAuthors);
        mockResponse.setAlbumId(albumId);

        when(albumService.getUserAlbums(Mockito.any())).thenReturn(List.of(mockResponse));

        mockMvc.perform(get("/albums")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId", is(userId.toString())))
                .andExpect(jsonPath("$[0].albumId", is(albumId.toString())));
    }

    @Test
    public void getAlbumPosts() throws Exception {
        UUID albumId = UUID.randomUUID();

        PostResponse post = new PostResponse();
        post.setText("Some post");

        when(albumService.getAlbumPosts(albumId)).thenReturn(List.of(post));

        mockMvc.perform(get("/albums/posts")
                        .param("albumId", albumId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text", is("Some post")));
    }
}
