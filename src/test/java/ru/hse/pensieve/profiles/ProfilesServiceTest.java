package ru.hse.pensieve.profiles;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import ru.hse.pensieve.database.cassandra.models.Profile;
import ru.hse.pensieve.database.cassandra.repositories.ProfileRepository;
import ru.hse.pensieve.database.postgres.repositories.UserRepository;
import ru.hse.pensieve.profiles.models.BadAvatarException;
import ru.hse.pensieve.profiles.models.ProfileRequest;
import ru.hse.pensieve.profiles.service.ProfileServiceImpl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfilesServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private final UUID authorId = UUID.randomUUID();
    private final String testDescription = "Test description";
    private final byte[] testAvatarContent = "test".getBytes();

    @Test
    void createProfile_ShouldSaveNewProfile() {
        ProfileRequest request = createProfileRequest();

        profileService.createProfile(request);

        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
        verify(profileRepository).save(profileCaptor.capture());

        Profile savedProfile = profileCaptor.getValue();
        assertEquals(authorId, savedProfile.getAuthorId());
        assertArrayEquals(testAvatarContent, savedProfile.getAvatar().array());
        assertEquals(testDescription, savedProfile.getDescription());
    }

    @Test
    void createProfile_ShouldThrowExceptionForInvalidAvatar() {
        ProfileRequest request = new ProfileRequest(
                authorId,
                new MockMultipartFile("avatar", "test.jpg", "image/jpeg", new byte[0]),
                testDescription
        );

        assertThrows(BadAvatarException.class, () -> profileService.createProfile(request));
    }

    @Test
    void editProfile_ShouldUpdateExistingProfile() {
        ProfileRequest request = createProfileRequest();

        Profile existingProfile = new Profile(authorId, ByteBuffer.wrap(new byte[0]), "Old description",
                new ArrayList<>(), new ArrayList<>(), 0, 0, false);
        when(profileRepository.findByAuthorId(authorId)).thenReturn(existingProfile);

        profileService.editProfile(request);

        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
        verify(profileRepository).save(profileCaptor.capture());

        Profile updatedProfile = profileCaptor.getValue();
        assertArrayEquals(testAvatarContent, updatedProfile.getAvatar().array());
        assertEquals(testDescription, updatedProfile.getDescription());
    }

    @Test
    void editProfile_ShouldThrowExceptionForInvalidAvatar() {
        ProfileRequest request = new ProfileRequest(
                authorId,
                new MockMultipartFile("avatar", "test.jpg", "image/jpeg", new byte[0]),
                testDescription
        );

        assertThrows(BadAvatarException.class, () -> profileService.editProfile(request));
    }

    @Test
    void getProfileByAuthorId_ShouldReturnProfile() {
        Profile expectedProfile = createProfile();
        when(profileRepository.findByAuthorId(authorId)).thenReturn(expectedProfile);

        Profile result = profileService.getProfileByAuthorId(authorId);

        assertSame(expectedProfile, result);
    }

    @Test
    void getAvatarByAuthorId_ShouldReturnAvatar() {
        ByteBuffer expectedAvatar = ByteBuffer.wrap(testAvatarContent);
        Profile profile = createProfile();
        profile.setAvatar(expectedAvatar);

        when(profileRepository.findByAuthorId(authorId)).thenReturn(profile);

        ByteBuffer result = profileService.getAvatarByAuthorId(authorId);

        assertSame(expectedAvatar, result);
    }

    @Test
    void getUsernameByAuthorId_ShouldReturnUsername() {
        String expectedUsername = "testUser";
        when(userRepository.findUsernameById(authorId)).thenReturn(expectedUsername);

        String result = profileService.getUsernameByAuthorId(authorId);

        assertEquals(expectedUsername, result);
    }

    private ProfileRequest createProfileRequest() {
        return new ProfileRequest(
                authorId,
                new MockMultipartFile("avatar", "test.jpg", "image/jpeg", testAvatarContent),
                testDescription
        );
    }

    private Profile createProfile() {
        return new Profile(
                authorId,
                null,
                "description",
                new ArrayList<>(),
                new ArrayList<>(),
                0,
                0,
                false
        );
    }
}