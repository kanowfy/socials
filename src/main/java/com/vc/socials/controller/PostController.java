package com.vc.socials.controller;

import com.vc.socials.dto.CommentDto;
import com.vc.socials.dto.PostDto;
import com.vc.socials.model.Comment;
import com.vc.socials.model.Post;
import com.vc.socials.model.User;
import com.vc.socials.repository.CommentRepository;
import com.vc.socials.repository.PostRepository;
import com.vc.socials.service.CommentService;
import com.vc.socials.service.PostService;
import com.vc.socials.service.PostServiceImpl;
import com.vc.socials.service.UserService;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class PostController {
    private PostService postService;
    private UserService userService;
    private CommentService commentService;

    @Autowired
    public PostController(PostService postService, UserService userService,
                          CommentService commentService){
        this.postService = postService;
        this.userService = userService;
        this.commentService = commentService;
    }

    @GetMapping("/api/home")
    public List<PostDto> getPosts(){
        List<Post> postList = postService.getPosts();
        return post2Dto(postList);
    }

    @GetMapping("/api/post/{post_id}")
    public List<PostDto> getPost(@PathVariable("post_id") Long post_id){
        List<Post> postList = new ArrayList<>();
        Optional<Post> post = postService.getPostById(post_id);
        if (post.isEmpty()){
            return null;
        }
        postList.add(post.get());
        return post2Dto(postList);
    }

    @PostMapping("/api/post/add")
    public ResponseEntity<?> createPost(@RequestBody PostDto postDto, Principal principal){
        Optional<User> user = userService.getUserByUserName(principal.getName());
        if (user.isEmpty()) return new ResponseEntity<>("Invalid User", HttpStatus.BAD_REQUEST);
        Post post = new Post();
        post.setUser(user.get());
        post.setContent(postDto.getContent());
        post.setCreatedAt(Timestamp.from(Instant.now()));
        userService.addPost(post, user.get().getId());
        postService.savePost(post);
        return new ResponseEntity<>("Create Post Successfully", HttpStatus.OK);
    }

    @PostMapping("/api/comment/add")
    public ResponseEntity<?> createComment(@RequestBody CommentDto commentDto, Principal principal){
        Optional<User> user = userService.getUserByUserName(principal.getName());
        if (user.isEmpty()) return new ResponseEntity<>("Invalid User", HttpStatus.BAD_REQUEST);
        Optional<Post> post = postService.getPostById(commentDto.getPost_id());
        if (post.isEmpty()) return new ResponseEntity<>("Invalid PostID", HttpStatus.BAD_REQUEST);
        Comment comment = new Comment();
        comment.setPost(post.get());
        comment.setCreatedAt(Timestamp.from(Instant.now()));
        comment.setContent(commentDto.getContent());
        comment.setUser(user.get());
        postService.addComment(comment, commentDto.getPost_id());
        commentService.saveComment(comment);
        return new ResponseEntity<>("Comment successfully", HttpStatus.OK);
    }

    private List<PostDto> post2Dto(List<Post> postList){
        List<PostDto> postDtoList = new ArrayList<>();
        for (Post post : postList){
            List<CommentDto> commentDtoList = new ArrayList<>();

            for (Comment comment : post.getComments()){
                commentDtoList.add(new CommentDto(comment.getPost().getId(),
                        comment.getUser().getId(), comment.getContent()));
            }
            postDtoList.add(new PostDto(post.getId(), post.getUser().getId(), post.getContent(), commentDtoList));
        }
        return postDtoList;
    }
}
