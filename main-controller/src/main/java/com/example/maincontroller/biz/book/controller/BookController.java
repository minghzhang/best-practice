package com.example.maincontroller.biz.book.controller;

import com.alibaba.fastjson2.JSON;
import com.example.maincontroller.biz.book.po.Book;
import com.example.maincontroller.biz.book.service.BookService;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/books")
@RestController
public class BookController {

    @Resource
    private BookService bookService;

    @GetMapping("/{bookId}")
    public Book findBook(@PathVariable("bookId") Long bookId) {
        Book book = bookService.getBook(bookId);
        log.info("findBook, bookId: {}, book: {}", bookId, JSON.toJSONString(book));
        return book;
    }

    @GetMapping("/")
    public List<Book> allBooks() {
        List<Book> allBooks = bookService.findAllBooks();
        log.info("allBooks, allBooks: {}", JSON.toJSONString(allBooks));
        return allBooks;
    }
}
