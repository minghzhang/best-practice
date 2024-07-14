package com.example.maincontroller.biz.book.service.impl;

import com.example.maincontroller.biz.book.po.Book;
import com.example.maincontroller.biz.book.repository.BookRepository;
import com.example.maincontroller.biz.book.service.BookService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;

    @Override
    public Book getBook(Long id) {
        Optional<Book> bookOptional = bookRepository.findById(id);
        int i = 9/0;
        return bookOptional.orElse(null);
    }

    @Override
    public List<Book> findAllBooks() {
        List<Book> books = bookRepository.findAll();
        return books;
    }

    @Override
    public Book addBook(Book book) {
        Book savedBook = bookRepository.save(book);
        return savedBook;
    }
}
