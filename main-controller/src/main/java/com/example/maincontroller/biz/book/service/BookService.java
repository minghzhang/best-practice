package com.example.maincontroller.biz.book.service;

import com.example.maincontroller.biz.book.po.Book;
import java.util.List;

public interface BookService {

    Book getBook(Long id);

    List<Book> findAllBooks();

    Book addBook(Book book);
}
