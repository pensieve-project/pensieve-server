package ru.hse.pensieve.posts;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import ru.hse.pensieve.database.cassandra.models.*;
import ru.hse.pensieve.database.cassandra.repositories.*;
import ru.hse.pensieve.database.redis.service.RedisService;
import ru.hse.pensieve.posts.kafka.PostEventProducer;
import ru.hse.pensieve.posts.models.*;
import ru.hse.pensieve.posts.service.PostServiceImpl;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostByIdRepository postByIdRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostEventProducer postEventProducer;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private PostServiceImpl postService;

    private final UUID authorId = UUID.randomUUID();
    private final UUID postId = UUID.randomUUID();
    private final Instant now = Instant.now();

    @Test
    void savePost_NoVip() {
        PostRequest request = createPostRequest();

        Post savedPost = createPost();
        when(postRepository.save(any())).thenReturn(savedPost);

        when(profileRepository.isVip(authorId)).thenReturn(true);

        PostResponse result = postService.savePost(request);

        verify(postRepository).save(any(Post.class));
        verify(redisService).cacheVipPost(any(Post.class));
        verify(postEventProducer, never()).sendPostCreated(any());
        assertNotNull(result);
    }

    @Test
    void savePost_Vip() {
        PostRequest request = createPostRequest();

        Post savedPost = createPost();
        when(postRepository.save(any())).thenReturn(savedPost);

        when(profileRepository.isVip(authorId)).thenReturn(false);

        PostResponse result = postService.savePost(request);

        verify(postRepository).save(any(Post.class));
        verify(redisService, never()).cacheVipPost(any());
        verify(postEventProducer).sendPostCreated(any());
        assertNotNull(result);
    }

    @Test
    void likePost_ShouldIncrementLikesCount() {
        LikeRequest request = new LikeRequest(authorId, postId);

        PostById post = createPostById();
        when(postByIdRepository.findByKeyPostId(postId)).thenReturn(List.of(post));

        Profile profile = createProfile();
        when(profileRepository.findByAuthorId(authorId)).thenReturn(profile);
        when(profileRepository.hasLikedPost(authorId, postId)).thenReturn(false);

        assertFalse(postService.hasUserLikedPost(request));

        postService.likePost(request);

        assertEquals(1, post.getLikesCount());
        verify(postRepository).save(any(Post.class));
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void likePost_ShouldNotIncrementLikesCount() {
        LikeRequest request = new LikeRequest(authorId, postId);

        when(profileRepository.hasLikedPost(authorId, postId)).thenReturn(true);

        assertTrue(postService.hasUserLikedPost(request));

        postService.likePost(request);

        verify(postRepository, never()).save(any(Post.class));
        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    void unlikePost_ShouldDecrementLikesCount() {
        LikeRequest request = new LikeRequest(authorId, postId);

        PostById post = createPostById();
        post.setLikesCount(1);
        when(postByIdRepository.findByKeyPostId(postId)).thenReturn(List.of(post));

        Profile profile = createProfile();
        profile.setLikedPostsIds(new ArrayList<>(List.of(postId)));
        when(profileRepository.findByAuthorId(authorId)).thenReturn(profile);
        when(profileRepository.hasLikedPost(authorId, postId)).thenReturn(true);

        assertTrue(postService.hasUserLikedPost(request));

        postService.unlikePost(request);

        assertEquals(0, post.getLikesCount());
        verify(postRepository).save(any(Post.class));
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void unlikePost_ShouldNotDecrementLikesCount() {
        LikeRequest request = new LikeRequest(authorId, postId);

        when(profileRepository.hasLikedPost(authorId, postId)).thenReturn(false);

        assertFalse(postService.hasUserLikedPost(request));

        postService.unlikePost(request);

        verify(postRepository, never()).save(any(Post.class));
        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    void leaveComment_ShouldIncrementCommentsCount() {
        CommentRequest request = createCommentRequest();
        
        PostById post = createPostById();
        when(postByIdRepository.findByKeyPostId(postId)).thenReturn(List.of(post));

        Comment savedComment = createComment();
        when(commentRepository.save(any())).thenReturn(savedComment);

        CommentResponse result = postService.leaveComment(request);

        assertEquals(1, post.getCommentsCount());
        assertNotNull(result);
        assertNotNull(result.getText());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void getPostComments_ShouldReturnAllComments() {
        Comment comment = createComment();
        when(commentRepository.findByKeyPostId(postId)).thenReturn(List.of(comment, comment, comment));

        List<CommentResponse> result = postService.getPostComments(postId);

        assertEquals(3, result.size());
        assertEquals("Test comment", result.get(0).getText());
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

    private PostRequest createPostRequest() {
        return new PostRequest(
                "Test post",
                new MockMultipartFile("photo", "test.jpg", "image/jpeg", "content".getBytes()),
                null,
                authorId,
                UUID.randomUUID(),
                null
        );
    }

    private Post createPost() {
        PostKey key = new PostKey(UUID.randomUUID(), now, authorId, postId);
        return new Post(key, ByteBuffer.wrap(new byte[0]), "Test", null, Set.of(), null, 0, 0);
    }

    private PostById createPostById() {
        PostByIdKey key = new PostByIdKey(postId, now, authorId, UUID.randomUUID());
        return new PostById(key, ByteBuffer.wrap(new byte[0]), "Test", null, null, UUID.randomUUID(), 0, 0);
    }

    private CommentRequest createCommentRequest() {
        return new CommentRequest(postId, authorId, "Test comment");
    }

    private Comment createComment() {
        return new Comment(new CommentKey(postId, UUID.randomUUID()), authorId, "Test comment");
    }
}