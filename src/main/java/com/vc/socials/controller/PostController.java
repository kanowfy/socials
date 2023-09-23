package com.vc.socials.controller;

import com.vc.socials.dto.CommentDto;
import com.vc.socials.dto.NotificationDto;
import com.vc.socials.dto.PostDto;
import com.vc.socials.model.*;

import com.vc.socials.service.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Tag(name = "post", description = "post API")
@RestController
public class PostController {
    private PostService postService;
    private UserService userService;
    private CommentService commentService;

    private KafkaProducerService producerService;
    private FriendshipService friendshipService;

    public PostController(PostService postService, UserService userService,
            CommentService commentService,
            KafkaProducerService producerService, FriendshipService friendshipService) {
        this.postService = postService;
        this.userService = userService;
        this.commentService = commentService;
        this.producerService = producerService;
        this.friendshipService = friendshipService;
    }

    @Operation(summary = "Get all posts")
    @ApiResponse(responseCode = "200", description = "Operation successful")
    @GetMapping("/api/posts")
    public ResponseEntity<?> getPosts() {
        List<Post> postList = postService.getPosts();
        List<PostDto> postDtoList = post2Dto(postList);
        return new ResponseEntity<>(postDtoList, HttpStatus.OK);
    }

    @Operation(summary = "Get a post by ID")
    @ApiResponses(value = { @ApiResponse(responseCode = "404", description = "Post not found"),
            @ApiResponse(responseCode = "200", description = "Operation successful") })
    @GetMapping("/api/posts/{id}")
    public ResponseEntity<?> getPost(@PathVariable("id") Long postId) {
        List<Post> postList = new ArrayList<>();
        Optional<Post> post = postService.getPostById(postId);
        if (post.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        postList.add(post.get());
        List<PostDto> postDtoList = post2Dto(postList);
        return new ResponseEntity<>(postDtoList, HttpStatus.OK);
    }

    @Operation(summary = "Create a new post")
    @ApiResponses(value = { @ApiResponse(responseCode = "400", description = "Invalid user"),
            @ApiResponse(responseCode = "201", description = "Post created successfully") })
    @PostMapping("/api/posts")
    public ResponseEntity<?> createPost(@RequestBody PostDto postDto, Principal principal) {
        Optional<User> user = userService.getUserByUserName(principal.getName());
        if (user.isEmpty())
            return new ResponseEntity<>("Invalid User", HttpStatus.BAD_REQUEST);
        Post post = new Post();
        post.setUser(user.get());
        post.setContent(postDto.getContent());
        post.setCreatedAt(Timestamp.from(Instant.now()));
        userService.addPost(post, user.get().getId());
        postService.savePost(post);
        return new ResponseEntity<>("Create Post Successfully", HttpStatus.CREATED);
    }

    @Operation(summary = "Add new comment to an existing post")
    @ApiResponses(value = { @ApiResponse(responseCode = "400", description = "Invalid post ID"),
            @ApiResponse(responseCode = "403", description = "Can not comment on posts of a user that is not a friend"),
            @ApiResponse(responseCode = "201", description = "Comment added successfully") })
    @PostMapping("/api/comments")
    public ResponseEntity<?> createComment(@RequestBody CommentDto commentDto, Principal principal) {
        Optional<User> user = userService.getUserByUserName(principal.getName());
        if (user.isEmpty())
            return new ResponseEntity<>("Invalid User", HttpStatus.BAD_REQUEST);
        Optional<Post> post = postService.getPostById(commentDto.getPostId());
        if (post.isEmpty())
            return new ResponseEntity<>("Invalid PostID", HttpStatus.BAD_REQUEST);
        Boolean is_friend = friendshipService.checkFriendship(user.get(), post.get().getUser());
        if (!is_friend)
            return new ResponseEntity<>("Only Friends can comment on this post", HttpStatus.FORBIDDEN);

        Comment comment = new Comment();
        comment.setPost(post.get());
        comment.setCreatedAt(Timestamp.from(Instant.now()));
        comment.setContent(commentDto.getContent());
        comment.setUser(user.get());
        postService.addComment(comment, commentDto.getPostId());
        commentService.saveComment(comment);

        NotificationDto notification = new NotificationDto();
        notification.setNotificationType(NotificationType.COMMENT);
        notification.setCommentId(comment.getId());
        notification.setCreatedAt(Timestamp.from(Instant.now()));
        notification.setUserId(post.get().getUser().getId());
        notification.setRead(false);
        // notificationService.saveNotification(notification);
        // send to kafka
        producerService.sendNotification(notification);
        return new ResponseEntity<>("Comment successfully", HttpStatus.CREATED);
    }

    private List<PostDto> post2Dto(List<Post> postList) {
        List<PostDto> postDtoList = new ArrayList<>();
        for (Post post : postList) {
            List<CommentDto> commentDtoList = new ArrayList<>();

            for (Comment comment : post.getComments()) {
                CommentDto commentDto = new CommentDto();
                commentDto.setCommentId(comment.getId());
                commentDto.setPostId(comment.getPost().getId());
                commentDto.setUserId(comment.getUser().getId());
                commentDto.setContent(comment.getContent());
                commentDto.setCreatedAt(comment.getCreatedAt());
                commentDtoList.add(commentDto);
            }
            postDtoList.add(new PostDto(post.getId(), post.getUser().getId(), post.getContent(), commentDtoList,
                    post.getCreatedAt()));
        }
        return postDtoList;
    }
}
