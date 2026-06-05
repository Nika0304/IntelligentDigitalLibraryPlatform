package com.library.repository;
import com.library.model.BookGroup;
import com.library.model.GroupMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {
    List<GroupMessage> findByGroupOrderByCreatedAtAsc(BookGroup g);
    long countByGroupAndDeletedFalse(BookGroup g);
}