package com.kremnev.blog.service;

import com.kremnev.blog.model.Comment;
import com.kremnev.blog.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentServiceImpl Tests")
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Comment testComment;
    private OffsetDateTime now;

    @BeforeEach
    void setUp() {
        now = OffsetDateTime.now();
        testComment = new Comment(1L, 10L, "Test comment text", now, now);
    }

    @Test
    @DisplayName("Should return all comments for a post")
    void testGetAllByPostId() {
        List<Comment> comments = List.of(
                testComment,
                new Comment(2L, 10L, "Another comment", now, now)
        );
        when(commentRepository.findAllByPostId(10L)).thenReturn(comments);

        List<Comment> result = commentService.getAllByPostId(10L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testComment, result.get(0));
        verify(commentRepository, times(1)).findAllByPostId(10L);
    }

    @Test
    @DisplayName("Should return empty list when post has no comments")
    void testGetAllByPostIdEmpty() {
        when(commentRepository.findAllByPostId(999L)).thenReturn(List.of());

        List<Comment> result = commentService.getAllByPostId(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(commentRepository, times(1)).findAllByPostId(999L);
    }

    @Test
    @DisplayName("Should return comment when found by id")
    void testGetById() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        Optional<Comment> result = commentService.getById(1L);

        assertTrue(result.isPresent());
        assertEquals(testComment, result.get());
        assertEquals(1L, result.get().getId());
        assertEquals("Test comment text", result.get().getText());
        verify(commentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty optional when comment not found by id")
    void testGetByIdNotFound() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Comment> result = commentService.getById(999L);

        assertFalse(result.isPresent());
        verify(commentRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should create comment with valid data")
    void testCreate() {
        when(commentRepository.create(anyLong(), anyString()))
                .thenReturn(testComment);

        Comment result = commentService.create(10L, "Test comment text");

        assertNotNull(result);
        assertEquals(testComment, result);
        assertEquals(10L, result.getPostId());
        assertEquals("Test comment text", result.getText());
        verify(commentRepository, times(1)).create(10L, "Test comment text");
    }

    @Test
    @DisplayName("Should create comment with long text")
    void testCreateWithLongText() {
        String longText = "This is a very long comment ".repeat(50);
        Comment longComment = new Comment(2L, 10L, longText, now, now);
        when(commentRepository.create(anyLong(), anyString()))
                .thenReturn(longComment);

        Comment result = commentService.create(10L, longText);

        assertNotNull(result);
        assertEquals(longText, result.getText());
        verify(commentRepository, times(1)).create(10L, longText);
    }

    @Test
    @DisplayName("Should update comment when comment exists")
    void testUpdate() {
        Comment updatedComment = new Comment(1L, 10L, "Updated text", now, OffsetDateTime.now());
        when(commentRepository.update(anyLong(), anyLong(), anyString()))
                .thenReturn(Optional.of(updatedComment));

        Optional<Comment> result = commentService.update(1L, 10L, "Updated text");

        assertTrue(result.isPresent());
        assertEquals("Updated text", result.get().getText());
        verify(commentRepository, times(1)).update(1L, 10L, "Updated text");
    }

    @Test
    @DisplayName("Should return empty optional when updating non-existent comment")
    void testUpdateNonExistentComment() {
        when(commentRepository.update(anyLong(), anyLong(), anyString()))
                .thenReturn(Optional.empty());

        Optional<Comment> result = commentService.update(999L, 10L, "Text");

        assertFalse(result.isPresent());
        verify(commentRepository, times(1)).update(999L, 10L, "Text");
    }

    @Test
    @DisplayName("Should return empty optional when updating comment with wrong post id")
    void testUpdateWithWrongPostId() {
        when(commentRepository.update(1L, 999L, "Text"))
                .thenReturn(Optional.empty());

        Optional<Comment> result = commentService.update(1L, 999L, "Text");

        assertFalse(result.isPresent());
        verify(commentRepository, times(1)).update(1L, 999L, "Text");
    }

    @Test
    @DisplayName("Should delete comment when comment exists")
    void testDelete() {
        when(commentRepository.delete(1L, 10L)).thenReturn(true);

        boolean result = commentService.delete(1L, 10L);

        assertTrue(result);
        verify(commentRepository, times(1)).delete(1L, 10L);
    }

    @Test
    @DisplayName("Should return false when deleting non-existent comment")
    void testDeleteNonExistentComment() {
        when(commentRepository.delete(999L, 10L)).thenReturn(false);

        boolean result = commentService.delete(999L, 10L);

        assertFalse(result);
        verify(commentRepository, times(1)).delete(999L, 10L);
    }

    @Test
    @DisplayName("Should return false when deleting comment with wrong post id")
    void testDeleteWithWrongPostId() {
        when(commentRepository.delete(1L, 999L)).thenReturn(false);

        boolean result = commentService.delete(1L, 999L);

        assertFalse(result);
        verify(commentRepository, times(1)).delete(1L, 999L);
    }

    @Test
    @DisplayName("Should verify repository is called exactly once for each operation")
    void testRepositoryInteractions() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.create(anyLong(), anyString())).thenReturn(testComment);
        when(commentRepository.delete(1L, 10L)).thenReturn(true);

        commentService.getById(1L);
        commentService.create(10L, "Text");
        commentService.delete(1L, 10L);

        verify(commentRepository, times(1)).findById(1L);
        verify(commentRepository, times(1)).create(10L, "Text");
        verify(commentRepository, times(1)).delete(1L, 10L);
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    @DisplayName("Should handle multiple comments for same post")
    void testMultipleCommentsForSamePost() {
        List<Comment> comments = List.of(
                new Comment(1L, 10L, "First comment", now, now),
                new Comment(2L, 10L, "Second comment", now, now),
                new Comment(3L, 10L, "Third comment", now, now)
        );
        when(commentRepository.findAllByPostId(10L)).thenReturn(comments);

        List<Comment> result = commentService.getAllByPostId(10L);

        assertEquals(3, result.size());
        assertEquals(10L, result.get(0).getPostId());
        assertEquals(10L, result.get(1).getPostId());
        assertEquals(10L, result.get(2).getPostId());
        verify(commentRepository, times(1)).findAllByPostId(10L);
    }
}
