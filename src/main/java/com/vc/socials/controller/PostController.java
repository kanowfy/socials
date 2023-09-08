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

    private NotificationService notificationService;
    private FriendshipService friendshipService;

    public PostController(PostService postService, UserService userService,
            CommentService commentService, NotificationService notificationService,
            KafkaProducerService producerService, FriendshipService friendshipService) {
        this.postService = postService;
        this.userService = userService;
        this.commentService = commentService;
        this.notificationService = notificationService;
        this.producerService = producerService;
        this.friendshipService = friendshipService;
    }

    @Operation(summary = "Get all posts")
    @ApiResponse(responseCode = "200", description = "Operation successful")
    @GetMapping("/api/home")
    public ResponseEntity<?> getPosts() {
        List<Post> postList = postService.getPosts();
        List<PostDto> postDtoList = post2Dto(postList);
        return new ResponseEntity<>(postDtoList, HttpStatus.OK);
    }

    @GetMapping("/api/post/{post_id}")
    public ResponseEntity<?> getPost(@PathVariable("post_id") Long post_id) {
        List<Post> postList = new ArrayList<>();
        Optional<Post> post = postService.getPostById(post_id);
        if (post.isEmpty()) {
            return null;
        }
        postList.add(post.get());
        List<PostDto> postDtoList = post2Dto(postList);
        return new ResponseEntity<>(postDtoList, HttpStatus.OK);
    }

    @Operation(summary = "Create a new post")
    @ApiResponses(value = { @ApiResponse(responseCode = "400", description = "Invalid user"),
            @ApiResponse(responseCode = "201", description = "Post created successfully") })
    @PostMapping("/api/post/add")
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
    @PostMapping("/api/comment/add")
    public ResponseEntity<?> createComment(@RequestBody CommentDto commentDto, Principal principal) {
        Optional<User> user = userService.getUserByUserName(principal.getName());
        if (user.isEmpty())
            return new ResponseEntity<>("Invalid User", HttpStatus.BAD_REQUEST);
        Optional<Post> post = postService.getPostById(commentDto.getPost_id());
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
        postService.addComment(comment, commentDto.getPost_id());
        commentService.saveComment(comment);

        NotificationDto notification = new NotificationDto();
        notification.setNotificationType(NotificationType.COMMENT);
        notification.setComment_id(comment.getId());
        notification.setCreated_at(Timestamp.from(Instant.now()));
        notification.setUser_id(post.get().getUser().getId());
        notification.set_read(false);
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
                commentDto.setComment_id(comment.getId());
                commentDto.setPost_id(comment.getPost().getId());
                commentDto.setUser_id(comment.getUser().getId());
                commentDto.setContent(comment.getContent());
                commentDto.setCreated_at(comment.getCreatedAt());
                commentDtoList.add(commentDto);
            }
            postDtoList.add(new PostDto(post.getId(), post.getUser().getId(), post.getContent(), commentDtoList,
                    post.getCreatedAt()));
        }
        return postDtoList;
    }
}
