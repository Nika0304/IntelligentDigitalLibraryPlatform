package com.library.repository;
import com.library.model.ChatQuestion;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ChatQuestionRepository extends JpaRepository<ChatQuestion, Long> {
    List<ChatQuestion> findByUserOrderByCreatedAtDesc(User user);
    List<ChatQuestion> findByStatusOrderByCreatedAtDesc(String status);
}