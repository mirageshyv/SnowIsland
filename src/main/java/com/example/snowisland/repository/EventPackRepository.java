package com.example.snowisland.repository;

import com.example.snowisland.entity.EventPack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventPackRepository extends JpaRepository<EventPack, Integer> {

    Optional<EventPack> findByName(String name);

    List<EventPack> findByNameIn(Collection<String> names);

    List<EventPack> findAllByOrderBySortOrderAscIdAsc();

    List<EventPack> findByParentIdOrderBySortOrderAscIdAsc(Integer parentId);
}
