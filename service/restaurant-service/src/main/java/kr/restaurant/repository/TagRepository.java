package kr.restaurant.repository;


import kr.restaurant.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, String> {

    Optional<TagEntity> findByName(String name);
}
