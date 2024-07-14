package com.example.maincontroller.biz.book.repository;

import com.example.maincontroller.biz.book.po.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

}
