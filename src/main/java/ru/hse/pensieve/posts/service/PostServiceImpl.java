package ru.hse.pensieve.posts.service;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.models.*;
import ru.hse.pensieve.database.cassandra.repositories.*;
import ru.hse.pensieve.database.redis.service.RedisService;
import ru.hse.pensieve.posts.kafka.PostEventProducer;
import ru.hse.pensieve.posts.models.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.*;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostByAuthorRepository postByAuthorRepository;

    @Autowired
    private PostByIdRepository postByIdRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostEventProducer postEventProducer;

    @Autowired
    private RedisService redisService;

    public PostResponse savePost(PostRequest request) throws BadPostException {

        byte[] photoBytes;
        if (request.getPhoto() == null || request.getPhoto().isEmpty()) {
            throw new BadPostException("Post photo is null!");
        }
        try {
            photoBytes = request.getPhoto().getBytes();
        } catch (IOException ex) {
            throw new BadPostException("Post photo is null!");
        }
        ByteBuffer photo = ByteBuffer.wrap(photoBytes);

        SortedSet<UUID> coAuthors = new TreeSet<>(request.getCoAuthors() != null ? request.getCoAuthors() : Collections.emptySet());
        UUID albumId = null;
        if (!coAuthors.isEmpty()) {
            coAuthors.add(request.getAuthorId());

            Optional<Album> album = albumRepository.findById(new AlbumKey(request.getAuthorId(), coAuthors));
            if (album.isPresent()) {
                albumId = album.get().getAlbumId();
            } else {
                albumId = UUID.randomUUID();
                for (UUID coAuthor : coAuthors) {
                    albumRepository.save(new Album(new AlbumKey(coAuthor, coAuthors), albumId, photo));
                }
            }
        }

        PostKey postKey = new PostKey(request.getThemeId(), Instant.now(), request.getAuthorId(), UUID.randomUUID());
        Post post = postRepository.save(new Post(postKey, photo, request.getText(), request.getLocation(), coAuthors, albumId, 0, 0));

        boolean isVip = profileRepository.isVip(post.getKey().getAuthorId());

        if (isVip) {
            redisService.cacheVipPost(post);
        } else {
            postEventProducer.sendPostCreated(post);
        }

        return PostMapper.fromPost(post);
    }

    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream().map(PostMapper::fromPost).toList();
    }

    public List<PostResponse> getPostsByAuthor(UUID authorId) {
        return postByAuthorRepository.findByKeyAuthorId(authorId).stream().map(PostMapper::fromPostByAuthor).toList();
    }

    public List<PostResponse> getPostsByTheme(UUID themeId) {
        return postRepository.findByKeyThemeId(themeId).stream().map(PostMapper::fromPost).toList();
    }

    public PostResponse getPostById(UUID postId) {
        return postByIdRepository.findByKeyPostId(postId).stream().map(PostMapper::fromPostById).findFirst().orElse(null);
    }

    public void likePost(LikeRequest request) {
        if (hasUserLikedPost(request)) {
            return;
        }
        List<PostById> posts = postByIdRepository.findByKeyPostId(request.getPostId());
        if (posts.isEmpty()) {
            return;
        }

        PostById post = posts.getFirst();
        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(PostMapper.postFromPostById(post));

        Profile profile = profileRepository.findByAuthorId(request.getAuthorId());
        ArrayList<UUID> likes = profile.getLikedPostsIds();
        if (likes == null) {
            likes = new ArrayList<>();
        }
        likes.add(request.getPostId());
        profile.setLikedPostsIds(likes);
        profileRepository.save(profile);
    }

    public void unlikePost(LikeRequest request) {
        if (!hasUserLikedPost(request)) {
            return;
        }
        List<PostById> posts = postByIdRepository.findByKeyPostId(request.getPostId());
        if (posts.isEmpty()) {
            return;
        }

        PostById post = posts.getFirst();
        post.setLikesCount(post.getLikesCount() - 1);
        postRepository.save(PostMapper.postFromPostById(post));

        Profile profile = profileRepository.findByAuthorId(request.getAuthorId());
        ArrayList<UUID> likes = profile.getLikedPostsIds();
        if (likes == null) {
            return;
        }
        likes.remove(request.getPostId());
        profile.setLikedPostsIds(likes);
        profileRepository.save(profile);
    }

    public Boolean hasUserLikedPost(LikeRequest request) {
        return profileRepository.hasLikedPost(request.getAuthorId(), request.getPostId());
    }

    public Integer getLikesCount(UUID postId) {
        PostById post = postByIdRepository.findByKeyPostId(postId).getFirst();
        return post.getLikesCount();
    }

    public CommentResponse leaveComment(CommentRequest request) {
        CommentKey key = new CommentKey(request.getPostId(), Uuids.timeBased());
        Comment comment = new Comment(key, request.getAuthorId(), request.getText());
        Comment newComment = commentRepository.save(comment);
        List<PostById> posts = postByIdRepository.findByKeyPostId(newComment.getKey().getPostId());
        if (posts.isEmpty()) {
            throw new PostNotFoundException("Post for comment not found");
        }

        PostById post = posts.getFirst();
        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(PostMapper.postFromPostById(post));
        return CommentMapper.fromComment(newComment);
    }

    public List<CommentResponse> getPostComments(UUID postId) {
        return commentRepository.findByKeyPostId(postId).stream().map(CommentMapper::fromComment).toList();
    }

    public Integer getCommentsCount(UUID postId) {
        PostById post = postByIdRepository.findByKeyPostId(postId).getFirst();
        return post.getCommentsCount();
    }
}
