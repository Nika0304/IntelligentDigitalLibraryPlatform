package com.library.repository;
import com.library.model.BookGroup;
import com.library.model.GroupMembership;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {
    Optional<GroupMembership> findByGroupAndUser(BookGroup g, User u);
    List<GroupMembership> findByUserOrderByJoinedAtDesc(User u);
    List<GroupMembership> findByGroup(BookGroup g);
    long countByGroup(BookGroup g);
}