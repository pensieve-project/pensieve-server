package ru.hse.pensieve.profiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.hse.pensieve.authentication.service.JwtService;
import ru.hse.pensieve.database.cassandra.models.Profile;
import ru.hse.pensieve.profiles.models.ProfileRequest;
import ru.hse.pensieve.profiles.service.ProfileService;
import ru.hse.pensieve.profiles.routes.ProfileController;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ProfileController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
public class ProfilesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void createProfile() throws Exception {
        UUID authorId = UUID.randomUUID();
        ProfileRequest request = new ProfileRequest(authorId, null, "Some description");

        mockMvc.perform(post("/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void editProfile() throws Exception {
        UUID authorId = UUID.randomUUID();
        ProfileRequest request = new ProfileRequest(authorId, null, "Some description");

        mockMvc.perform(put("/profile/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void getProfileByAuthorId() throws Exception {
        UUID authorId = UUID.randomUUID();

        Profile profile = new Profile(authorId, null, "Some description", null, null, null, null, null);

        when(profileService.getProfileByAuthorId(authorId)).thenReturn(profile);

        mockMvc.perform(get("/profile/by-authorId")
                        .param("authorId", authorId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorId", is(authorId.toString())))
                .andExpect(jsonPath("$.description", is("Some description")));
    }

    @Test
    public void getAvatarByAuthorId() throws Exception {
        UUID authorId = UUID.randomUUID();

        byte[] testPhotoBytes = new byte[10];
        new Random().nextBytes(testPhotoBytes);
        ByteBuffer photo = ByteBuffer.wrap(testPhotoBytes);

        when(profileService.getAvatarByAuthorId(authorId)).thenReturn(photo);

        String expectedBase64 = Base64.getEncoder().encodeToString(testPhotoBytes);

        mockMvc.perform(get("/profile/avatar")
                        .param("authorId", authorId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("\"" + expectedBase64 + "\""));
    }

    @Test
    public void getUsernameByAuthorId() throws Exception {
        UUID authorId = UUID.randomUUID();

        String username = "admin";

        when(profileService.getUsernameByAuthorId(authorId)).thenReturn(username);

        mockMvc.perform(get("/profile/username")
                        .param("authorId", authorId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(username));
    }
}
