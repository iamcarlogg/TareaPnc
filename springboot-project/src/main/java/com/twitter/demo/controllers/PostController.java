package com.twitter.demo.controllers;

import com.twitter.demo.entities.Comments;
import com.twitter.demo.entities.Post;
import com.twitter.demo.entities.User;
import com.twitter.demo.entities.dto.CreateCommentDto;
import com.twitter.demo.entities.dto.CreatePostDto;
import com.twitter.demo.entities.dto.LikeDto;
import com.twitter.demo.repositories.CommentsRepository;
import com.twitter.demo.repositories.PostRepository;
import com.twitter.demo.repositories.UserRepository;
import com.twitter.demo.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentsRepository commentsRepository;

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody CreatePostDto dto,
                                        Principal principal) {

        String username = principal.getName();


        User user = userRepository.findUserByName(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));


        Post post = new Post();

        post.setMessage(dto.getContent());
        post.setAuthor(user);


        postRepository.save(post);

        return ResponseEntity.status(HttpStatus.CREATED).body("Post creado exitosamente");
    }

    @PostMapping("/likes")
    public ResponseEntity<Void> likePost(@RequestBody LikeDto request){
        postService.likePost(request.getUserId(), request.getPostId());
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/comments")
    public ResponseEntity<?> addComment(@RequestBody CreateCommentDto dto, Principal principal) {
        User user =
                userRepository.findUserByName(principal.getName()).orElseThrow();
        Post post = postRepository.findById(dto.getPostId()).orElseThrow();

        Comments comment = new Comments();
        comment.setAuthor(user);
        comment.setMessage(dto.getMessage());
        comment.setPost(post);

        commentsRepository.save(comment);
        return ResponseEntity.ok("Comentario agregado");
    }
    @DeleteMapping("/dislike{postId}")
    public ResponseEntity<?> dislikePost(@PathVariable UUID postId, Principal principal) {
        String username = principal.getName();

        User user = userRepository.findUserByName(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post no encontrado"));

        // Verifica si el usuario ya ha dado like
        if (post.getLikes().contains(user)) {
            post.getLikes().remove(user);
            postRepository.save(post);
            return ResponseEntity.ok("Dislike registrado");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Este usuario no había dado like");
        }
    }
}
