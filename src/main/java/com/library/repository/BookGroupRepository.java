package com.library.repository;
import com.library.model.BookGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface BookGroupRepository extends JpaRepository<BookGroup, Long> {
    List<BookGroup> findByStatusOrderByCreatedAtDesc(String status);
}