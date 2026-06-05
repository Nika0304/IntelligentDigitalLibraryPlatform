package com.library.repository;
import com.library.model.FaqEntry;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FaqEntryRepository extends JpaRepository<FaqEntry, Long> {}