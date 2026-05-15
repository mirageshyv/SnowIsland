package com.example.snowisland.repository;

import com.example.snowisland.entity.CatastropheCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatastropheCardRepository extends JpaRepository<CatastropheCard, Integer> {
    List<CatastropheCard> findAllByOrderByCardNumberAsc();
    CatastropheCard findByCardNumber(Integer cardNumber);
}